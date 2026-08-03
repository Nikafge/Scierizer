@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\package-jar.ps1"
exit /b %ERRORLEVEL%
