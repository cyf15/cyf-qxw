@echo off
setlocal
cd /d "%~dp0"
if not exist "target\spire-hotseat.jar" (
  call mvnw.cmd -B package
  if errorlevel 1 exit /b 1
)
java -cp "target\spire-hotseat.jar;target\lib\*" lab.pvp.view.cui.ConsoleLauncher
