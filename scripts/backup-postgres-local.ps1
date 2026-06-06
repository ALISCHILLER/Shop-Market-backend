param(
    [string]$PgBin = "C:\Program Files\PostgreSQL\16\bin",
    [string]$HostName = "localhost",
    [int]$Port = 5432,
    [string]$Database = "eshop",
    [string]$Username = "eshop",
    [string]$BackupDir = "backups"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

$pgDump = Join-Path $PgBin "pg_dump.exe"

if (!(Test-Path $pgDump)) {
    Write-Error "pg_dump.exe not found at: $pgDump"
    exit 1
}

$timestamp = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$outputFile = Join-Path $BackupDir "eshop-local-$timestamp.dump"

Write-Host "Creating local PostgreSQL backup..."
Write-Host "Database: $Database"
Write-Host "Output: $outputFile"

& $pgDump `
    -h $HostName `
    -p $Port `
    -U $Username `
    -d $Database `
    -Fc `
    --no-owner `
    --no-acl `
    -f $outputFile

Write-Host "Backup completed: $outputFile"