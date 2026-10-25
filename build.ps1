# Builds the signed NEXT Dialer release APK and copies it into builds/.
# Requires the portable JDK 21 in tools/jdk21 and the Android SDK (local.properties).

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$jdk = Get-ChildItem (Join-Path $root "tools\jdk21") -Directory -ErrorAction SilentlyContinue |
    Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
    Select-Object -First 1

if ($null -eq $jdk) {
    Write-Error "JDK 21 not found under tools\jdk21. Download Temurin 21 into that folder."
}

$env:JAVA_HOME = $jdk.FullName
$env:Path = "$($jdk.FullName)\bin;$env:Path"

Write-Host "JAVA_HOME = $env:JAVA_HOME"
Write-Host "Building NEXT Dialer..."

& (Join-Path $root "gradlew.bat") --no-daemon assembleRelease
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed with exit code $LASTEXITCODE."
}

$apk = Join-Path $root "app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apk)) {
    Write-Error "Release APK not found at $apk"
}

$version = "0.2.0-rc1"
$destDir = Join-Path $root "builds"
New-Item -ItemType Directory -Force $destDir | Out-Null
$dest = Join-Path $destDir "next-dialer-$version.apk"
Copy-Item $apk $dest -Force

$size = [math]::Round((Get-Item $dest).Length / 1MB, 2)
Write-Host "APK: $dest ($size MB)"
