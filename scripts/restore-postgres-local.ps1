param(
    [Parameter(Mandatory = $true)]
    [string]$BackupFile,

    [string]$PgBin = "C:\Program Files\PostgreSQL\16\bin",
    [string]$HostName = "localhost",
    [int]$Port = 5432,
    [string]$Database = "eshop",
    [string]$Username = "eshop"
)

$ErrorActionPreference = "Stop"

if (!(Test-Path $BackupFile)) {
    Write-Error "Backup file not found: $BackupFile"
    exit 1
}

$pgRestore = Join-Path $PgBin "pg_restore.exe"

if (!(Test-Path $pgRestore)) {
    Write-Error "pg_restore.exe not found at: $pgRestore"
    exit 1
}

Write-Host "WARNING: This will restore backup into database '$Database'."
Write-Host "Backup file: $BackupFile"
Read-Host "Press ENTER to continue or Ctrl+C to cancel"

& $pgRestore `
    -h $HostName `
    -p $Port `
    -U $Username `
    -d $Database `
    --clean `
    --if-exists `
    --no-owner `
    --no-acl `
    $BackupFile

Write-Host "Restore completed."