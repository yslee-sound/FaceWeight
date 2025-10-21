<#!
.SYNOPSIS
  빌드 산출물(AAB, mapping.txt, SHA256 해시, CHANGELOG 스냅샷)을 release/ 디렉터리에 구조화해 아카이브합니다.

.DESCRIPTION
  릴리스 직후 수동 실행하여 재현 가능성과 검증 로그를 보관합니다.
  - 기본 입력: app/build/outputs/bundle/release/app-release.aab
  - 매핑: app/build/outputs/mapping/release/mapping.txt
  - SHA256 생성
  - CHANGELOG.md 해당 버전 섹션 추출

.PARAMETER VersionName
  릴리스 versionName (예: 1.0.0) - 폴더명과 CHANGELOG 추출에 사용.

.PARAMETER VersionCode
  릴리스 versionCode (예: 20251005). 폴더명 안에 포함.

.PARAMETER OutputRoot
  기본값: ./release

.EXAMPLE
  pwsh ./scripts/archive_release.ps1 -VersionName 1.0.0 -VersionCode 20251005

.NOTES
  - 실행 전 bundleRelease 완료 상태여야 함.
  - 이미 동일 경로 있으면 덮어쓰지 않고 중단.
#>
param(
  [Parameter(Mandatory=$true)][string]$VersionName,
  [Parameter(Mandatory=$true)][int]$VersionCode,
  [string]$OutputRoot = 'release'
)

$ErrorActionPreference = 'Stop'
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $repoRoot

$aab = 'app/build/outputs/bundle/release/app-release.aab'
$mapping = 'app/build/outputs/mapping/release/mapping.txt'
if(-not (Test-Path $aab)){ throw "AAB not found: $aab (먼저 gradlew.bat :app:bundleRelease 실행)" }
if(-not (Test-Path $mapping)){ Write-Warning "mapping.txt not found: $mapping (minifyEnabled true인지, R8 결과 생성됐는지 확인)" }

$folderName = "${VersionName}_$VersionCode"
$outDir = Join-Path $OutputRoot $folderName
if(Test-Path $outDir){ throw "Output directory already exists: $outDir" }
New-Item -ItemType Directory -Path $outDir | Out-Null

Write-Host "Archiving to $outDir" -ForegroundColor Cyan
Copy-Item $aab (Join-Path $outDir 'app-release.aab')
if(Test-Path $mapping){ Copy-Item $mapping (Join-Path $outDir 'mapping.txt') }

# SHA256
$hash = (Get-FileHash -Algorithm SHA256 $aab).Hash
$hashLine = "SHA256  app-release.aab  $hash"
$hashLine | Out-File (Join-Path $outDir 'SHA256SUMS.txt') -Encoding UTF8
Write-Host "SHA256: $hash" -ForegroundColor Green

# CHANGELOG 추출
$changelogPath = 'CHANGELOG.md'
if(Test-Path $changelogPath){
  $clContent = Get-Content $changelogPath -Raw
  $pattern = "## \[$([Regex]::Escape($VersionName))\][\s\S]*?(?=\n## \[|\n## 형식 가이드|\n$)"
  $match = [Regex]::Match($clContent, $pattern)
  if($match.Success){
    $match.Value | Out-File (Join-Path $outDir 'CHANGELOG_EXTRACT.md') -Encoding UTF8
  } else {
    Write-Warning "해당 버전 섹션을 CHANGELOG에서 찾을 수 없습니다: $VersionName"
  }
}

# 메타 정보
@(
  "versionName=$VersionName",
  "versionCode=$VersionCode",
  "createdUtc=$(Get-Date -AsUTC -Format o)",
  "gitCommit=$(git rev-parse --short HEAD 2>$null)"
) | Out-File (Join-Path $outDir 'meta.txt') -Encoding UTF8

Write-Host "Archive complete." -ForegroundColor Yellow
