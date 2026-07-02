# Rama desde main actualizado. Uso: .\scripts\dev\git-new-branch.ps1 -Prefix feature -Name mi-tarea [-Stash]

param(
    [Parameter(Mandatory)]
    [ValidateSet('feature', 'fix', 'chore')]
    [string]$Prefix,

    [Parameter(Mandatory)]
    [string]$Name,

    [switch]$Stash
)

. "$PSScriptRoot\_common.ps1"
Assert-CommandInPath -Name 'git'

$segment = ($Name.Trim() -replace '\s+', '-').ToLowerInvariant()
if ($segment -notmatch '^[a-z0-9]+(-[a-z0-9]+)*$') {
    throw "Nombre de rama no válido: '$segment' (minúsculas, números y guiones)."
}
$branch = "$Prefix/$segment"

function Invoke-Git {
    param([Parameter(Mandatory)][string[]]$GitArgs)
    & git @GitArgs
    if ($LASTEXITCODE -ne 0) {
        throw "git $($GitArgs -join ' ') falló"
    }
}

Invoke-MtlInDirectory (Get-MtlRepoRoot) {
    if (-not (Test-MtlGitWorkingTreeClean)) {
        if (-not $Stash) {
            Write-MtlError 'Hay cambios sin commit. Commit, -Stash o aborta (.cursor/commands/git-commit.md).'
            exit 1
        }
        Write-MtlWarn "Guardando cambios en stash antes de $branch"
        Invoke-Git @('stash', 'push', '-u', '-m', "WIP antes de $branch")
    }

    Invoke-Git @('checkout', 'main')
    Invoke-Git @('pull', 'origin', 'main')
    Invoke-Git @('checkout', '-b', $branch)

    Write-MtlOk "Rama creada: $branch"
    Write-Host 'Subir:  git push -u origin HEAD'
    if ($Stash) { Write-Host 'Stash:  git stash pop' }
}
