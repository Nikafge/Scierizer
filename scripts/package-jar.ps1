$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$targetDir = Join-Path $projectRoot "target"
$jarOutput = Join-Path $targetDir "jar"
$libOutput = Join-Path $jarOutput "lib"
$appJar = Join-Path $jarOutput "Summarizer.jar"

function Require-Command($name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "Required command '$name' was not found. Install/use a JDK and ensure it is on PATH."
    }
}

Require-Command "java"

Push-Location $projectRoot
try {
    & ".\mvnw.cmd" -DskipTests clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$libOutput"
    if ($LASTEXITCODE -ne 0) {
        throw "Maven jar build failed with exit code $LASTEXITCODE."
    }

    $mainJar = Get-ChildItem -Path $targetDir -Filter "Summarizer-*.jar" |
            Where-Object { $_.Name -notmatch "sources|javadoc|tests" } |
            Select-Object -First 1
    if ($null -eq $mainJar) {
        throw "Could not find the built application jar in '$targetDir'."
    }

    New-Item -ItemType Directory -Force -Path $jarOutput | Out-Null
    Copy-Item -LiteralPath $mainJar.FullName -Destination $appJar -Force

    Write-Host "Runnable jar distribution created:"
    Write-Host $jarOutput
    Write-Host ""
    Write-Host "Run with:"
    Write-Host "java -jar `"$appJar`""
}
finally {
    Pop-Location
}
