@echo off
setlocal
cd /d "%~dp0"
if not exist "target\spire-hotseat.jar" (
  call mvnw.cmd -B package
  if errorlevel 1 exit /b 1
)
java -jar target\spire-hotseat.jar
if errorlevel 1 pause
