# Ejecuta los E2E de UI (Playwright) del flujo de alta de ejemplar.
# Estrategia y variantes: docs/engineering/testing-e2e.md
#
# Por defecto: variante autocontenida en Docker (la que corre en CI/PR).
#   Compila los jars, levanta el stack en segundo plano, ejecuta en serie
#   system-e2e-tests (Maven) y Playwright, y baja el stack (volumenes efimeros).
#
# Uso:
#   .\scripts\dev\test-e2e.ps1                 # variante Docker (build + run + down -v)
#   .\scripts\dev\test-e2e.ps1 -SkipBuild      # Docker sin recompilar los jars
#   .\scripts\dev\test-e2e.ps1 -KeepStack      # Docker, no baja el stack al terminar
#   .\scripts\dev\test-e2e.ps1 -Local          # contra entorno YA levantado (front+gateway+...)
#   .\scripts\dev\test-e2e.ps1 -Local -SkipInstall
#   .\scripts\dev\test-e2e.ps1 -Local -Ui      # abre el runner UI de Playwright
#   .\scripts\dev\test-e2e.ps1 -Local -BaseUrl http://localhost:5173
#   .\scripts\dev\test-e2e.ps1 -VerboseLogs   # Docker: logs del arranque del stack en primer plano

[CmdletBinding(DefaultParameterSetName = 'Docker')]
param(
    [Parameter(ParameterSetName = 'Docker')]
    [switch]$SkipBuild,

    [Parameter(ParameterSetName = 'Docker')]
    [switch]$KeepStack,

    [Parameter(ParameterSetName = 'Docker')]
    [switch]$VerboseLogs,

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
            Write-MtlInfo 'npm install...'
            npm install
            if ($LASTEXITCODE -ne 0) { throw "npm install termino con codigo $LASTEXITCODE" }

            Write-MtlInfo 'npx playwright install --with-deps chromium...'
            npx playwright install --with-deps chromium
            if ($LASTEXITCODE -ne 0) { throw "playwright install termino con codigo $LASTEXITCODE" }
        }

        $env:BASE_URL = $BaseUrl
        $npmScript = if ($Ui) { 'e2e:ui' } else { 'e2e' }
        Write-MtlInfo "npm run ${npmScript}..."
        npm run $npmScript
        if ($LASTEXITCODE -ne 0) { throw "Playwright (npm run $npmScript) termino con codigo $LASTEXITCODE" }
    }

    Write-MtlOk 'E2E (local): pruebas completadas.'
    return
}

# --- Variante B: self-contained en Docker (CI/PR) ------------------------------
$servicesDir = Join-Path $repoRoot 'services'
$composeDir = Join-Path $repoRoot 'infra\compose'
$composeFile = 'docker-compose.e2e.yml'

$stackServices = @(
    'postgres',
    'mongo',
    'kafka',
    'redis',
    'keycloak',
    'catalog-service',
    'media-service',
    'ai-assistant-service',
    'api-gateway',
    'frontend'
)

Assert-CommandInPath -Name 'docker' -Hint 'Instala Docker Desktop y arrancalo.'

if (-not $SkipBuild) {
    Assert-CommandInPath -Name 'mvn' -Hint 'Instala Maven y anadelo al PATH.'
    Write-MtlInfo 'E2E (Docker): compilando jars de catalog/media/ai-assistant/api-gateway...'
    Invoke-MtlInDirectory -Path $servicesDir -Action {
        mvn -pl catalog-service,media-service,ai-assistant-service,api-gateway -am package -DskipTests
        if ($LASTEXITCODE -ne 0) { throw "mvn package termino con codigo $LASTEXITCODE" }
    }
}

Write-MtlInfo 'E2E (Docker): secuencia: stack en background, luego system-e2e-tests Maven, luego Playwright UI.'
Write-MtlInfo 'E2E (Docker): si Maven falla, Playwright no se ejecuta.'

Invoke-MtlInDirectory -Path $composeDir -Action {
    $e2eExit = 0
    try {
        $stackUpArgs = @('compose', '-f', $composeFile, 'up', '--build', '--wait') + $stackServices
        if ($VerboseLogs) {
            Write-MtlInfo 'E2E (Docker): arrancando stack (logs en primer plano)...'
            & docker @stackUpArgs
        }
        else {
            Write-MtlInfo 'E2E (Docker): arrancando stack en segundo plano (--wait healthchecks)...'
            $stackUpArgs = @('compose', '-f', $composeFile, 'up', '-d', '--build', '--wait') + $stackServices
            & docker @stackUpArgs
        }
        if ($LASTEXITCODE -ne 0) { throw "docker compose up del stack termino con codigo $LASTEXITCODE" }

        Write-MtlInfo 'E2E (Docker): paso 1/2 system-e2e-tests (Maven/JWT)...'
        & docker compose -f $composeFile run --rm system-e2e-tests
        if ($LASTEXITCODE -ne 0) { throw "system-e2e-tests termino con codigo $LASTEXITCODE" }

        Write-MtlInfo 'E2E (Docker): paso 2/2 Playwright UI...'
        & docker compose -f $composeFile run --rm playwright
        $e2eExit = $LASTEXITCODE
        if ($e2eExit -ne 0) { throw "Playwright termino con codigo $e2eExit" }
    }
    finally {
        if (-not $KeepStack) {
            Write-MtlInfo 'E2E (Docker): bajando stack (down -v, volumenes efimeros)...'
            docker compose -f $composeFile down -v
        }
        else {
            Write-MtlWarn "E2E (Docker): stack en marcha (-KeepStack). Para limpiar: docker compose -f $composeFile down -v"
        }
    }
}

Write-MtlOk 'E2E (Docker): pipeline completado. Informe HTML: e2e/playwright-report/index.html (en e2e/: npm run report)'
