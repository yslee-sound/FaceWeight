<#!
.SYNOPSIS
  app/build.gradle.kts 내 computedVersionCode / computedVersionName 기본값을 수정합니다.

.DESCRIPTION
  프로젝트는 환경변수 VERSION_CODE / VERSION_NAME 주입을 우선하지만, 로컬 수동 릴리스 편의를 위해
  build.gradle.kts 에 선언된 fallback 기본값을 직접 치환합니다.

.PARAMETER VersionName
  새 semantic version (예: 1.0.1)

.PARAMETER VersionCode
  새 정수 versionCode (예: 20251006). 지정하지 않으면 오늘 날짜(YYYYMMdd) 사용.

.EXAMPLE
  pwsh ./scripts/bump_version.ps1 -VersionName 1.0.1

.EXAMPLE
  pwsh ./scripts/bump_version.ps1 -VersionName 1.1.0 -VersionCode 20251101

.NOTES
  - 백업 파일을 build.gradle.kts.bak 로 생성.
  - 단순 정규식 치환이므로 선언 패턴이 크게 변하면 스크립트 수정 필요.
#>
param(
  [Parameter(Mandatory=$true)][string]$VersionName,
  [int]$VersionCode
)

$gradleFile = Join-Path $PSScriptRoot '..' 'app' 'build.gradle.kts' | Resolve-Path
if(-not $VersionCode){
  $VersionCode = [int](Get-Date -Format 'yyyyMMdd')
}

Write-Host "Target build.gradle.kts: $gradleFile" -ForegroundColor Cyan

$content = Get-Content $gradleFile -Raw

# 백업
$backup = "$gradleFile.bak"
if(-not (Test-Path $backup)) {
  Copy-Item $gradleFile $backup
  Write-Host "Backup created: $backup" -ForegroundColor DarkGray
}

$patternCode = 'val\s+computedVersionCode\s*=.*'
$patternName = 'val\s+computedVersionName\s*=.*'

$newLineCode = "    val computedVersionCode = System.getenv(\"VERSION_CODE\")?.toIntOrNull()\n        ?: $VersionCode // YYYYMMDD (auto-updated by bump_version.ps1)"
$newLineName = "    val computedVersionName = System.getenv(\"VERSION_NAME\") ?: \"$VersionName\" // Semantic Versioning (auto-updated)"

if($content -notmatch $patternCode){ throw 'computedVersionCode declaration not found.' }
if($content -notmatch $patternName){ throw 'computedVersionName declaration not found.' }

$content = [System.Text.RegularExpressions.Regex]::Replace($content, $patternCode, $newLineCode, 1)
$content = [System.Text.RegularExpressions.Regex]::Replace($content, $patternName, $newLineName, 1)

Set-Content -Path $gradleFile -Value $content -Encoding UTF8
Write-Host "Updated VersionCode=$VersionCode VersionName=$VersionName" -ForegroundColor Green

