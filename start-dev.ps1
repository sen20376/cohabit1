[CmdletBinding()]
param(
    [switch]$ForceRestart
)

$ErrorActionPreference = 'Continue'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendRoot = Join-Path $root 'cohabit-backend'
$frontendRoot = Join-Path $root 'cohabit-frontend'

function Assert-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Befehl '$Name' wurde nicht gefunden. Bitte installiere ihn und starte erneut."
    }
}

function Test-PortInUse {
    param([int]$Port)
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $listener
}

function Stop-PortListeners {
    param([int]$Port)

    $listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $listeners) { return @() }

    $stopped = @()
    $pids = $listeners | Select-Object -ExpandProperty OwningProcess -Unique

    foreach ($procId in $pids) {
        try {
            $proc = Get-Process -Id $procId -ErrorAction Stop
            # /T kills the entire process tree (wichtig fuer node/npm child processes)
            taskkill /T /F /PID $procId 2>$null | Out-Null
            $stopped += ("{0} (PID {1})" -f $proc.ProcessName, $procId)
        } catch {
            Write-Host "[WARN] Konnte Prozess auf Port $Port nicht beenden (PID $procId): $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    return $stopped
}

function Start-ServiceTerminal {
    param(
        [string]$Title,
        [string]$WorkingDirectory,
        [string]$Command
    )

    $fullCommand = "`$host.UI.RawUI.WindowTitle = '$Title'; Set-Location '$WorkingDirectory'; $Command"
    Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $fullCommand) | Out-Null
}

function Wait-ForPort {
    param(
        [string]$Name,
        [int]$Port,
        [int]$TimeoutSeconds = 60
    )

    Write-Host "  Warte auf $Name (Port $Port)..." -NoNewline -ForegroundColor DarkGray
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (Test-PortInUse -Port $Port) {
            Write-Host " bereit nach ${elapsed}s" -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host '.' -NoNewline -ForegroundColor DarkGray
    }
    Write-Host " Timeout nach ${TimeoutSeconds}s" -ForegroundColor Red
    return $false
}

# Voraussetzungen pruefen
$prereqOk = $true
try { Assert-Command -Name 'mvn' } catch { Write-Host "[FEHLER] $_" -ForegroundColor Red; $prereqOk = $false }
try { Assert-Command -Name 'npm' } catch { Write-Host "[FEHLER] $_" -ForegroundColor Red; $prereqOk = $false }
if (-not $prereqOk) { exit 1 }

if (-not (Test-Path $backendRoot)) { Write-Host "[FEHLER] Backend-Ordner nicht gefunden: $backendRoot" -ForegroundColor Red; exit 1 }
if (-not (Test-Path $frontendRoot)) { Write-Host "[FEHLER] Frontend-Ordner nicht gefunden: $frontendRoot" -ForegroundColor Red; exit 1 }

$services = @(
    @{ Name = 'user';     Port = 8080; Dir = Join-Path $backendRoot 'user';      Cmd = 'mvn quarkus:dev -Ddebug=false' },
    @{ Name = 'catalog';  Port = 8081; Dir = Join-Path $backendRoot 'catalog';   Cmd = 'mvn quarkus:dev -Ddebug=false' },
    @{ Name = 'rating';   Port = 8082; Dir = Join-Path $backendRoot 'rating\be'; Cmd = 'mvn quarkus:dev -Ddebug=false' },
    @{ Name = 'frontend'; Port = 5173; Dir = $frontendRoot;                      Cmd = 'npm run dev -- --host' }
)

$started = @()

foreach ($svc in $services) {
    if (-not (Test-Path $svc.Dir)) {
        Write-Host "[FEHLER] Ordner fehlt: $($svc.Dir)" -ForegroundColor Red
        continue
    }

    if (Test-PortInUse -Port $svc.Port) {
        if ($ForceRestart) {
            Write-Host "[RESTART] Port $($svc.Port) belegt. Beende Prozesse..." -ForegroundColor Yellow
            $stopped = Stop-PortListeners -Port $svc.Port
            if ($stopped.Count -gt 0) {
                Write-Host ("[INFO] Beendet: {0}" -f ($stopped -join ', ')) -ForegroundColor DarkYellow
            }
            Start-Sleep -Milliseconds 800
            if (Test-PortInUse -Port $svc.Port) {
                Write-Host "[SKIP] $($svc.Name) nicht gestartet, Port $($svc.Port) bleibt belegt." -ForegroundColor Red
                continue
            }
        } else {
            Write-Host "[SKIP] $($svc.Name) nicht gestartet, Port $($svc.Port) ist bereits belegt." -ForegroundColor Yellow
            continue
        }
    }

    Write-Host "[START] $($svc.Name) auf Port $($svc.Port)..." -ForegroundColor Cyan
    Start-ServiceTerminal -Title "Cohabit - $($svc.Name)" -WorkingDirectory $svc.Dir -Command $svc.Cmd
    $started += $svc
}

if ($started.Count -eq 0) {
    Write-Host "`nKeine neuen Services gestartet." -ForegroundColor Yellow
    exit 0
}

Write-Host "`nWarte auf Services..." -ForegroundColor Cyan
foreach ($svc in $started) {
    Wait-ForPort -Name $svc.Name -Port $svc.Port
}

Write-Host ''
Write-Host 'Start abgeschlossen.' -ForegroundColor Green
Write-Host 'Frontend:   http://localhost:5173' -ForegroundColor Green
Write-Host 'User API:   http://localhost:8080' -ForegroundColor Green
Write-Host 'Catalog API: http://localhost:8081' -ForegroundColor Green
Write-Host 'Rating API:  http://localhost:8082' -ForegroundColor Green
Write-Host 'Tipp: Fuer Neustart trotz belegter Ports -> .\start-dev.ps1 -ForceRestart' -ForegroundColor DarkGray
