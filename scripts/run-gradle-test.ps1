$ErrorActionPreference = 'Stop'

if (-not (Get-PSDrive -Name X -ErrorAction SilentlyContinue)) {
    subst X: E:\软件 | Out-Null
}

Set-Location X:\
& .\gradlew.bat --no-daemon testDebugUnitTest
