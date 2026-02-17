# Remove Old Node.js Backend

## Status
The old Node.js backend in the `backend/` directory is no longer needed since the Java backend is fully operational.

## Why Remove It?
- ✅ Java backend is working and tested
- ✅ Frontend is connected to Java backend
- ✅ All APIs migrated and functional
- ✅ Database schema compatible
- ✅ JWT tokens interoperable
- ❌ Node.js backend is obsolete

## Current Issue
The `backend/` directory cannot be deleted automatically because it's being used by another process (likely File Explorer or an editor).

## Manual Removal Steps

### Option 1: Close and Delete
1. Close any File Explorer windows showing the `backend` folder
2. Close any editors with files from `backend` folder open
3. Run this command:
   ```powershell
   Remove-Item -Path "backend" -Recurse -Force
   ```

### Option 2: Using File Explorer
1. Close all applications that might have the folder open
2. Navigate to the project root in File Explorer
3. Right-click the `backend` folder
4. Select "Delete"

### Option 3: Restart and Delete
1. Restart your computer (closes all file handles)
2. Delete the `backend` folder

### Option 4: Use Command Prompt
```cmd
cd C:\Users\f5065879\dev\muti-tentant-saas-poc
rmdir /s /q backend
```

## What's Being Removed

The `backend/` directory contains:
```
backend/
├── src/
│   ├── index.js           # Node.js Express server
│   ├── middleware/        # Auth and error handling
│   ├── models/           # Database models
│   ├── routes/           # API routes
│   └── services/         # Business logic
├── .env                  # Environment variables
├── .env.example          # Example env file
└── package.json          # Node.js dependencies
```

## What to Keep

✅ **Keep these directories:**
- `java-backend/` - The new Java Spring Boot backend
- `frontend/` - React frontend (already configured for Java backend)
- `docker-compose.yml` - Infrastructure (PostgreSQL, Camunda)

## Verification After Removal

After removing the `backend/` directory, verify the system still works:

1. **Check Java backend is running:**
   ```bash
   curl http://localhost:3000/health
   ```

2. **Check frontend is accessible:**
   ```bash
   curl http://localhost:5173
   ```

3. **Test login:**
   - Open http://localhost:5173
   - Login with test@example.com / password123
   - Should work normally

## Backup (Optional)

If you want to keep a backup before deletion:

```powershell
# Create a backup
Compress-Archive -Path "backend" -DestinationPath "backend-nodejs-backup.zip"

# Then delete
Remove-Item -Path "backend" -Recurse -Force
```

## Alternative: Archive Instead of Delete

If you're not ready to permanently delete:

```powershell
# Rename to indicate it's archived
Rename-Item -Path "backend" -NewName "backend-nodejs-archived"
```

## Update Documentation

After removing the backend folder, update these files:

### 1. Update `SETUP.md`
Remove references to Node.js backend installation and startup.

### 2. Update `package.json` (root level, if exists)
Remove any scripts that reference the Node.js backend.

### 3. Update `.gitignore` (if needed)
Remove backend-specific ignore patterns.

## Migration Complete! 🎉

Once the old backend is removed:
- ✅ Clean project structure
- ✅ Only Java backend remains
- ✅ No confusion about which backend to use
- ✅ Reduced maintenance burden
- ✅ Migration officially complete

## Troubleshooting

### "Access Denied" or "File in Use"
- Close Visual Studio Code or any IDE
- Close File Explorer windows
- Close PowerShell/Command Prompt windows in that directory
- Try again

### Want to Keep for Reference
Instead of deleting, move it outside the project:
```powershell
Move-Item -Path "backend" -Destination "C:\Temp\backend-nodejs-reference"
```

### Accidentally Deleted and Need to Restore
If you have git:
```bash
git checkout backend/
```
