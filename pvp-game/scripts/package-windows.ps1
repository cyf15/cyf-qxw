param([string]$JdkHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
if (-not $JdkHome) {
    $JdkHome = Split-Path (Split-Path (Get-Command javac.exe -ErrorAction Stop).Source -Parent) -Parent
}
$java = Join-Path $JdkHome 'bin/java.exe'
$jlink = Join-Path $JdkHome 'bin/jlink.exe'
if (-not (Test-Path $jlink)) { throw 'A Windows x64 JDK 17+ with jlink is required to build the package.' }
$propertyLog = Join-Path $env:TEMP ('spire-java-' + [guid]::NewGuid() + '.txt')
Start-Process -FilePath $java -ArgumentList '-XshowSettings:properties', '-version' -RedirectStandardError $propertyLog -WindowStyle Hidden -Wait
$properties = Get-Content -LiteralPath $propertyLog -Raw
Remove-Item -LiteralPath $propertyLog
if ($properties -notmatch 'os.arch = amd64') { throw 'Use a Windows x64 JDK.' }
$env:JAVA_HOME = $JdkHome
Push-Location $project
try {
    & .\mvnw.cmd -B '-Dpvp.buildDir=output/portable-build' clean verify
    if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed.' }
    # Unique staging keeps previous releases and user saves untouched.
    $release = Join-Path $project ('output/portable-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    $game = Join-Path $release 'SpireDuel'
    New-Item -ItemType Directory -Path $game -Force | Out-Null
    Copy-Item 'output/portable-build/spire-hotseat.jar' $game
    Copy-Item 'output/portable-build/lib' $game -Recurse
    & $jlink --module-path (Join-Path $JdkHome 'jmods') --add-modules ALL-MODULE-PATH --strip-debug --no-header-files --no-man-pages --output (Join-Path $game 'runtime')
    if ($LASTEXITCODE -ne 0) { throw 'Runtime creation failed.' }
    Copy-Item (Join-Path $project '../启动游戏.cmd') $game
    Copy-Item (Join-Path $project 'SOURCE_NOTES.md') $game
    $readme = @'
Spire Duel - Windows x64 portable edition

Extract the whole ZIP to a writable folder, then double-click 启动游戏.cmd.
No Java installation, Maven, source checkout, or network is required to play.
Keep runtime/, lib/ and spire-hotseat.jar together with the launcher.
Saves are created in data/. Keep that folder when updating the game.
Startup diagnostics: logs/startup.log.
Requires Windows 10/11 x64 and a working OpenGL graphics driver.
Original game asset provenance: SOURCE_NOTES.md.
Bundled Java license notices: runtime/legal/.
'@
    [IO.File]::WriteAllText((Join-Path $game '使用说明.txt'), $readme, [Text.UTF8Encoding]::new($true))
    $zip = Join-Path $release 'SpireDuel-Windows-x64.zip'
    Compress-Archive -LiteralPath $game -DestinationPath $zip
    Get-FileHash $zip -Algorithm SHA256 | Format-List
    Write-Host "Portable folder: $game"
    Write-Host "Download package: $zip"
} finally { Pop-Location }
