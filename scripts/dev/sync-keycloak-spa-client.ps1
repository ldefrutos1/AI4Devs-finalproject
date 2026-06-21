# Sincroniza redirect URIs / web origins del cliente OIDC mtl-spa en Keycloak en marcha
# con infra/compose/init/keycloak/mtl-realm.json (util cuando el realm ya existia y --import-realm no reaplica).
#
# Uso (desde la raiz del repo, Keycloak levantado en Compose):
#   .\scripts\dev\sync-keycloak-spa-client.ps1
#   .\scripts\dev\sync-keycloak-spa-client.ps1 -KeycloakPort 8180

[CmdletBinding()]
param(
    [string]$KeycloakPort = '',
    [string]$AdminUser = '',
    [string]$AdminPassword = ''
)

. "$PSScriptRoot\_common.ps1"

$repoRoot = Get-MtlRepoRoot
$composeDir = Join-Path $repoRoot 'infra\compose'
$realmJsonPath = Join-Path $composeDir 'init\keycloak\mtl-realm.json'
$envFile = Join-Path $composeDir '.env'

if (-not (Test-Path -LiteralPath $realmJsonPath)) {
    throw "No se encontro $realmJsonPath"
}

if ([string]::IsNullOrWhiteSpace($KeycloakPort) -and (Test-Path -LiteralPath $envFile)) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^\s*KEYCLOAK_PORT\s*=\s*(\d+)\s*$') {
            $KeycloakPort = $Matches[1]
            break
        }
    }
}
if ([string]::IsNullOrWhiteSpace($KeycloakPort)) {
    $KeycloakPort = '8180'
}

if ([string]::IsNullOrWhiteSpace($AdminUser) -and (Test-Path -LiteralPath $envFile)) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^\s*KEYCLOAK_ADMIN\s*=\s*(\S+)\s*$') {
            $AdminUser = $Matches[1]
            break
        }
    }
}
if ([string]::IsNullOrWhiteSpace($AdminUser)) {
    $AdminUser = 'admin'
}

if ([string]::IsNullOrWhiteSpace($AdminPassword) -and (Test-Path -LiteralPath $envFile)) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^\s*KEYCLOAK_ADMIN_PASSWORD\s*=\s*(\S+)\s*$') {
            $AdminPassword = $Matches[1]
            break
        }
    }
}
if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
    $AdminPassword = 'admin_dev_password'
}

$realmDoc = Get-Content -LiteralPath $realmJsonPath -Raw | ConvertFrom-Json
$spaClient = $realmDoc.clients | Where-Object { $_.clientId -eq 'mtl-spa' } | Select-Object -First 1
if (-not $spaClient) {
    throw 'No se encontro clientId mtl-spa en mtl-realm.json'
}

$keycloakBase = "http://localhost:$KeycloakPort"
Write-MtlInfo "Keycloak: $keycloakBase - sincronizando cliente mtl-spa..."

$tokenBody = @{
    client_id     = 'admin-cli'
    username      = $AdminUser
    password      = $AdminPassword
    grant_type    = 'password'
}
try {
    $tokenResponse = Invoke-RestMethod `
        -Uri "$keycloakBase/realms/master/protocol/openid-connect/token" `
        -Method POST `
        -ContentType 'application/x-www-form-urlencoded' `
        -Body $tokenBody
} catch {
    throw "No se pudo autenticar en Keycloak ($keycloakBase). Comprueba que este levantado y las credenciales admin."
}

$adminToken = $tokenResponse.access_token
$authHeader = @{ Authorization = "Bearer $adminToken" }

$clients = Invoke-RestMethod `
    -Uri "$keycloakBase/admin/realms/mtl/clients?clientId=mtl-spa" `
    -Headers $authHeader
if (-not $clients -or $clients.Count -eq 0) {
    throw 'Cliente mtl-spa no encontrado en realm mtl. Keycloak importo el realm?'
}

$clientUuid = $clients[0].id
$clientResponse = Invoke-WebRequest `
    -Uri "$keycloakBase/admin/realms/mtl/clients/$clientUuid" `
    -Headers $authHeader `
    -UseBasicParsing
$clientJson = $clientResponse.Content | ConvertFrom-Json

$clientJson.redirectUris = @($spaClient.redirectUris)
$clientJson.webOrigins = @($spaClient.webOrigins)
if ($null -eq $clientJson.attributes) {
    $clientJson.attributes = [ordered]@{}
}
$postLogoutProp = $spaClient.attributes.PSObject.Properties['post.logout.redirect.uris']
if ($null -ne $postLogoutProp) {
    $clientJson.attributes.'post.logout.redirect.uris' = $postLogoutProp.Value
}

$jsonBody = $clientJson | ConvertTo-Json -Depth 12 -Compress
Invoke-RestMethod `
    -Uri "$keycloakBase/admin/realms/mtl/clients/$clientUuid" `
    -Method PUT `
    -Headers @{ Authorization = "Bearer $adminToken"; 'Content-Type' = 'application/json' } `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($jsonBody)) | Out-Null

Write-MtlOk 'Cliente mtl-spa actualizado.'
Write-MtlInfo 'Valid redirect URIs:'
foreach ($uri in $spaClient.redirectUris) {
    Write-Host "  $uri"
}
Write-MtlInfo 'Web origins:'
foreach ($origin in $spaClient.webOrigins) {
    Write-Host "  $origin"
}
Write-MtlOk 'Prueba login en http://localhost:8088'
