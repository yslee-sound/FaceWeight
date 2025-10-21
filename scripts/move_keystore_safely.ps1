# AlcoholicTimer 키스토어 안전 이동 스크립트
# 실행: .\scripts\move_keystore_safely.ps1 -NewPath "G:\new\path\alcoholic-timer-upload.jks"

param(
    [Parameter(Mandatory=$true)]
    [string]$NewPath,
    [string]$CurrentPath = "G:\secure\alcoholic-timer-upload.jks",
    [switch]$DeleteOriginal = $false
)

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

Write-ColorOutput "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
Write-ColorOutput "  키스토어 안전 이동 도구" "Cyan"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

# 1. 원본 파일 확인
Write-ColorOutput "[1/5] 원본 키스토어 확인..." "Yellow"
if (-not (Test-Path $CurrentPath)) {
    Write-ColorOutput "❌ 오류: 원본 파일을 찾을 수 없습니다!" "Red"
    Write-ColorOutput "   경로: $CurrentPath" "Red"
    Write-ColorOutput "`n💡 현재 위치를 지정하세요:" "Yellow"
    Write-ColorOutput "   .\scripts\move_keystore_safely.ps1 -CurrentPath '실제경로' -NewPath '새경로'" "Gray"
    exit 1
}

$OriginalHash = (Get-FileHash $CurrentPath -Algorithm SHA256).Hash
$OriginalSize = (Get-Item $CurrentPath).Length
Write-ColorOutput "✅ 원본 파일 확인 완료" "Green"
Write-ColorOutput "   현재 위치: $CurrentPath" "Gray"
Write-ColorOutput "   파일 크기: $OriginalSize bytes" "Gray"
Write-ColorOutput "   SHA-256: $OriginalHash" "Gray"

# 2. 새 경로 확인
Write-ColorOutput "`n[2/5] 새 위치 확인..." "Yellow"
$NewDir = Split-Path $NewPath -Parent

if (Test-Path $NewPath) {
    Write-ColorOutput "⚠️  경고: 새 위치에 이미 파일이 존재합니다!" "Yellow"
    Write-ColorOutput "   경로: $NewPath" "Yellow"

    $ExistingHash = (Get-FileHash $NewPath -Algorithm SHA256).Hash
    if ($ExistingHash -eq $OriginalHash) {
        Write-ColorOutput "✅ 기존 파일이 동일합니다 (SHA-256 일치)" "Green"
        Write-ColorOutput "   이동 작업을 건너뜁니다." "Gray"

        if ($DeleteOriginal) {
            Write-ColorOutput "`n⚠️  원본 삭제 플래그가 설정되어 있지만," "Yellow"
            Write-ColorOutput "   새 위치에 동일한 파일이 이미 있으므로" "Yellow"
            Write-ColorOutput "   원본 삭제만 진행합니다." "Yellow"

            $Confirmation = Read-Host "`n원본 파일을 삭제하시겠습니까? (yes/no)"
            if ($Confirmation -eq "yes") {
                Remove-Item $CurrentPath -Force
                Write-ColorOutput "✅ 원본 파일 삭제 완료" "Green"
            } else {
                Write-ColorOutput "❌ 삭제 취소됨" "Yellow"
            }
        }
        exit 0
    } else {
        Write-ColorOutput "❌ 오류: 기존 파일의 해시가 다릅니다!" "Red"
        Write-ColorOutput "   원본: $OriginalHash" "Red"
        Write-ColorOutput "   기존: $ExistingHash" "Red"
        Write-ColorOutput "`n⚠️  다른 파일을 덮어쓸 수 없습니다. 다른 경로를 사용하세요." "Yellow"
        exit 1
    }
}

Write-ColorOutput "✅ 새 위치: $NewPath" "Green"

# 3. 디렉토리 생성
Write-ColorOutput "`n[3/5] 디렉토리 생성 중..." "Yellow"
if (-not (Test-Path $NewDir)) {
    New-Item -ItemType Directory -Path $NewDir -Force | Out-Null
    Write-ColorOutput "✅ 디렉토리 생성: $NewDir" "Green"
} else {
    Write-ColorOutput "✅ 디렉토리 이미 존재: $NewDir" "Green"
}

# 4. 파일 복사
Write-ColorOutput "`n[4/5] 파일 복사 중..." "Yellow"
Copy-Item $CurrentPath $NewPath -Force
Write-ColorOutput "✅ 복사 완료" "Green"

# 5. 해시 검증
Write-ColorOutput "`n[5/5] 무결성 검증 중..." "Yellow"
$NewHash = (Get-FileHash $NewPath -Algorithm SHA256).Hash

if ($NewHash -eq $OriginalHash) {
    Write-ColorOutput "✅ 검증 성공! SHA-256 해시 일치" "Green"
    Write-ColorOutput "   원본: $OriginalHash" "Gray"
    Write-ColorOutput "   새 파일: $NewHash" "Gray"
} else {
    Write-ColorOutput "❌ 오류: 파일이 손상되었습니다!" "Red"
    Write-ColorOutput "   원본: $OriginalHash" "Red"
    Write-ColorOutput "   새 파일: $NewHash" "Red"
    Write-ColorOutput "`n⚠️  새 파일을 삭제하고 다시 시도하세요." "Yellow"
    Remove-Item $NewPath -Force
    exit 1
}

# 원본 삭제 (옵션)
if ($DeleteOriginal) {
    Write-ColorOutput "`n⚠️  원본 파일 삭제 확인" "Yellow"
    Write-ColorOutput "   원본: $CurrentPath" "Gray"
    Write-ColorOutput "   새 위치: $NewPath" "Gray"
    $Confirmation = Read-Host "`n정말 원본을 삭제하시겠습니까? (yes/no)"

    if ($Confirmation -eq "yes") {
        Remove-Item $CurrentPath -Force
        Write-ColorOutput "✅ 원본 파일 삭제 완료" "Green"
    } else {
        Write-ColorOutput "❌ 삭제 취소됨 (원본 유지)" "Yellow"
    }
} else {
    Write-ColorOutput "`n💡 원본 파일은 유지됩니다" "Cyan"
    Write-ColorOutput "   원본: $CurrentPath" "Gray"
    Write-ColorOutput "   삭제하려면: -DeleteOriginal 플래그 사용" "Gray"
}

# 완료 메시지
Write-ColorOutput "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" "Cyan"
Write-ColorOutput "  이동 완료!" "Cyan"
Write-ColorOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" "Cyan"

Write-ColorOutput "📍 새 키스토어 위치: $NewPath" "Green"
Write-ColorOutput "🔐 SHA-256: $NewHash" "Gray"

Write-ColorOutput "`n📝 다음 단계:" "Yellow"
Write-ColorOutput "   1. 환경변수 업데이트:" "White"
Write-ColorOutput "      `$env:KEYSTORE_PATH=`"$NewPath`"" "Gray"
Write-ColorOutput "`n   2. 빌드 테스트:" "White"
Write-ColorOutput "      .\gradlew.bat clean :app:bundleRelease" "Gray"
Write-ColorOutput "`n   3. 백업 스크립트 업데이트:" "White"
Write-ColorOutput "      .\scripts\backup_keystore.ps1 -KeystorePath `"$NewPath`"" "Gray"
Write-ColorOutput "`n   4. 새 위치로 백업 생성:" "White"
Write-ColorOutput "      지금 바로 백업 스크립트 실행 권장" "Gray"

if (-not $DeleteOriginal) {
    Write-ColorOutput "`n⚠️  참고: 원본 파일이 아직 남아 있습니다" "Yellow"
    Write-ColorOutput "   빌드 테스트 후 원본 삭제를 권장합니다:" "Yellow"
    Write-ColorOutput "   Remove-Item `"$CurrentPath`"" "Gray"
}

Write-ColorOutput "`n✅ 키스토어 이동 완료!`n" "Green"
