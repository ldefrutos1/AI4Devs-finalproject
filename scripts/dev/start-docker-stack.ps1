# Automatizacion local: build de imagenes + infraestructura + aplicacion en Docker.
#
# Uso (desde la raiz del repo):
#   .\scripts\dev\start-docker-stack.ps1              # build + infra + apps
#   .\scripts\dev\start-docker-stack.ps1 -SkipBuild   # usa imagenes ya construidas
#   .\scripts\dev\start-docker-stack.ps1 -InfraOnly   # solo dependencias
#   .\scripts\dev\start-docker-stack.ps1 -AppsOnly    # solo apps (infra ya arriba)
#   .\scripts\dev\start-docker-stack.ps1 -Down        # baja apps + infra
#   .\scripts\dev\start-docker-stack.ps1 -Down -KeepVolumes  # down sin borrar datos

[CmdletBinding()]
param(
    [string]$Tag = 'local',
    [switch]$SkipBuild,
    [switch]$InfraOnly,
    [switch]$AppsOnly,
    [switch]$Down,
    [switch]$KeepVolumes
)

. "$PSScriptRoot\_common.ps1"

$repoRoot = Get-MtlRepoRoot
$composeDir = Join-Path $repoRoot 'infra\compose'
$infraFile = 'docker-compose.yml'
$appsFile = 'docker-compose.apps.yml'

Assert-CommandInPath -Name 'docker' -Hint 'Instala Docker Desktop y arrancalo.'

$env:MTL_IMAGE_TAG = $Tag

if ($Down) {
    Write-MtlInfo 'Bajando stack Docker (infra + aplicacion)…'
    Invoke-MtlInDirectory -Path $composeDir -Action {
        $downArgs = @('compose', '-f', $infraFile, '-f', $appsFile, 'down')
        if (-not $KeepVolumes) {
            $downArgs += '-v'
        }
        docker @downArgs
        if ($LASTEXITCODE -ne 0) { throw "docker compose down termino con codigo $LASTEXITCODE" }
    }
    Write-MtlOk 'Stack Docker detenido.'
    return
}

if (-not $SkipBuild -and -not $AppsOnly) {
    $buildScript = Join-Path $PSScriptRoot 'build-images.ps1'
    & $buildScript -Tag $Tag
    if ($LASTEXITCODE -ne 0) { throw 'build-images.ps1 fallo' }
}

Invoke-MtlInDirectory -Path $composeDir -Action {
    if (-not $AppsOnly) {
        Write-MtlInfo 'Levantando infraestructura (docker-compose.yml)…'
        docker compose -f $infraFile up -d
        if ($LASTEXITCODE -ne 0) { throw "docker compose up (infra) termino con codigo $LASTEXITCODE" }
    }

    if (-not $InfraOnly) {
        if ($SkipBuild -and -not $AppsOnly) {
            Write-MtlWarn 'SkipBuild: se asume que las imagenes mtl/* ya existen en Docker local.'
        }
        Write-MtlInfo 'Levantando aplicacion (docker-compose.apps.yml)…'
        $appsUpArgs = @('compose', '-f', $infraFile, '-f', $appsFile, 'up', '-d')
        if (-not $SkipBuild -or $AppsOnly) {
            # Tras build-images (o AppsOnly tras build manual), usar la imagen mtl/* recien etiquetada.
            $appsUpArgs += '--force-recreate'
        }
        docker @appsUpArgs
        if ($LASTEXITCODE -ne 0) { throw "docker compose up (apps) termino con codigo $LASTEXITCODE" }
    }
}

if (-not $InfraOnly) {
    Write-MtlOk @'
Stack listo.
  SPA:      http://localhost:8088  (FRONTEND_PORT en infra/compose/.env)
  API GW:   http://localhost:8080
  Keycloak: http://localhost:8180
'@
}
else {
    Write-MtlOk 'Infraestructura lista (sin servicios de aplicacion).'
}
