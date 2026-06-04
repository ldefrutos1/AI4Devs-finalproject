# Ejecuta los E2E de UI (Playwright) del flujo de alta de ejemplar.
# Estrategia y variantes: docs/engineering/testing-e2e.md
#
# Por defecto: variante autocontenida en Docker (la que corre en CI/PR).
#   Compila los jars (catalog/media/gateway), levanta docker-compose.e2e.yml,
#   ejecuta Playwright como contenedor y baja el stack (volúmenes efímeros).
#
# Uso:
#   .\scripts\dev\test-e2e.ps1                 # variante Docker (build + up + down -v)
#   .\scripts\dev\test-e2e.ps1 -SkipBuild      # Docker sin recompilar los jars
#   .\scripts\dev\test-e2e.ps1 -KeepStack      # Docker, no baja el stack al terminar
#   .\scripts\dev\test-e2e.ps1 -Local          # contra entorno YA levantado (front+gateway+...)
#   .\scripts\dev\test-e2e.ps1 -Local -SkipInstall
#   .\scripts\dev\test-e2e.ps1 -Local -Ui      # abre el runner UI de Playwright
#   .\scripts\dev\test-e2e.ps1 -Local -BaseUrl http://localhost:5173

[CmdletBinding(DefaultParameterSetName = 'Docker')]
param(
    [Parameter(ParameterSetName = 'Docker')]
    [switch]$SkipBuild,

    [Parameter(ParameterSetName = 'Docker')]
    [switch]$KeepStack,

    [Parameter(Mandatory, ParameterSetName = 'Local')]
    [switch]$Local,

    [Parameter(ParameterSetName = 'Local')]
    [switch]$SkipInstall,

    [Parameter(ParameterSetName = 'Local')]
    [switch]$Ui,

    [Parameter(ParameterSetName = 'Local')]
    [string]$BaseUrl = 'http://localhost:5173'
)

. "$PSScriptRoot\_common.ps1"

$repoRoot = Get-MtlRepoRoot

if ($Local) {
    # --- Variante A: entorno ya levantado --------------------------------------
    $e2eDir = Join-Path $repoRoot 'e2e'
    Assert-CommandInPath -Name 'npm' -Hint 'Instala Node.js (npm incluido).'

    Write-MtlInfo "E2E (local): Playwright contra $BaseUrl (asume stack arriba)."

    Invoke-MtlInDirectory -Path $e2eDir -Action {
        if (-not $SkipInstall) {
            Write-MtlInfo 'npm install…'
            npm install
            if ($LASTEXITCODE -ne 0) { throw "npm install terminó con código $LASTEXITCODE" }

            Write-MtlInfo 'npx playwright install --with-deps chromium…'
            npx playwright install --with-deps chromium
            if ($LASTEXITCODE -ne 0) { throw "playwright install terminó con código $LASTEXITCODE" }
        }

        $env:BASE_URL = $BaseUrl
        $npmScript = if ($Ui) { 'e2e:ui' } else { 'e2e' }
        Write-MtlInfo "npm run $npmScript…"
        npm run $npmScript
        if ($LASTEXITCODE -ne 0) { throw "Playwright (npm run $npmScript) terminó con código $LASTEXITCODE" }
    }

    Write-MtlOk 'E2E (local): pruebas completadas.'
    return
}

# --- Variante B: self-contained en Docker (CI/PR) ------------------------------
$servicesDir = Join-Path $repoRoot 'services'
$composeDir = Join-Path $repoRoot 'infra\compose'
$composeFile = 'docker-compose.e2e.yml'

Assert-CommandInPath -Name 'docker' -Hint 'Instala Docker Desktop y arráncalo.'

if (-not $SkipBuild) {
    Assert-CommandInPath -Name 'mvn' -Hint 'Instala Maven y añádelo al PATH.'
    Write-MtlInfo 'E2E (Docker): compilando jars de catalog/media/gateway…'
    Invoke-MtlInDirectory -Path $servicesDir -Action {
        mvn -pl catalog-service,media-service,api-gateway -am package -DskipTests
        if ($LASTEXITCODE -ne 0) { throw "mvn package terminó con código $LASTEXITCODE" }
    }
}

Write-MtlInfo 'E2E (Docker): levantando stack autocontenido y ejecutando Playwright…'
Invoke-MtlInDirectory -Path $composeDir -Action {
    $e2eExit = 0
    try {
        docker compose -f $composeFile up --build --abort-on-container-exit --exit-code-from playwright
        $e2eExit = $LASTEXITCODE
    }
    finally {
        if (-not $KeepStack) {
            Write-MtlInfo 'E2E (Docker): bajando stack (down -v, volúmenes efímeros)…'
            docker compose -f $composeFile down -v
        }
        else {
            Write-MtlWarn "E2E (Docker): stack en marcha (-KeepStack). Para limpiar: docker compose -f $composeFile down -v"
        }
    }
    if ($e2eExit -ne 0) { throw "Playwright (stack Docker) terminó con código $e2eExit" }
}

Write-MtlOk 'E2E (Docker): pruebas completadas. Informe en e2e/playwright-report/.'
