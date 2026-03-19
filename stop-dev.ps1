[CmdletBinding()]
param()

$ErrorActionPreference = 'Continue'

$servicePorts = @(
    @{ Name = 'user';     Port = 8080 },
    @{ Name = 'catalog';  Port = 8081 },
    @{ Name = 'rating';   Port = 8082 },
    @{ Name = 'frontend'; Port = 5173 }
)

function Stop-PortListeners {
    param(
        [string]$ServiceName,
        [int]$Port
    )

    $listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $listeners) {
        Write-Host "[SKIP] ${ServiceName}: Port ${Port} ist nicht belegt." -ForegroundColor Yellow
        return
    }

    $pids = $listeners | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $pids) {
        try {
            $proc = Get-Process -Id $procId -ErrorAction Stop
            # /T killt den gesamten Prozessbaum (wichtig fuer node/npm child processes)
            taskkill /T /F /PID $procId 2>$null | Out-Null
            Write-Host "[STOP] ${ServiceName}: $($proc.ProcessName) (PID $procId) und Kindprozesse beendet." -ForegroundColor Cyan
        }
        catch {
            Write-Host "[WARN] ${ServiceName}: Prozess auf Port $Port (PID $procId) konnte nicht beendet werden: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

foreach ($svc in $servicePorts) {
    Stop-PortListeners -ServiceName $svc.Name -Port $svc.Port
}

Write-Host ''
Write-Host 'Stop abgeschlossen.' -ForegroundColor Green
