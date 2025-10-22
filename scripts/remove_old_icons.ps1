$ErrorActionPreference = "Stop"

# 프로젝트 루트: scripts 폴더의 한 단계 상위(= 프로젝트 루트)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Split-Path -Parent $scriptDir
Write-Host "Project root: $root" -ForegroundColor Cyan

$targets = @(
  "app/src/main/res/drawable-anydpi-v26/ic_bottle_foreground.xml",
  "app/src/main/res/drawable-anydpi-v26/ic_launcher_foreground.xml",
  "app/src/main/res/drawable-anydpi-v26/ic_launcher_monochrome.xml",
  "app/src/main/res/mipmap-anydpi/app_icon.xml",
  "app/src/main/res/mipmap-anydpi-v26/app_icon.xml",
  "app/src/main/res/drawable/ic_legacy_foreground_inset.xml",
  "app/src/main/res/drawable-anydpi-v26/splash_app_icon_inset.xml",
  "app/src/main/res/drawable/splash_app_icon.xml"
)

$removed = @()
$missing = @()
foreach ($rel in $targets) {
  $p = Join-Path $root $rel
  if (Test-Path $p) {
    Remove-Item -LiteralPath $p -Force
    $removed += $rel
  } else {
    $missing += $rel
  }
}

Write-Host "Removed files:" -ForegroundColor Green
$removed | ForEach-Object { Write-Host " - $_" }

if ($missing.Count -gt 0) {
  Write-Host "Not found (already removed):" -ForegroundColor Yellow
  $missing | ForEach-Object { Write-Host " - $_" }
}

Write-Host "Done."
