param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,

    [string]$ComposeFile = "docker-compose.prod.yml",
    [string]$ServiceName = "postgres"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path ".env")) {
    Write-Error "Missing .env file. Copy .env.prod.example to .env and set production values."
    exit 1
}

if (!(Test-Path $BackupFile)) {
    Write-Error "Backup file not found: $BackupFile"
    exit 1
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

Write-Host "WARNING: This will restore backup into database '$env:POSTGRES_DB'."
Write-Host "Backup file: $BackupFile"
Read-Host "Press ENTER to continue or Ctrl+C to cancel"

Write-Host "Restoring PostgreSQL backup..."

Get-Content -Path $BackupFile -Encoding Byte -ReadCount 0 |
    docker compose -f $ComposeFile exec -T $ServiceName `
        pg_restore `
        -U $env:POSTGRES_USER `
        -d $env:POSTGRES_DB `
        --clean `
        --if-exists `
        --no-owner `
        --no-acl

Write-Host "Restore completed."