<#!
.SYNOPSIS
  app/build.gradle.kts 내 releaseVersionCode / releaseVersionName 값을 업데이트합니다.
.DESCRIPTION
  현재 build.gradle.kts 는 computed* 변수가 아닌 releaseVersionCode / releaseVersionName 을 사용하므로
  기존 bump_version.ps1 과 다른 정규식을 사용합니다.
  백업 파일 build.gradle.kts.verbak 생성 (여러 버전 유지 위해 타임스탬프 포함).
.PARAMETER VersionName
  새 versionName (semantic)
.PARAMETER VersionCode
  새 versionCode (정수)
.EXAMPLE
  pwsh ./scripts/update_version.ps1 -VersionName 1.0.2 -VersionCode 2025100701
#>
param(
  [Parameter(Mandatory=$true)][string]$VersionName,
  [Parameter(Mandatory=$true)][int]$VersionCode
)

$ErrorActionPreference = 'Stop'
$gradleFile = Join-Path $PSScriptRoot '..' 'app' 'build.gradle.kts' | Resolve-Path
$content = Get-Content $gradleFile -Raw

$patCode = 'val\s+releaseVersionCode\s*=\s*\d+'
$patName = 'val\s+releaseVersionName\s*=\s*"[^"]+"'
if($content -notmatch $patCode){ throw 'releaseVersionCode declaration not found.' }
if($content -notmatch $patName){ throw 'releaseVersionName declaration not found.' }

$backupName = "build.gradle.kts." + (Get-Date -Format 'yyyyMMdd_HHmmss') + '.verbak'
Copy-Item $gradleFile (Join-Path ([IO.Path]::GetDirectoryName($gradleFile)) $backupName)
Write-Host "Backup created: $backupName" -ForegroundColor DarkGray

$content = [Regex]::Replace($content, $patCode, "val releaseVersionCode = $VersionCode", 1)
$content = [Regex]::Replace($content, $patName, "val releaseVersionName = \"$VersionName\"", 1)

Set-Content -Path $gradleFile -Value $content -Encoding UTF8
Write-Host "Updated releaseVersionCode=$VersionCode releaseVersionName=$VersionName" -ForegroundColor Green

