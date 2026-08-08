$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent
subst X: /D 2>$null | Out-Null
subst X: $projectRoot | Out-Null

Set-Location X:\
& .\gradlew.bat --no-daemon testDebugUnitTest
