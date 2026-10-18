@echo off
setlocal
cd /d "%~dp0"
echo Building current source for the CUI...
call mvnw.cmd -B -q package -DskipTests -Dpvp.buildDir=output/cmd-build
if errorlevel 1 exit /b 1
java -Dpvp.dataDir=data/cui-demo -cp "output/cmd-build/spire-hotseat.jar;output/cmd-build/lib/*" lab.pvp.view.cui.ConsoleLauncher
