param(
    [string]$ComposeFile = "docker-compose.prod.yml",
    [string]$ServiceName = "postgres",
    [string]$BackupDir = "backups"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path ".env")) {
    Write-Error "Missing .env file. Copy .env.prod.example to .env and set production values."
    exit 1
}

if (!(Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

Get-Content ".env" | ForEach-Object {
    if ($_ -match "^\s*#" -or $_ -match "^\s*$") {
        return
    }

    $parts = $_ -split "=", 2
    if ($parts.Count -eq 2) {
        [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
    }
}

if ([string]::IsNullOrWhiteSpace($env:POSTGRES_DB)) {
    Write-Error "POSTGRES_DB is required in .env"
    exit 1
}

if ([string]::IsNullOrWhiteSpace($env:POSTGRES_USER)) {
    Write-Error "POSTGRES_USER is required in .env"
    exit 1
}

$timestamp = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$outputFile = Join-Path $BackupDir "eshop-$timestamp.dump"

Write-Host "Creating PostgreSQL backup..."
Write-Host "Compose file: $ComposeFile"
Write-Host "Database: $env:POSTGRES_DB"
Write-Host "Output: $outputFile"

docker compose -f $ComposeFile exec -T $ServiceName `
    pg_dump `
    -U $env:POSTGRES_USER `
    -d $env:POSTGRES_DB `
    -Fc `
    --no-owner `
    --no-acl `
    | Set-Content -Path $outputFile -Encoding Byte

Write-Host "Backup completed: $outputFile"