param(
    [switch]$Build,
    [switch]$StopStack,
    [int]$TimeoutSeconds = 1200,
    [string]$ComposeFile = (Join-Path $PSScriptRoot "..\\infra\\docker-compose.stack.yml"),
    [string]$HealthUrl = "http://localhost:8080/actuator/health",
    [string]$SystemInfoUrl = "http://localhost:8080/api/v1/system/info"
)

$ErrorActionPreference = "Stop"

function Assert-DockerReady {
    $serverVersion = docker version --format "{{.Server.Version}}" 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($serverVersion)) {
        throw "Docker Desktop is not ready. Start Docker Desktop and confirm 'docker version' returns server info before running this script."
    }
}

function Wait-ForHttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 15
            return $response
        }
        catch {
            Start-Sleep -Seconds 10
        }
    }

    throw "Endpoint '$Url' did not become ready within $TimeoutSeconds seconds."
}

Assert-DockerReady

$resolvedComposeFile = (Resolve-Path $ComposeFile).Path
$composeArgs = @("compose", "-f", $resolvedComposeFile, "up", "-d")

if ($Build) {
    $composeArgs += "--build"
}

docker @composeArgs
if ($LASTEXITCODE -ne 0) {
    throw "Failed to start the full stack from $resolvedComposeFile."
}

try {
    $health = Wait-ForHttpReady -Url $HealthUrl -TimeoutSeconds $TimeoutSeconds
    $systemInfo = Wait-ForHttpReady -Url $SystemInfoUrl -TimeoutSeconds 60

    Write-Output "Health:"
    $health | ConvertTo-Json -Depth 8

    Write-Output "System Info:"
    $systemInfo | ConvertTo-Json -Depth 8
}
finally {
    if ($StopStack) {
        docker compose -f $resolvedComposeFile down
    }
}
