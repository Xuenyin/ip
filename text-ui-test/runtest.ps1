$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$classesDirectory = Join-Path $projectRoot "target/classes"
$mavenRepository = Join-Path $projectRoot "_temp/.m2"
$dataFile = Join-Path $projectRoot "data/gongrilla.txt"
$actualFile = Join-Path $PSScriptRoot "ACTUAL.TXT"
$expectedDataFile = Join-Path $PSScriptRoot "EXPECTED-DATA.TXT"

Remove-Item $dataFile -ErrorAction SilentlyContinue

mvn "-Dmaven.repo.local=$mavenRepository" -q -DskipTests compile
if ($LASTEXITCODE -ne 0) {
    throw "Maven compilation failed."
}
Get-Content (Join-Path $PSScriptRoot "INPUT.TXT") |
        java -cp $classesDirectory Gongrilla |
        Set-Content $actualFile

$expectedData = Get-Content $expectedDataFile
$actualData = Get-Content $dataFile
if (Compare-Object $actualData $expectedData) {
    throw "Saved task data did not match EXPECTED-DATA.TXT"
}

Write-Output "UI persistence test passed."
