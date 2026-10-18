@echo off
setlocal
cd /d "%~dp0pvp-game"
title Spire Duel - CUI demonstration
:menu
cls
echo SPIRE DUEL - PROJECT 1 CUI
echo.
echo 1. Build and launch CUI
echo 2. Run JUnit tests
echo 0. Exit
echo.
choice /c 120 /n /m "Choose 1, 2 or 0: "
if errorlevel 3 exit /b 0
if errorlevel 2 goto tests
call run-cui.cmd
pause
goto menu
:tests
call mvnw.cmd -B verify -Dpvp.buildDir=output/cmd-build
if errorlevel 1 (echo TESTS FAILED) else (echo TESTS PASSED)
pause
goto menu
