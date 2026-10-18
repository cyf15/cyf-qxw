@echo off
setlocal
cd /d "%~dp0"
if not exist "runtime\bin\java.exe" (
  echo Please extract the complete SpireDuel-Windows-x64.zip first.
  echo The portable runtime is missing. See README.md for build instructions.
  pause
  exit /b 1
)
if not exist "spire-hotseat.jar" goto incomplete
if not exist "lib\" goto incomplete
if not exist "logs" mkdir "logs"
"%~dp0runtime\bin\java.exe" -jar "%~dp0spire-hotseat.jar" >"%~dp0logs\startup.log" 2>&1
set "game_exit=%errorlevel%"
if "%game_exit%"=="0" exit /b 0
echo Game failed to start. Diagnostic log:
type "%~dp0logs\startup.log"
pause
exit /b %game_exit%
:incomplete
echo Package incomplete. Extract the whole ZIP including lib and runtime.
pause
exit /b 1
