# Ejecuta tests Maven del backend (desde services/).
# Uso: .\scripts\dev\test-backend.ps1
#      .\scripts\dev\test-backend.ps1 -Quick   # solo mvn test (Surefire)

[CmdletBinding()]
param(
    [switch]$Quick
)

. "$PSScriptRoot\_common.ps1"

$repoRoot = Get-MtlRepoRoot
$servicesDir = Join-Path $repoRoot 'services'
Assert-CommandInPath -Name 'mvn' -Hint 'Instala Maven y añádelo al PATH.'

$goal = if ($Quick) { 'test' } else { 'verify' }
Write-MtlInfo "Backend: mvn $goal en services/ ($(if ($Quick) { 'solo unitarios' } else { 'unitarios + IT Failsafe' }))"

Invoke-MtlInDirectory -Path $servicesDir -Action {
    # $goal, no @goal: @ sobre un string en PowerShell hace splat por carácter (verify -> v, e, r…).
    mvn $goal
    if ($LASTEXITCODE -ne 0) {
        throw "mvn $goal terminó con código $LASTEXITCODE"
    }
}

Write-MtlOk "Backend: mvn $goal completado."
