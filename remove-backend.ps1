# Script to remove the old Node.js backend directory
# Run this script after closing all applications that might have the folder open

Write-Host "Removing old Node.js backend..." -ForegroundColor Yellow

# Try to remove the directory
try {
    Remove-Item -Path "backend" -Recurse -Force -ErrorAction Stop
    Write-Host "✅ Successfully removed backend directory!" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to remove backend directory." -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting steps:" -ForegroundColor Yellow
    Write-Host "1. Close File Explorer windows showing the backend folder"
    Write-Host "2. Close any editors with backend files open"
    Write-Host "3. Close PowerShell/Command Prompt windows in the backend directory"
    Write-Host "4. Try running this script again"
    Write-Host ""
    Write-Host "Alternative: Run this command in Command Prompt:" -ForegroundColor Cyan
    Write-Host "  cd $PWD"
    Write-Host "  rmdir /s /q backend"
}
