@echo off
if "%~1"=="" (
    echo Usage: generate_flyway_filename.bat ^<migration_name^>
    exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0generate_flyway_filename.ps1" -Name "%~1"
