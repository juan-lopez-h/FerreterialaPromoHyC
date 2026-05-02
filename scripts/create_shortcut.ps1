<#
Script: create_shortcut.ps1
Crea un acceso directo (.lnk) en el Escritorio o en la ruta indicada.
Uso:
  .\create_shortcut.ps1 -TargetPath "C:\Program Files\FerreteriaLaPromoHC\FerreteriaLaPromoHC.exe" -ShortcutName "FerreteriaLaPromoHC"
#>
param(
    [Parameter(Mandatory=$true)][string]$TargetPath,
    [string]$ShortcutName = "FerreteriaLaPromoHC",
    [string]$ShortcutPath = "$env:USERPROFILE\Desktop",
    [string]$WorkingDirectory = $null
)

if (-not (Test-Path $TargetPath)) {
    Write-Host "[ERR] TargetPath no existe: $TargetPath" -ForegroundColor Red
    exit 1
}

if (-not $WorkingDirectory) { $WorkingDirectory = Split-Path $TargetPath }

$lnk = Join-Path $ShortcutPath ("$ShortcutName.lnk")
$WshShell = New-Object -ComObject WScript.Shell
$shortcut = $WshShell.CreateShortcut($lnk)
$shortcut.TargetPath = $TargetPath
$shortcut.WorkingDirectory = $WorkingDirectory
$shortcut.IconLocation = $TargetPath
$shortcut.Save()

Write-Host "[OK] Shortcut creado: $lnk" -ForegroundColor Green

