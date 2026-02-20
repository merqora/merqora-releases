# Rendly AI Support - Development Server Script (Windows)
# Run this from the ai-support directory

Write-Host "[*] Starting Rendly AI Support Development Server..." -ForegroundColor Cyan

# Check if Python is installed
if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host "[X] Python not found. Please install Python 3.11+" -ForegroundColor Red
    exit 1
}

# Navigate to python directory
Set-Location -Path "$PSScriptRoot\..\python"

# Create virtual environment if it doesn't exist
if (-not (Test-Path "venv")) {
    Write-Host "[*] Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
}

# Activate virtual environment
Write-Host "[*] Activating virtual environment..." -ForegroundColor Yellow
& ".\venv\Scripts\Activate.ps1"

# Install dependencies
Write-Host "[*] Installing dependencies..." -ForegroundColor Yellow
pip install -r requirements.txt --quiet

# Check for .env file
if (-not (Test-Path ".env")) {
    Write-Host "[!] No .env file found. Creating from .env.example..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "[!] Please edit .env file with your configuration" -ForegroundColor Yellow
}

# Run the server
Write-Host ""
Write-Host "[OK] Starting FastAPI server on http://localhost:8000" -ForegroundColor Green
Write-Host "[OK] API Docs: http://localhost:8000/docs" -ForegroundColor Green
Write-Host ""

uvicorn main:app --host 0.0.0.0 --port 8000 --reload
