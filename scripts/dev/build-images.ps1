# Construye las imagenes locales mtl/* (frontend + 5 microservicios).
# Estrategia Opcion A: Maven genera los jars; los Dockerfiles solo empaquetan JRE 21.
#
# Uso (desde la raiz del repo):
#   .\scripts\dev\build-images.ps1
#   .\scripts\dev\build-images.ps1 -Tag dev
#   .\scripts\dev\build-images.ps1 -SkipMaven          # jars ya compilados
#   .\scripts\dev\build-images.ps1 -ServicesOnly       # solo backend (sin frontend)

[CmdletBinding()]
param(
    [string]$Tag = 'local',
    [switch]$SkipMaven,
    [switch]$ServicesOnly
)

. "$PSScriptRoot\_common.ps1"

$repoRoot = Get-MtlRepoRoot
$servicesDir = Join-Path $repoRoot 'services'
$frontendDir = Join-Path $repoRoot 'frontend'
$composeDir = Join-Path $repoRoot 'infra\compose'

Assert-CommandInPath -Name 'docker' -Hint 'Instala Docker Desktop y arrancalo.'

$keycloakPort = '8180'
$envFile = Join-Path $composeDir '.env'
if (Test-Path -LiteralPath $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^\s*KEYCLOAK_PORT\s*=\s*(\d+)\s*$') {
            $keycloakPort = $Matches[1]
            break
        }
    }
}

$backendModules = @(
    'api-gateway',
    'catalog-service',
    'media-service',
    'notification-service',
    'ai-assistant-service'
)

if (-not $SkipMaven) {
    Assert-CommandInPath -Name 'mvn' -Hint 'Instala Maven y anadelo al PATH.'
    $moduleList = ($backendModules -join ',')
    Write-MtlInfo "Compilando jars ($moduleList)…"
    Invoke-MtlInDirectory -Path $servicesDir -Action {
        mvn -pl $moduleList -am package -DskipTests
        if ($LASTEXITCODE -ne 0) { throw "mvn package termino con codigo $LASTEXITCODE" }
    }
}

function Invoke-MtlDockerBuild {
    param(
        [Parameter(Mandatory)][string]$Context,
        [Parameter(Mandatory)][string]$Dockerfile,
        [Parameter(Mandatory)][string]$ImageName,
        [hashtable]$BuildArgs = @{}
    )
    $args = @('build', '-t', $ImageName, '-f', $Dockerfile)
    foreach ($key in $BuildArgs.Keys) {
        $args += @('--build-arg', "${key}=$($BuildArgs[$key])")
    }
    $args += $Context
    Write-MtlInfo "docker $($args -join ' ')"
    & docker @args
    if ($LASTEXITCODE -ne 0) { throw "docker build fallo para $ImageName (codigo $LASTEXITCODE)" }
}

foreach ($module in $backendModules) {
    $context = Join-Path $servicesDir $module
    $dockerfile = Join-Path $context 'Dockerfile'
    if (-not (Test-Path -LiteralPath $dockerfile)) {
        throw "Falta Dockerfile en $context"
    }
    Invoke-MtlDockerBuild `
        -Context $context `
        -Dockerfile $dockerfile `
        -ImageName "mtl/${module}:$Tag"
}

if (-not $ServicesOnly) {
    $viteIssuer = "http://localhost:${keycloakPort}/realms/mtl"
    Invoke-MtlDockerBuild `
        -Context $frontendDir `
        -Dockerfile (Join-Path $frontendDir 'Dockerfile') `
        -ImageName "mtl/frontend:$Tag" `
        -BuildArgs @{
            VITE_OIDC_ISSUER       = $viteIssuer
            VITE_OIDC_CLIENT_ID    = 'mtl-spa'
            VITE_OIDC_SCOPE        = 'openid profile email'
            VITE_GATEWAY_BASE_URL  = ''
        }
}

Write-MtlOk "Imagenes locales listas (tag: $Tag). Ejemplo: mtl/frontend:$Tag"
Write-MtlInfo 'Si ya hay contenedores en marcha, recrealos para cargar los jars nuevos:'
Write-MtlInfo '  cd infra\compose'
Write-MtlInfo '  docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --force-recreate'
Write-MtlInfo 'O bien: .\scripts\dev\start-docker-stack.ps1 -SkipBuild -AppsOnly'
