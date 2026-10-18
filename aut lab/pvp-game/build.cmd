@echo off
setlocal
cd /d "%~dp0"
call mvnw.cmd -B verify
exit /b %errorlevel%
