$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$targetDir = Join-Path $projectRoot "target"
$packageInput = Join-Path $targetDir "package-input"
$windowsOutput = Join-Path $targetDir "windows"
$appImage = Join-Path $windowsOutput "Scierizer"
$appExe = Join-Path $appImage "Scierizer.exe"

function Require-Command($name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "Required command '$name' was not found. Install/use a JDK that includes it and ensure it is on PATH."
    }
}

Require-Command "java"
Require-Command "jpackage"

Push-Location $projectRoot
try {
    & ".\mvnw.cmd" -DskipTests clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$packageInput"
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package build failed with exit code $LASTEXITCODE."
    }

    $mainJar = Get-ChildItem -Path $targetDir -Filter "Scierizer-*.jar" |
            Where-Object { $_.Name -notmatch "sources|javadoc|tests" } |
            Select-Object -First 1
    if ($null -eq $mainJar) {
        throw "Could not find the built application jar in '$targetDir'."
    }

    New-Item -ItemType Directory -Force -Path $packageInput | Out-Null
    Copy-Item -LiteralPath $mainJar.FullName -Destination $packageInput -Force

    if (Test-Path $appImage) {
        Remove-Item -LiteralPath $appImage -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $windowsOutput | Out-Null

    & jpackage `
        --type app-image `
        --name Scierizer `
        --app-version 1.0.0 `
        --vendor "org.example" `
        --dest $windowsOutput `
        --input $packageInput `
        --main-jar $mainJar.Name `
        --main-class org.example.scierizer.Launcher `
        --java-options "-Dfile.encoding=UTF-8"
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed with exit code $LASTEXITCODE."
    }

    if (-not (Test-Path $appExe)) {
        throw "Packaging finished, but '$appExe' was not found."
    }

    Write-Host "Windows app image created:"
    Write-Host $appImage
    Write-Host ""
    Write-Host "Executable:"
    Write-Host $appExe
}
finally {
    Pop-Location
}
