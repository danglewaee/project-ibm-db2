param(
    [switch]$StartContainer,
    [switch]$StopContainer,
    [switch]$RunFullSuite,
    [int]$TimeoutSeconds = 900,
    [string]$ComposeFile = (Join-Path $PSScriptRoot "..\\infra\\docker-compose.db2.yml"),
    [string]$MavenExecutable = "",
    [string]$Db2Url = "jdbc:db2://localhost:50000/B2BOPS",
    [string]$Db2Username = "db2inst1",
    [string]$Db2Password = "password",
    [string]$ContainerName = "b2bops-db2"
)

$ErrorActionPreference = "Stop"

function Resolve-MavenExecutable {
    param(
        [string]$PreferredExecutable,
        [string]$RepoRoot
    )

    if ($PreferredExecutable) {
        if (Test-Path $PreferredExecutable) {
            return (Resolve-Path $PreferredExecutable).Path
        }

        throw "Provided Maven executable was not found: $PreferredExecutable"
    }

    $commands = @(
        (Get-Command mvn.cmd -ErrorAction SilentlyContinue),
        (Get-Command mvn -ErrorAction SilentlyContinue)
    ) | Where-Object { $_ }

    if ($commands.Count -gt 0) {
        return $commands[0].Source
    }

    $wrapperRoots = @()

    if ($env:USERPROFILE) {
        $wrapperRoots += (Join-Path $env:USERPROFILE ".m2\\wrapper\\dists")
    }

    $wrapperRoots += (Join-Path $RepoRoot ".m2\\wrapper\\dists")

    $userHomes = Get-ChildItem "C:\\Users" -Directory -ErrorAction SilentlyContinue |
            Select-Object -ExpandProperty FullName
    foreach ($userHome in $userHomes) {
        $wrapperRoots += (Join-Path $userHome ".m2\\wrapper\\dists")
    }

    $wrapperCandidate = $wrapperRoots |
            Where-Object { Test-Path $_ } |
            Get-Unique |
            ForEach-Object {
                Get-ChildItem -Path $_ -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue
            } |
            Where-Object { $_.FullName -match "\\\\bin\\\\mvn\\.cmd$" } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1

    if ($wrapperCandidate) {
        return $wrapperCandidate.FullName
    }

    throw "Could not find a Maven executable. Install Maven or run backend\\mvnw.cmd once to cache a wrapper distribution."
}

function Wait-ForDb2Ready {
    param(
        [string]$ContainerName,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        $logs = docker logs $ContainerName 2>&1 | Out-String
        if ($LASTEXITCODE -eq 0 -and $logs -match "Setup has completed") {
            return
        }

        Start-Sleep -Seconds 10
    }

    throw "Db2 container '$ContainerName' did not report readiness within $TimeoutSeconds seconds."
}

function Assert-DockerReady {
    $serverVersion = docker version --format "{{.Server.Version}}" 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($serverVersion)) {
        throw "Docker Desktop is not ready. Start Docker Desktop and confirm 'docker version' returns server info before running this script."
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$backendDir = Join-Path $repoRoot "backend"
$mavenExecutable = Resolve-MavenExecutable -PreferredExecutable $MavenExecutable -RepoRoot $repoRoot
$resolvedComposeFile = (Resolve-Path $ComposeFile).Path

if ($StartContainer) {
    Assert-DockerReady

    docker compose -f $resolvedComposeFile up -d
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start Db2 container from $resolvedComposeFile."
    }

    Wait-ForDb2Ready -ContainerName $ContainerName -TimeoutSeconds $TimeoutSeconds
}

$env:DB2_URL = $Db2Url
$env:DB2_USERNAME = $Db2Username
$env:DB2_PASSWORD = $Db2Password

$selectedTests = if ($RunFullSuite) {
    "BackendApplicationTests,SalesOrderControllerTest,StockCountControllerTest,AuditLogControllerTest"
} else {
    "BackendApplicationTests"
}

Push-Location $backendDir
try {
    & $mavenExecutable -q "-Dmaven.repo.local=../.m2" "-Dspring.profiles.active=db2,seed-demo-data" "-Dtest=$selectedTests" test
    if ($LASTEXITCODE -ne 0) {
        throw "Db2 smoke run failed."
    }
}
finally {
    Pop-Location

    if ($StopContainer) {
        docker compose -f $resolvedComposeFile down
    }
}
