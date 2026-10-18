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
    $inputDir = Join-Path $release 'input'
    New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
    Copy-Item 'output/portable-build/spire-hotseat.jar' $inputDir
    Copy-Item 'output/portable-build/lib' $inputDir -Recurse
    & $jlink --module-path (Join-Path $JdkHome 'jmods') --add-modules ALL-MODULE-PATH --strip-debug --no-header-files --no-man-pages --output (Join-Path $release 'runtime')
    if ($LASTEXITCODE -ne 0) { throw 'Runtime creation failed.' }
    & (Join-Path $JdkHome 'bin/jpackage.exe') --type app-image --name SpireDuel --input $inputDir --main-jar spire-hotseat.jar --dest $release --runtime-image (Join-Path $release 'runtime') --java-options '-Dpvp.dataDir=$APPDIR/../data' --app-version 0.1.0 --vendor 'Spire Duel'
    if ($LASTEXITCODE -ne 0) { throw 'Native launcher creation failed.' }
    Copy-Item (Join-Path $project 'SOURCE_NOTES.md') $game
    $readme = @'
Spire Duel - Windows x64 portable edition

Extract the whole ZIP to a writable folder, then double-click SpireDuel.exe.
No Java installation, Maven, source checkout, or network is required to play.
Keep app/ and runtime/ together with SpireDuel.exe.
Saves are created in data/. Keep that folder when updating the game.
The native launcher starts without a console window.
Requires Windows 10/11 x64 and a working OpenGL graphics driver.
Original game asset provenance: SOURCE_NOTES.md.
Bundled Java license notices: runtime/legal/.
'@
    [IO.File]::WriteAllText((Join-Path $game '使用说明.txt'), $readme, [Text.UTF8Encoding]::new($true))
    $zip = Join-Path $release 'SpireDuel-Windows-x64.zip'
    # Put the EXE at the extraction root so extracting into aut lab gives aut lab/SpireDuel.exe.
    Compress-Archive -Path (Join-Path $game '*') -DestinationPath $zip
    # Deliver the project folder together with the ready-to-run game.
    $workspace = Split-Path $project -Parent
    $submission = Join-Path $release 'submission'
    $wholeProject = Join-Path $submission 'aut lab'
    New-Item -ItemType Directory -Path $wholeProject -Force | Out-Null
    foreach ($repoRelative in @('', 'MySlayTheSpire')) {
        $repo = Join-Path $workspace $repoRelative
        $tracked = & git -C $repo -c core.quotepath=false ls-files
        if ($LASTEXITCODE -ne 0) { throw 'Could not enumerate project source files.' }
        foreach ($file in $tracked) {
            $source = Join-Path $repo $file
            if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { continue }
            $destination = Join-Path (Join-Path $wholeProject $repoRelative) $file
            New-Item -ItemType Directory -Path (Split-Path $destination -Parent) -Force | Out-Null
            Copy-Item -LiteralPath $source -Destination $destination
        }
    }
    foreach ($item in Get-ChildItem -LiteralPath $game) {
        Copy-Item -LiteralPath $item.FullName -Destination $wholeProject -Recurse -Force
    }
    Compress-Archive -LiteralPath $wholeProject -DestinationPath (Join-Path $release 'aut-lab-Windows-x64.zip')
    Get-FileHash $zip -Algorithm SHA256 | Format-List
    Write-Host "Portable folder: $game"
    Write-Host "Download package: $zip"
} finally { Pop-Location }
