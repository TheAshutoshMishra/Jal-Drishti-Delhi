@echo off
echo ========================================
echo Jal-Drishti Delhi LITE - Starting...
echo ========================================
echo.
echo This version works without GIS maps
echo (Saves disk space)
echo.

cd /d "%~dp0"

echo Starting Streamlit Application...
echo.
echo Your app will open in your default browser
echo Press Ctrl+C to stop the server
echo.

streamlit run app_nomap.py

pause
