# Add Node.js to PATH for this session
$env:Path = "C:\Program Files\nodejs;" + $env:Path

# Navigate to frontend directory
Set-Location frontend

# Start the frontend development server
npm run dev
