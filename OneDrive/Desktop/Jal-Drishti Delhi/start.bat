@echo off
echo ========================================
echo Jal-Drishti Delhi - Starting...
echo ========================================
echo.

REM Check if in correct directory
cd /d "%~dp0"

echo Checking Python installation...
python --version
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo Checking required packages...
pip show streamlit-folium >nul 2>&1
if errorlevel 1 (
    echo.
    echo WARNING: streamlit-folium is not installed!
    echo.
    echo Option 1: Install full version (requires disk space)
    echo   Command: pip install streamlit-folium folium
    echo.
    echo Option 2: Use LITE version (no map, works now!)
    echo   Running LITE version automatically...
    echo.
    echo ========================================
    echo Starting LITE Version (No Map)...
    echo ========================================
    echo.
    streamlit run app_nomap.py
    pause
    exit /b 0
)

echo.
echo ========================================
echo Starting FULL Version (With Map)...
echo ========================================
echo.
echo Your app will open in your default browser
echo Press Ctrl+C to stop the server
echo.

streamlit run app.py

pause
