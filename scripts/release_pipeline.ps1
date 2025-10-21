<#!
.SYNOPSIS
  단일 명령으로 버전 증가(선택), 빌드, 검증, 아카이브 요약까지 수행하는 릴리스 파이프라인.
.DESCRIPTION
  기본 단계:
    1. (옵션) 버전 bump: update_version.ps1 호출
    2. Gradle clean
    3. (옵션) 디자인 토큰 검사 / 테스트
    4. :app:bundleRelease 빌드 (서명 키 미설정 시 경고 후 unsigned)
    5. 산출물 검증 (AAB / mapping)
    6. archive_release.ps1 실행 (release/<VersionName>_<VersionCode>)
    7. 요약 출력 (SHA256, 파일 경로)
  Play 업로드는 현재 수동 (Play Publisher 플러그인 미통합).
.PARAMETER VersionName
  새 versionName (Semantic). -SkipVersionBump 지정 시 무시.
.PARAMETER VersionCode
  새 versionCode (정수). 미지정 시 yyyymmdd00. -SkipVersionBump 지정 시 무시.
.PARAMETER SkipVersionBump
  지정하면 build.gradle.kts 수정 생략.
.PARAMETER SkipTests
  unit test / designTokenCheck 생략.
.PARAMETER OutputRoot
  아카이브 루트 (기본: release)
.EXAMPLE
  pwsh ./scripts/release_pipeline.ps1 -VersionName 1.0.2 -VersionCode 2025100701
.EXAMPLE
  pwsh ./scripts/release_pipeline.ps1 -SkipVersionBump -SkipTests
#>
param(
  [string]$VersionName,
  [int]$VersionCode,
  [switch]$SkipVersionBump,
  [switch]$SkipTests,
  [string]$OutputRoot = 'release'
)

$ErrorActionPreference = 'Stop'
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $repoRoot

function Info($m){ Write-Host $m -ForegroundColor Cyan }
function Warn($m){ Write-Warning $m }
function Fail($m){ Write-Error $m; exit 1 }

# 1. Version bump (optional)
if(-not $SkipVersionBump){
  if(-not $VersionName){ Fail 'VersionName required unless -SkipVersionBump' }
  if(-not $VersionCode){
    $VersionCode = [int]((Get-Date -Format 'yyyyMMdd') + '00')
  }
  Info "[1] Bumping version -> Name=$VersionName Code=$VersionCode"
  pwsh -NoProfile -File (Join-Path $PSScriptRoot 'update_version.ps1') -VersionName $VersionName -VersionCode $VersionCode
} else {
  Info '[1] Skipping version bump'
  # 현재 build.gradle.kts 내 버전 읽어오기
  $gradleFile = Resolve-Path 'app/build.gradle.kts'
  $txt = Get-Content $gradleFile -Raw
  $mName = [Regex]::Match($txt, 'val\s+releaseVersionName\s*=\s*"([^"]+)"')
  $mCode = [Regex]::Match($txt, 'val\s+releaseVersionCode\s*=\s*(\d+)')
  if($mName.Success){ $VersionName = $mName.Groups[1].Value }
  if($mCode.Success){ $VersionCode = [int]$mCode.Groups[1].Value }
  if(-not $VersionName -or -not $VersionCode){ Fail 'Failed to parse version from build.gradle.kts' }
  Info "Parsed existing version: $VersionName / $VersionCode"
}

# 2. Clean
Info '[2] Gradle clean'
cmd /c gradlew.bat clean | Out-Null

# 3. Verification tasks (optional)
if(-not $SkipTests){
  Info '[3] designTokenCheck'
  cmd /c gradlew.bat :app:designTokenCheck | Out-Null
  Info '[3] unit tests (debug)'
  cmd /c gradlew.bat :app:testDebugUnitTest | Out-Null
} else {
  Info '[3] Skipping tests & designTokenCheck'
}

# 4. Build bundle
Info '[4] Building :app:bundleRelease'
cmd /c gradlew.bat :app:bundleRelease --warning-mode=all | Out-Null

$aab = 'app/build/outputs/bundle/release/app-release.aab'
$mapping = 'app/build/outputs/mapping/release/mapping.txt'
if(-not (Test-Path $aab)){ Fail "AAB not found after build: $aab" }
if(-not (Test-Path $mapping)){ Warn "mapping.txt not found (minifyEnabled=false? or R8 optimized?)." }

# 5. Archive
Info '[5] Archiving artifacts'
$archiveParams = @('-VersionName', $VersionName, '-VersionCode', $VersionCode, '-OutputRoot', $OutputRoot)
# archive_release 자체에서 중복 디렉터리 있으면 throw
pwsh -NoProfile -File (Join-Path $PSScriptRoot 'archive_release.ps1') @archiveParams

$archiveDir = Join-Path $OutputRoot ("{0}_{1}" -f $VersionName, $VersionCode)
if(-not (Test-Path $archiveDir)){ Fail "Archive directory not created: $archiveDir" }

# 6. Summary
$sha = (Get-FileHash -Algorithm SHA256 $aab).Hash
$mappingArch = Join-Path $archiveDir 'mapping.txt'
$metaFile = Join-Path $archiveDir 'meta.txt'

Write-Host "\n=== RELEASE SUMMARY ===" -ForegroundColor Yellow
Write-Host ("Version        : {0} ({1})" -f $VersionName,$VersionCode)
Write-Host ("Bundle (AAB)   : {0}" -f (Resolve-Path $aab))
Write-Host ("Bundle SHA256  : {0}" -f $sha)
if(Test-Path $mapping){ Write-Host ("Mapping (R8)   : {0}" -f (Resolve-Path $mapping)) }
if(Test-Path $mappingArch){ Write-Host ("Archived map   : {0}" -f (Resolve-Path $mappingArch)) }
if(Test-Path $metaFile){ Write-Host ("Meta           : {0}" -f (Resolve-Path $metaFile)) }
Write-Host ("Archive Dir    : {0}" -f (Resolve-Path $archiveDir))
Write-Host 'Next (manual): Upload AAB to Play Console (Internal Testing), attach mapping.txt for deobfuscation.' -ForegroundColor DarkCyan
Write-Host 'If adopting automation: integrate Gradle Play Publisher plugin next.' -ForegroundColor DarkCyan

Info 'Release pipeline finished.'

