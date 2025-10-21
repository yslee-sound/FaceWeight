# AlcoholicTimer 키스토어 자동 백업 스크립트
# 실행: .\scripts\backup_keystore.ps1

param(
    [string]$KeystorePath = "G:\secure\alcoholic-timer-upload.jks",
    [string]$BackupBaseDir = "G:\Workspace\AlcoholicTimer\keystore-backups",
    [string]$UsbDrive = "E:",  # USB 드라이브 문자 (필요시 변경)
    [switch]$CreateEncryptedZip = $true
)

# 색상 출력 함수
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

# 헤더 출력
Write-ColorOutput "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
Write-ColorOutput "  AlcoholicTimer 키스토어 백업 스크립트" "Cyan"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

# 1. 원본 키스토어 존재 확인
Write-ColorOutput "[1/6] 원본 키스토어 확인..." "Yellow"
if (-not (Test-Path $KeystorePath)) {
    Write-ColorOutput "❌ 오류: 키스토어 파일을 찾을 수 없습니다!" "Red"
    Write-ColorOutput "   경로: $KeystorePath" "Red"
    Write-ColorOutput "`n💡 해결 방법:" "Yellow"
    Write-ColorOutput "   1. 키스토어 파일이 올바른 위치에 있는지 확인" "White"
    Write-ColorOutput "   2. 스크립트 실행 시 -KeystorePath 매개변수로 경로 지정" "White"
    Write-ColorOutput "      예: .\backup_keystore.ps1 -KeystorePath 'G:\other\path\keystore.jks'" "Gray"
    exit 1
}
Write-ColorOutput "✅ 원본 파일 확인 완료: $KeystorePath" "Green"

# SHA-256 해시 계산
$OriginalHash = (Get-FileHash $KeystorePath -Algorithm SHA256).Hash
Write-ColorOutput "   SHA-256: $OriginalHash" "Gray"

# 2. 백업 디렉토리 생성
Write-ColorOutput "`n[2/6] 백업 디렉토리 생성..." "Yellow"
$Timestamp = Get-Date -Format "yyyy-MM-dd_HHmmss"
$DateFolder = Get-Date -Format "yyyy-MM"
$BackupDir = Join-Path $BackupBaseDir $DateFolder
$BackupDirWithTime = Join-Path $BackupDir $Timestamp

if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
    Write-ColorOutput "✅ 월별 폴더 생성: $BackupDir" "Green"
}

New-Item -ItemType Directory -Path $BackupDirWithTime -Force | Out-Null
Write-ColorOutput "✅ 타임스탬프 폴더 생성: $BackupDirWithTime" "Green"

# 3. 로컬 백업 (날짜별 폴더)
Write-ColorOutput "`n[3/6] 로컬 백업 생성 중..." "Yellow"
$LocalBackupPath = Join-Path $BackupDirWithTime "alcoholic-timer-upload.jks"
Copy-Item $KeystorePath $LocalBackupPath -Force

# 해시 검증
$BackupHash = (Get-FileHash $LocalBackupPath -Algorithm SHA256).Hash
if ($BackupHash -eq $OriginalHash) {
    Write-ColorOutput "✅ 로컬 백업 완료 (해시 일치)" "Green"
    Write-ColorOutput "   위치: $LocalBackupPath" "Gray"
} else {
    Write-ColorOutput "❌ 경고: 백업 파일의 해시가 일치하지 않습니다!" "Red"
    exit 1
}

# 4. README 파일 생성
Write-ColorOutput "`n[4/6] 복구 안내 파일 생성 중..." "Yellow"
$ReadmePath = Join-Path $BackupDirWithTime "README.txt"
$ReadmeContent = @"
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AlcoholicTimer 키스토어 백업
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

백업 날짜: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
원본 경로: $KeystorePath

파일 정보:
  - 파일명: alcoholic-timer-upload.jks
  - SHA-256: $OriginalHash
  - 크기: $((Get-Item $KeystorePath).Length) bytes

키스토어 정보:
  - Key Alias: alcoholictimeruploadkey
  - Keystore 비밀번호: [별도 보관 - 비밀번호 관리자 확인]
  - Key 비밀번호: [별도 보관 - 비밀번호 관리자 확인]

⚠️ 중요 안내:
  - 이 파일은 앱 업데이트의 유일한 열쇠입니다
  - 분실 시 앱 업데이트가 영구 불가능합니다
  - Google도 복구해줄 수 없습니다
  - 안전한 곳에 보관하세요

복구 방법:
  1. alcoholic-timer-upload.jks 파일을 G:/secure/ 폴더로 복사
  2. 환경변수 설정:
     `$env:KEYSTORE_PATH="G:/secure/alcoholic-timer-upload.jks"
     `$env:KEY_ALIAS="alcoholictimeruploadkey"
     `$env:KEYSTORE_STORE_PW="[비밀번호]"
     `$env:KEY_PASSWORD="[키 비밀번호]"
  3. 빌드 테스트: .\gradlew.bat :app:bundleRelease

SHA-256 해시 검증:
  Get-FileHash "alcoholic-timer-upload.jks" -Algorithm SHA256
  결과가 위의 SHA-256과 일치해야 함

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"@

$ReadmeContent | Out-File -FilePath $ReadmePath -Encoding UTF8
Write-ColorOutput "✅ README.txt 생성 완료" "Green"

# 5. SHA-256 해시 파일 생성
$HashFilePath = Join-Path $BackupDirWithTime "SHA256.txt"
"$OriginalHash  alcoholic-timer-upload.jks" | Out-File -FilePath $HashFilePath -Encoding ASCII
Write-ColorOutput "✅ SHA256.txt 생성 완료" "Green"

# 6. 암호화 ZIP 생성 (클라우드 업로드용)
if ($CreateEncryptedZip) {
    Write-ColorOutput "`n[5/6] 암호화 ZIP 생성 중..." "Yellow"
    $ZipPath = Join-Path $BackupDir "alcoholic-timer-backup-$Timestamp.zip"

    Write-ColorOutput "📦 7-Zip 또는 내장 압축 사용..." "Gray"

    # PowerShell 내장 압축 (암호화 없음 - 경고 표시)
    Compress-Archive -Path $BackupDirWithTime -DestinationPath $ZipPath -Force
    Write-ColorOutput "✅ ZIP 파일 생성: $ZipPath" "Green"
    Write-ColorOutput "⚠️  경고: 이 ZIP은 암호화되지 않았습니다!" "Yellow"
    Write-ColorOutput "   7-Zip으로 수동 암호화 권장:" "Yellow"
    Write-ColorOutput "   1. $ZipPath 마우스 우클릭" "Gray"
    Write-ColorOutput "   2. 7-Zip > 압축하기" "Gray"
    Write-ColorOutput "   3. '암호화' 섹션에서 강력한 비밀번호 입력" "Gray"
    Write-ColorOutput "   4. 암호화 방법: AES-256 선택" "Gray"
} else {
    Write-ColorOutput "`n[5/6] 암호화 ZIP 생성 건너뛰기" "Gray"
}

# 7. USB 백업 (선택적)
Write-ColorOutput "`n[6/6] USB 백업 확인 중..." "Yellow"
if (Test-Path "${UsbDrive}\") {
    $UsbBackupDir = "${UsbDrive}\AlcoholicTimer_Keystore_Backup"
    $UsbBackupDirWithDate = Join-Path $UsbBackupDir $DateFolder

    if (-not (Test-Path $UsbBackupDirWithDate)) {
        New-Item -ItemType Directory -Path $UsbBackupDirWithDate -Force | Out-Null
    }

    $UsbBackupPath = Join-Path $UsbBackupDirWithDate "alcoholic-timer-upload.jks"
    $UsbReadmePath = Join-Path $UsbBackupDirWithDate "README.txt"

    Copy-Item $KeystorePath $UsbBackupPath -Force
    Copy-Item $ReadmePath $UsbReadmePath -Force

    # USB 백업 해시 검증
    $UsbHash = (Get-FileHash $UsbBackupPath -Algorithm SHA256).Hash
    if ($UsbHash -eq $OriginalHash) {
        Write-ColorOutput "✅ USB 백업 완료: $UsbBackupPath" "Green"
    } else {
        Write-ColorOutput "❌ 경고: USB 백업 파일의 해시가 일치하지 않습니다!" "Red"
    }
} else {
    Write-ColorOutput "⚠️  USB 드라이브를 찾을 수 없습니다 (${UsbDrive})" "Yellow"
    Write-ColorOutput "   USB 백업을 건너뜁니다. 나중에 수동으로 복사하세요." "Yellow"
}

# 백업 요약
Write-ColorOutput "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
Write-ColorOutput "  백업 완료!" "Cyan"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

Write-ColorOutput "📋 백업 위치:" "White"
Write-ColorOutput "   1️⃣  로컬: $LocalBackupPath" "Green"
if ($CreateEncryptedZip) {
    Write-ColorOutput "   2️⃣  ZIP: $ZipPath" "Green"
}
if (Test-Path "${UsbDrive}\") {
    Write-ColorOutput "   3️⃣  USB: $UsbBackupPath" "Green"
}

Write-ColorOutput "`n🔐 SHA-256 해시: $OriginalHash" "Gray"

Write-ColorOutput "`n📝 다음 단계:" "Yellow"
Write-ColorOutput "   1. ZIP 파일을 7-Zip으로 암호화 (AES-256)" "White"
Write-ColorOutput "   2. 암호화된 ZIP을 Google Drive에 업로드" "White"
Write-ColorOutput "   3. USB 드라이브를 안전한 곳에 보관" "White"
Write-ColorOutput "   4. 비밀번호 관리자(1Password)에 정보 저장" "White"
Write-ColorOutput "   5. 종이에 비밀번호 기록 후 금고 보관" "White"

Write-ColorOutput "`n⚠️  중요: 이 키스토어 파일은 앱 업데이트의 유일한 열쇠입니다!" "Red"
Write-ColorOutput "   분실 시 앱 업데이트가 영구 불가능합니다." "Red"

Write-ColorOutput "`n✅ 백업 스크립트 실행 완료!" "Green"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

# 백업 로그 파일 생성
$LogPath = Join-Path $BackupBaseDir "backup-log.txt"
$LogEntry = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 백업 완료 - SHA-256: $OriginalHash - 위치: $BackupDirWithTime"
Add-Content -Path $LogPath -Value $LogEntry

Write-ColorOutput "📄 백업 로그: $LogPath`n" "Gray"

