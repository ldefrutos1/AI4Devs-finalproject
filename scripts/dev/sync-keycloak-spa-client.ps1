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

$composeDir = Join-Path (Get-MtlRepoRoot) 'infra\compose'
$realmJsonPath = Join-Path $composeDir 'init\keycloak\mtl-realm.json'
$envFile = Join-Path $composeDir '.env'

if (-not (Test-Path -LiteralPath $realmJsonPath)) {
    throw "No se encontro $realmJsonPath"
}

$envVars = @{}
if (Test-Path -LiteralPath $envFile) {
    Get-Content -LiteralPath $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#=\s]+)\s*=\s*(\S+)\s*$') {
            $envVars[$Matches[1]] = $Matches[2]
        }
    }
}

function Resolve-Param {
    param(
        [string]$Value,
        [string]$EnvKey,
        [string]$Default
    )
    if (-not [string]::IsNullOrWhiteSpace($Value)) { return $Value }
    if ($envVars.ContainsKey($EnvKey)) { return $envVars[$EnvKey] }
    return $Default
}

$KeycloakPort = Resolve-Param $KeycloakPort 'KEYCLOAK_PORT' '8180'
$AdminUser = Resolve-Param $AdminUser 'KEYCLOAK_ADMIN' 'admin'
$AdminPassword = Resolve-Param $AdminPassword 'KEYCLOAK_ADMIN_PASSWORD' 'admin_dev_password'

$spaClient = (Get-Content -LiteralPath $realmJsonPath -Raw | ConvertFrom-Json).clients |
    Where-Object clientId -eq 'mtl-spa' |
    Select-Object -First 1
if (-not $spaClient) {
    throw 'No se encontro clientId mtl-spa en mtl-realm.json'
}

$keycloakBase = "http://localhost:$KeycloakPort"
Write-MtlInfo "Keycloak: $keycloakBase - sincronizando cliente mtl-spa..."

try {
    $adminToken = (Invoke-RestMethod `
        -Uri "$keycloakBase/realms/master/protocol/openid-connect/token" `
        -Method POST `
        -ContentType 'application/x-www-form-urlencoded' `
        -Body @{
            client_id  = 'admin-cli'
            username   = $AdminUser
            password   = $AdminPassword
            grant_type = 'password'
        }
    ).access_token
} catch {
    throw "No se pudo autenticar en Keycloak ($keycloakBase). Comprueba que este levantado y las credenciales admin."
}

$headers = @{ Authorization = "Bearer $adminToken" }
$clients = Invoke-RestMethod `
    -Uri "$keycloakBase/admin/realms/mtl/clients?clientId=mtl-spa" `
    -Headers $headers
if (-not $clients -or $clients.Count -eq 0) {
    throw 'Cliente mtl-spa no encontrado en realm mtl. Keycloak importo el realm?'
}

$clientUuid = $clients[0].id
$clientJson = Invoke-RestMethod `
    -Uri "$keycloakBase/admin/realms/mtl/clients/$clientUuid" `
    -Headers $headers

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
Write-MtlInfo "Valid redirect URIs:`n  $($spaClient.redirectUris -join "`n  ")"
Write-MtlInfo "Web origins:`n  $($spaClient.webOrigins -join "`n  ")"
Write-MtlOk 'Prueba login en http://localhost:8088'
