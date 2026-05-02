<#
Script: run_installer_and_check.ps1
¿Qué hace?
- Ejecuta el instalador EXE (por defecto: ../dist/FerreteriaLaPromoHC-1.0.0.exe)
- Espera a que termine
- Verifica rutas de instalación comunes (per-user y Program Files)
- Verifica existencia de accesos directos en Escritorio y Menú Inicio
- Verifica entrada en registro (uninstall key)
- Si no encuentra atajo en Escritorio, puede llamar a create_shortcut.ps1 para crearlo

Uso:
  PowerShell (no administrador):
    cd C:\Users\murde\IdeaProjects\FerreteriaLaPromoHC\scripts
    .\run_installer_and_check.ps1

  Para especificar otro instalador:
    .\run_installer_and_check.ps1 -InstallerPath "C:\ruta\a\instalador.exe"
#>

param(
    [string]$InstallerPath = "..\dist\FerreteriaLaPromoHC-1.0.0.exe",
    [switch]$CreateShortcutIfMissing
)

function Write-Info($m){ Write-Host "[INFO] $m" -ForegroundColor Cyan }
function Write-Ok($m){ Write-Host "[OK]   $m" -ForegroundColor Green }
function Write-Warn($m){ Write-Host "[WARN] $m" -ForegroundColor Yellow }
function Write-Err($m){ Write-Host "[ERR]  $m" -ForegroundColor Red }

# Normalize path
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$installerFull = (Resolve-Path (Join-Path $scriptDir $InstallerPath) -ErrorAction SilentlyContinue)
if (-not $installerFull) {
    Write-Err "Instalador no encontrado en: $InstallerPath (desde: $scriptDir)"
    exit 2
}
$installerFull = $installerFull.Path
Write-Info "Instalador: $installerFull"

# Ejecutar instalador
Write-Info "Ejecutando instalador (se abrirá la UI). Espera a que termine..."
try {
    $p = Start-Process -FilePath $installerFull -Wait -PassThru -ErrorAction Stop
    Write-Ok "Instalador finalizó. ExitCode: $($p.ExitCode)"
} catch {
    Write-Err "Error al ejecutar el instalador: $($_.Exception.Message)"
}

# Rutas a comprobar
$perUserPath = Join-Path $env:LOCALAPPDATA "Programs\FerreteriaLaPromoHC\FerreteriaLaPromoHC.exe"
$progFilesPath = "C:\Program Files\FerreteriaLaPromoHC\FerreteriaLaPromoHC.exe"
$desktopShortcut = Join-Path $env:USERPROFILE "Desktop\FerreteriaLaPromoHC.lnk"
$startMenuShortcut1 = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs\FerreteriaLaPromoHC.lnk"
$startMenuDir = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs\FerreteriaLaPromoHC"

$found = @()

if (Test-Path $perUserPath) { $found += $perUserPath; Write-Ok "Instalación encontrada (per-user): $perUserPath" }
if (Test-Path $progFilesPath) { $found += $progFilesPath; Write-Ok "Instalación encontrada (Program Files): $progFilesPath" }

if (Test-Path $desktopShortcut) { Write-Ok "Acceso directo en Escritorio: $desktopShortcut" } else { Write-Warn "No se encontró acceso directo en Escritorio: $desktopShortcut" }

if (Test-Path $startMenuShortcut1) { Write-Ok "Acceso directo en Menú Inicio: $startMenuShortcut1" } elseif (Test-Path $startMenuDir) { Write-Ok "Carpeta en Menú Inicio encontrada: $startMenuDir" } else { Write-Warn "No se encontró entrada en Menú Inicio" }

# Buscar en registro uninstall (HKCU y HKLM)
function Search-UninstallReg($hive) {
    # Construir la ruta del registro de forma segura: ej. HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall
    $base = Join-Path ("${hive}:") "Software\Microsoft\Windows\CurrentVersion\Uninstall"
    if (-not (Test-Path $base)) { return $null }
    $keys = Get-ChildItem $base -ErrorAction SilentlyContinue
    foreach ($k in $keys) {
        try {
            $displayName = (Get-ItemProperty $k.PSPath -ErrorAction SilentlyContinue).DisplayName
            if ($displayName -and $displayName -match "Ferreteria|FerreteriaLaPromo") {
                return @{ Path = $k.PSPath; Name = $displayName }
            }
        } catch { }
    }
    return $null
}

$regCU = Search-UninstallReg 'HKCU'
$regLM = Search-UninstallReg 'HKLM'
if ($regCU) { Write-Ok "Entrada uninstall encontrada en HKCU: $($regCU.Name)" } else { Write-Warn "No se encontró entry uninstall en HKCU" }
if ($regLM) { Write-Ok "Entrada uninstall encontrada en HKLM: $($regLM.Name)" } else { Write-Warn "No se encontró entry uninstall en HKLM" }

# Si no hay accesos directos y se pide crear, ejecutar create_shortcut.ps1
if ($CreateShortcutIfMissing -and -not (Test-Path $desktopShortcut)) {
    # determinar target (PowerShell no tiene operador ternario como en C#)
    $target = $null
    if (Test-Path $perUserPath) { $target = $perUserPath }
    elseif (Test-Path $progFilesPath) { $target = $progFilesPath }
    if ($null -eq $target) {
        Write-Err "No se encontró el ejecutable para crear shortcut. Busca manualmente la ruta de instalación y vuelve a ejecutar con -CreateShortcutIfMissing"
        exit 3
    }
    Write-Info "Creando shortcut en Escritorio apuntando a: $target"
    & "$scriptDir\create_shortcut.ps1" -TargetPath $target -ShortcutName "FerreteriaLaPromoHC" -WorkingDirectory (Split-Path $target) | Out-Null
    if (Test-Path $desktopShortcut) { Write-Ok "Shortcut creado: $desktopShortcut" } else { Write-Err "No se pudo crear shortcut" }
}

Write-Host "\nResumen:" -ForegroundColor Cyan
if ($found.Count -gt 0) { Write-Host "Instalación detectada en:"; $found | ForEach-Object { Write-Host " - $_" } } else { Write-Host "No se detectó carpeta de instalación común." }

Write-Host "Comprobaciones finalizadas." -ForegroundColor Green

