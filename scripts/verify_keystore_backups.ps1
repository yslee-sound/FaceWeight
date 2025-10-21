# AlcoholicTimer 키스토어 백업 검증 스크립트
# 실행: .\scripts\verify_keystore_backups.ps1

param(
    [string]$KeystorePath = "G:\secure\alcoholic-timer-upload.jks",
    [string]$BackupBaseDir = "G:\Workspace\AlcoholicTimer\keystore-backups"
)

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

Write-ColorOutput "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
Write-ColorOutput "  키스토어 백업 검증" "Cyan"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

# 원본 확인
Write-ColorOutput "[1/3] 원본 키스토어 확인..." "Yellow"
if (-not (Test-Path $KeystorePath)) {
    Write-ColorOutput "❌ 원본 파일을 찾을 수 없습니다: $KeystorePath" "Red"
    exit 1
}

$OriginalHash = (Get-FileHash $KeystorePath -Algorithm SHA256).Hash
$OriginalSize = (Get-Item $KeystorePath).Length
Write-ColorOutput "✅ 원본 파일 확인" "Green"
Write-ColorOutput "   경로: $KeystorePath" "Gray"
Write-ColorOutput "   크기: $OriginalSize bytes" "Gray"
Write-ColorOutput "   SHA-256: $OriginalHash" "Gray"

# 백업 파일 검색
Write-ColorOutput "`n[2/3] 백업 파일 검색 중..." "Yellow"
if (-not (Test-Path $BackupBaseDir)) {
    Write-ColorOutput "❌ 백업 디렉토리가 없습니다: $BackupBaseDir" "Red"
    Write-ColorOutput "   백업 스크립트를 먼저 실행하세요: .\scripts\backup_keystore.ps1" "Yellow"
    exit 1
}

$BackupFiles = Get-ChildItem -Path $BackupBaseDir -Recurse -Filter "alcoholic-timer-upload.jks"
Write-ColorOutput "✅ 백업 파일 $($BackupFiles.Count)개 발견" "Green"

# 각 백업 검증
Write-ColorOutput "`n[3/3] 백업 파일 검증 중..." "Yellow"
$ValidBackups = 0
$InvalidBackups = 0

foreach ($backup in $BackupFiles) {
    $backupHash = (Get-FileHash $backup.FullName -Algorithm SHA256).Hash
    $backupSize = $backup.Length

    if ($backupHash -eq $OriginalHash) {
        Write-ColorOutput "`n✅ 유효한 백업: $($backup.Directory.Name)" "Green"
        Write-ColorOutput "   경로: $($backup.FullName)" "Gray"
        Write-ColorOutput "   날짜: $($backup.LastWriteTime)" "Gray"
        Write-ColorOutput "   해시: 일치 ✓" "Green"
        $ValidBackups++
    } else {
        Write-ColorOutput "`n❌ 무효한 백업: $($backup.Directory.Name)" "Red"
        Write-ColorOutput "   경로: $($backup.FullName)" "Gray"
        Write-ColorOutput "   해시: 불일치 ✗" "Red"
        $InvalidBackups++
    }
}

# 요약
Write-ColorOutput "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
Write-ColorOutput "  검증 완료" "Cyan"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

Write-ColorOutput "📊 검증 결과:" "White"
Write-ColorOutput "   총 백업: $($BackupFiles.Count)개" "Gray"
Write-ColorOutput "   ✅ 유효: $ValidBackups개" "Green"
if ($InvalidBackups -gt 0) {
    Write-ColorOutput "   ❌ 무효: $InvalidBackups개" "Red"
}

if ($ValidBackups -eq 0) {
    Write-ColorOutput "`n⚠️  경고: 유효한 백업이 없습니다!" "Red"
    Write-ColorOutput "   지금 바로 백업하세요: .\scripts\backup_keystore.ps1" "Yellow"
} elseif ($ValidBackups -lt 3) {
    Write-ColorOutput "`n⚠️  권장: 최소 3개의 백업을 유지하세요" "Yellow"
    Write-ColorOutput "   현재: $ValidBackups개 / 권장: 3개 이상" "Gray"
} else {
    Write-ColorOutput "`n✅ 백업 상태 양호!" "Green"
}

Write-ColorOutput "`n📝 백업 위치 권장사항:" "Yellow"
Write-ColorOutput "   1. 로컬: $BackupBaseDir" "White"
Write-ColorOutput "   2. 클라우드: Google Drive (암호화 ZIP)" "White"
Write-ColorOutput "   3. USB: 외장 드라이브 (물리적 분리)" "White"
Write-ColorOutput "   4. 비밀번호 관리자: 1Password/Bitwarden" "White"
Write-ColorOutput "   5. 종이 기록: 금고/서랍 보관`n" "White"

Write-ColorOutput "💡 키스토어 위치 변경 시:" "Cyan"
Write-ColorOutput "   현재 경로: $KeystorePath" "Gray"
Write-ColorOutput "   변경 방법: .\scripts\verify_keystore_backups.ps1 -KeystorePath '새경로'" "Gray"
Write-ColorOutput "   ⚠️  위치 변경 후 반드시 SHA-256 해시 검증 필요!`n" "Yellow"
