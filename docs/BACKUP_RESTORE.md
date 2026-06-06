# PostgreSQL Backup and Restore

این سند نحوه backup و restore دیتابیس PostgreSQL پروژه را توضیح می‌دهد.

## Production با Docker

برای production یا سرور Linux که Docker دارد:

```bash
scripts/backup-postgres.sh
```

Restore:

```bash
scripts/restore-postgres.sh backups/eshop-20260606T120000Z.dump
```

## Windows Local بدون Docker

برای سیستم توسعه ویندوزی که PostgreSQL local دارد:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\backup-postgres-local.ps1
```

Restore:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\restore-postgres-local.ps1 -BackupFile .\backups\eshop-local-20260606T120000Z.dump
```

اگر PostgreSQL 15 داری:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\backup-postgres-local.ps1 -PgBin "C:\Program Files\PostgreSQL\15\bin"
```

## نکته‌های مهم production

1. Backupها نباید commit شوند.
2. Backupها باید خارج از سرور اصلی هم نگهداری شوند.
3. Restore باید حداقل ماهی یک‌بار روی staging تست شود.
4. قبل از migrationهای مهم، backup دستی بگیر.
5. بعد از اولین production deploy، migrationهای قبلی را تغییر نده.