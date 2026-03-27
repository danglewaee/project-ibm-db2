param(
    [string]$IBMCloudExecutable = "",
    [string]$ApiEndpoint = "https://cloud.ibm.com",
    [string]$Region = "us-south",
    [string]$ResourceGroup = "Default",
    [string]$ProjectName = "b2bops-demo",
    [string]$ProjectEndpoint = "public",
    [string]$AppName = "b2bops-backend",
    [string]$Visibility = "public",
    [string]$SecretName = "b2bops-db2",
    [string]$SecretEnvFile = (Join-Path $PSScriptRoot "..\\infra\\codeengine\\db2-secret.env"),
    [string]$BuildSource = (Join-Path $PSScriptRoot "..\\backend"),
    [string]$ApiKey = "",
    [string]$ApiKeyFile = "",
    [switch]$UseSso,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Resolve-IbmCloudExecutable {
    if ($IBMCloudExecutable) {
        if (-not (Test-Path $IBMCloudExecutable)) {
            throw "Specified IBM Cloud CLI path was not found: $IBMCloudExecutable"
        }
        return (Resolve-Path $IBMCloudExecutable).Path
    }

    $wingetPackagePath = "C:\Users\$env:USERNAME\AppData\Local\Microsoft\WinGet\Packages\IBM.Cloud.CLI_Microsoft.Winget.Source_8wekyb3d8bbwe\IBM_Cloud_CLI\ibmcloud.exe"
    $wingetLinkPath = "C:\Users\$env:USERNAME\AppData\Local\Microsoft\WinGet\Links\ibmcloud.exe"
    $candidates = @(
        (Get-Command ibmcloud -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue),
        "C:\Program Files\IBM\Cloud\bin\ibmcloud.exe",
        $wingetPackagePath,
        $wingetLinkPath
    ) | Where-Object { $_ }

    foreach ($candidate in $candidates) {
        if (-not (Test-Path $candidate)) {
            continue
        }

        try {
            & $candidate --version *> $null
            if ($LASTEXITCODE -eq 0) {
                return (Resolve-Path $candidate).Path
            }
        }
        catch {
            continue
        }
    }

    throw "Could not find 'ibmcloud'. Install IBM Cloud CLI first."
}

function Invoke-IbmCloud {
    param(
        [string]$Executable,
        [string[]]$Arguments,
        [switch]$AllowFailure
    )

    $quotedArgs = $Arguments | ForEach-Object {
        if ($_ -match "\s") { '"' + $_ + '"' } else { $_ }
    }
    Write-Host ("ibmcloud " + ($quotedArgs -join " "))

    if ($DryRun) {
        return @{
            ExitCode = 0
            Output = @()
        }
    }

    $output = & $Executable @Arguments 2>&1
    $exitCode = $LASTEXITCODE

    if (-not $AllowFailure -and $exitCode -ne 0) {
        $message = ($output | Out-String).Trim()
        throw "IBM Cloud CLI command failed with exit code $exitCode.`n$message"
    }

    return @{
        ExitCode = $exitCode
        Output = $output
    }
}

function Assert-Value {
    param(
        [string]$Value,
        [string]$Message
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw $Message
    }
}

function Test-IbmCloudTarget {
    param([string]$Executable)

    $result = Invoke-IbmCloud -Executable $Executable -Arguments @("target") -AllowFailure
    return $result.ExitCode -eq 0
}

function Ensure-IbmCloudLogin {
    param([string]$Executable)

    if (Test-IbmCloudTarget -Executable $Executable) {
        Invoke-IbmCloud -Executable $Executable -Arguments @("target", "-r", $Region, "-g", $ResourceGroup) | Out-Null
        return
    }

    if ($UseSso) {
        Invoke-IbmCloud -Executable $Executable -Arguments @(
            "login",
            "-a", $ApiEndpoint,
            "--sso",
            "-r", $Region,
            "-g", $ResourceGroup
        ) | Out-Null
        return
    }

    if ($ApiKeyFile) {
        $resolvedApiKeyFile = (Resolve-Path $ApiKeyFile).Path
        Invoke-IbmCloud -Executable $Executable -Arguments @(
            "login",
            "-a", $ApiEndpoint,
            "--apikey", "@$resolvedApiKeyFile",
            "-r", $Region,
            "-g", $ResourceGroup
        ) | Out-Null
        return
    }

    if ($ApiKey) {
        Invoke-IbmCloud -Executable $Executable -Arguments @(
            "login",
            "-a", $ApiEndpoint,
            "--apikey", $ApiKey,
            "-r", $Region,
            "-g", $ResourceGroup
        ) | Out-Null
        return
    }

    throw "Not logged in to IBM Cloud. Re-run with -UseSso, -ApiKey, or -ApiKeyFile."
}

function Ensure-Project {
    param(
        [string]$Executable,
        [string]$Name
    )

    $project = Invoke-IbmCloud -Executable $Executable -Arguments @(
        "ce", "project", "get",
        "--name", $Name,
        "--output", "json"
    ) -AllowFailure

    if ($project.ExitCode -ne 0) {
        Invoke-IbmCloud -Executable $Executable -Arguments @(
            "ce", "project", "create",
            "--name", $Name,
            "--endpoint", $ProjectEndpoint,
            "--wait"
        ) | Out-Null
    }

    Invoke-IbmCloud -Executable $Executable -Arguments @(
        "ce", "project", "select",
        "--name", $Name
    ) | Out-Null
}

function Ensure-Secret {
    param(
        [string]$Executable,
        [string]$Name,
        [string]$EnvFile
    )

    $secret = Invoke-IbmCloud -Executable $Executable -Arguments @(
        "ce", "secret", "get",
        "--name", $Name,
        "--output", "json"
    ) -AllowFailure

    if ($secret.ExitCode -eq 0) {
        Invoke-IbmCloud -Executable $Executable -Arguments @(
            "ce", "secret", "update",
            "--name", $Name,
            "--from-env-file", $EnvFile
        ) | Out-Null
        return
    }

    Invoke-IbmCloud -Executable $Executable -Arguments @(
        "ce", "secret", "create",
        "--name", $Name,
        "--format", "generic",
        "--from-env-file", $EnvFile
    ) | Out-Null
}

function Ensure-App {
    param(
        [string]$Executable,
        [string]$Name,
        [string]$Secret
    )

    $resolvedBuildSource = (Resolve-Path $BuildSource).Path
    $liveProbeArgs = @(
        "--probe-live", "type=http",
        "--probe-live", "path=/actuator/health/liveness",
        "--probe-live", "port=8080",
        "--probe-live", "initial-delay=60",
        "--probe-live", "timeout=10",
        "--probe-live", "interval=30",
        "--probe-live", "failure-threshold=5"
    )
    $readyProbeArgs = @(
        "--probe-ready", "type=http",
        "--probe-ready", "path=/actuator/health/readiness",
        "--probe-ready", "port=8080",
        "--probe-ready", "initial-delay=60",
        "--probe-ready", "timeout=10",
        "--probe-ready", "interval=15",
        "--probe-ready", "failure-threshold=5"
    )
    $baseArgs = @(
        "--name", $Name,
        "--build-source", $resolvedBuildSource,
        "--build-context-dir", ".",
        "--build-dockerfile", "Dockerfile",
        "--build-strategy", "dockerfile",
        "--build-timeout", "3600",
        "--port", "http1:8080",
        "--visibility", $Visibility,
        "--request-timeout", "300",
        "--max-scale", "2",
        "--env", "SPRING_PROFILES_ACTIVE=db2,seed-demo-data",
        "--env", "APP_RUNTIME_MODE=code-engine",
        "--env", "APP_PERSISTENCE_MODE=jpa-flyway-db2-code-engine",
        "--env-from-secret", "${Secret}:DB2_URL,DB2_USERNAME,DB2_PASSWORD",
        "--wait",
        "--wait-timeout", "3600"
    ) + $liveProbeArgs + $readyProbeArgs

    $app = Invoke-IbmCloud -Executable $Executable -Arguments @(
        "ce", "app", "get",
        "--name", $Name,
        "--output", "json"
    ) -AllowFailure

    if ($app.ExitCode -eq 0) {
        Invoke-IbmCloud -Executable $Executable -Arguments (@(
            "ce", "app", "update"
        ) + $baseArgs + @(
            "--env-from-secret-rm", $Secret,
            "--env-from-secret", "${Secret}:DB2_URL,DB2_USERNAME,DB2_PASSWORD",
            "--rebuild"
        )) | Out-Null
        return
    }

    Invoke-IbmCloud -Executable $Executable -Arguments (@(
        "ce", "app", "create"
    ) + $baseArgs) | Out-Null
}

$ibmcloud = Resolve-IbmCloudExecutable
$resolvedSecretEnvFile = (Resolve-Path $SecretEnvFile -ErrorAction SilentlyContinue)

Assert-Value -Value $ProjectName -Message "Project name is required."
Assert-Value -Value $AppName -Message "App name is required."
Assert-Value -Value $SecretName -Message "Secret name is required."

if (-not $resolvedSecretEnvFile) {
    throw "Secret env file not found: $SecretEnvFile. Copy infra\\codeengine\\db2-secret.env.template to infra\\codeengine\\db2-secret.env and fill the Db2 values."
}

if ($ProjectEndpoint -notin @("public", "private")) {
    throw "ProjectEndpoint must be 'public' or 'private'."
}

if ($Visibility -notin @("public", "private", "project")) {
    throw "Visibility must be 'public', 'private', or 'project'."
}

Ensure-IbmCloudLogin -Executable $ibmcloud
Ensure-Project -Executable $ibmcloud -Name $ProjectName
Ensure-Secret -Executable $ibmcloud -Name $SecretName -EnvFile $resolvedSecretEnvFile.Path
Ensure-App -Executable $ibmcloud -Name $AppName -Secret $SecretName

$appUrl = Invoke-IbmCloud -Executable $ibmcloud -Arguments @(
    "ce", "app", "get",
    "--name", $AppName,
    "--output", "url"
)

Write-Host ""
if ($DryRun) {
    Write-Host "Code Engine dry run completed."
}
else {
    Write-Host "Code Engine app deployed."
    Write-Host ("App URL: " + (($appUrl.Output | Out-String).Trim()))
}
