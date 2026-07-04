# Lista puertos MTL en escucha (microservicios, Vite, Compose).
# Uso: .\scripts\dev\check-ports.ps1
#      .\scripts\dev\check-ports.ps1 -All   # también muestra puertos libres
#
# Puertos habituales: alinear con services/README.md e infra/compose/README.md.

[CmdletBinding()]
param(
    [switch]$All
)

. "$PSScriptRoot\_common.ps1"

$mtlPortDefinitions = @(
    @{ Port = 8080; Label = 'api-gateway (Spring Boot dev)' }
    @{ Port = 8081; Label = 'catalog-service' }
    @{ Port = 8082; Label = 'media-service' }
    @{ Port = 8083; Label = 'notification-service' }
    @{ Port = 8084; Label = 'ai-assistant-service' }
    @{ Port = 5173; Label = 'frontend Vite dev' }
    @{ Port = 8088; Label = 'frontend Docker (Nginx)' }
    @{ Port = 5433; Label = 'PostgreSQL (host, Compose)' }
    @{ Port = 27017; Label = 'MongoDB' }
    @{ Port = 6379; Label = 'Redis' }
    @{ Port = 9000; Label = 'MinIO API' }
    @{ Port = 9001; Label = 'MinIO consola' }
    @{ Port = 9094; Label = 'Kafka (host)' }
    @{ Port = 8180; Label = 'Keycloak' }
    @{ Port = 1025; Label = 'Mailpit SMTP' }
    @{ Port = 8025; Label = 'Mailpit UI' }
    @{ Port = 9090; Label = 'Prometheus' }
    @{ Port = 3000; Label = 'Grafana' }
)

$portNumbers = @($mtlPortDefinitions | ForEach-Object { $_.Port })
$portLabels = @{}
foreach ($def in $mtlPortDefinitions) {
    $portLabels[$def.Port] = $def.Label
}

$portSet = [System.Collections.Generic.HashSet[int]]::new()
foreach ($p in $portNumbers) {
    [void]$portSet.Add($p)
}

Write-MtlInfo 'Comprobando puertos en escucha (desarrollo local MTL)…'

$byPort = @{}

function Get-PortLabel {
    param([int]$Port)
    if ($portLabels.ContainsKey($Port)) { return $portLabels[$Port] }
    return 'puerto MTL (sin etiqueta)'
}

function Add-ListeningPort {
    param([int]$Port, [int]$ProcessId)
    if (-not $portSet.Contains($Port)) { return }
    if ($byPort.ContainsKey($Port)) { return }
    $procName = '?'
    if ($ProcessId -gt 0) {
        $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
        if ($proc) { $procName = $proc.ProcessName }
    }
    $byPort[$Port] = [pscustomobject]@{
        Port      = $Port
        Label     = Get-PortLabel -Port $Port
        Process   = $procName
        ProcessId = $ProcessId
    }
}

try {
    foreach ($conn in Get-NetTCPConnection -State Listen -ErrorAction Stop) {
        Add-ListeningPort -Port $conn.LocalPort -ProcessId $conn.OwningProcess
    }
}
catch {
    Write-MtlWarn "Get-NetTCPConnection: $($_.Exception.Message). Probando netstat…"
    $lines = netstat -ano | Select-String 'LISTENING'
    foreach ($line in $lines) {
        if ($line.Line -notmatch ':(\d+)\s+\S+\s+LISTENING\s+(\d+)\s*$') {
            continue
        }
        Add-ListeningPort -Port ([int]$Matches[1]) -ProcessId ([int]$Matches[2])
    }
}

$listening = @($byPort.Values | Sort-Object Port)

if ($listening.Count -eq 0) {
    Write-MtlOk 'Ningún puerto MTL habitual está en escucha.'
}
else {
    Write-Host ''
    Write-Host 'En escucha:' -ForegroundColor Yellow
    $listening | Format-Table -AutoSize Port, Label, Process, ProcessId
}

if ($All) {
    $free = @($portNumbers | Where-Object { -not $byPort.ContainsKey($_) } | ForEach-Object {
        [pscustomobject]@{
            Port  = $_
            Label = Get-PortLabel -Port $_
            State = 'libre'
        }
    })
    if ($free.Count -gt 0) {
        Write-Host ''
        Write-Host 'Libres (habitual MTL):' -ForegroundColor DarkGray
        $free | Format-Table -AutoSize Port, Label, State
    }
}

Write-MtlInfo 'Referencia: services/README.md, infra/compose/README.md'
