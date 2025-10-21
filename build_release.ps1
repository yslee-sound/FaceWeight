# AlcoholicTimer 릴리즈 빌드 스크립트
# 사용법: .\build_release.ps1

# 환경변수 설정
$env:KEYSTORE_PATH = "G:/secure/AlcoholicTimer_Secure/alcoholic-timer-upload.jks"  # 실제 경로로 변경
$env:KEYSTORE_STORE_PW = "your_keystore_password"  # 실제 비밀번호로 변경
$env:KEY_ALIAS = "alcoholictimeruploadkey"  # 실제 별칭으로 변경
$env:KEY_PASSWORD = "your_key_password"  # 실제 키 비밀번호로 변경

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "  AlcoholicTimer 릴리즈 빌드" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

Write-Host "환경변수 설정 확인..." -ForegroundColor Yellow
Write-Host "KEYSTORE_PATH: $env:KEYSTORE_PATH" -ForegroundColor Gray
Write-Host "KEY_ALIAS: $env:KEY_ALIAS" -ForegroundColor Gray

# 키스토어 파일 존재 확인
if (-not (Test-Path $env:KEYSTORE_PATH)) {
    Write-Host "`n❌ 오류: 키스토어 파일을 찾을 수 없습니다!" -ForegroundColor Red
    Write-Host "   경로: $env:KEYSTORE_PATH" -ForegroundColor Red
    Write-Host "`n💡 스크립트 상단의 KEYSTORE_PATH를 실제 경로로 수정하세요." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ 키스토어 파일 확인 완료`n" -ForegroundColor Green

# 릴리즈 빌드 실행
Write-Host "AAB 빌드 시작..." -ForegroundColor Yellow
.\gradlew.bat clean :app:bundleRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "  빌드 성공! 🎉" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Cyan

    $aabPath = "app\build\outputs\bundle\release\app-release.aab"
    if (Test-Path $aabPath) {
        $size = (Get-Item $aabPath).Length / 1MB
        Write-Host "📦 AAB 파일 생성:" -ForegroundColor Green
        Write-Host "   위치: $aabPath" -ForegroundColor Gray
        Write-Host "   크기: $([math]::Round($size, 2)) MB`n" -ForegroundColor Gray
    }
} else {
    Write-Host "`n❌ 빌드 실패!" -ForegroundColor Red
    exit 1
}

