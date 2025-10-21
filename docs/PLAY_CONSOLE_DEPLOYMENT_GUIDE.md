# Play Console 비공개 테스트 배포 가이드

## 📱 현재 상태
- **앱 이름**: AlcoholicTimer
- **applicationId**: kr.sweetapps.alcoholictimer
- **현재 버전**: 1.0.2 (versionCode: 2025100801)
- **배포 트랙**: 비공개 테스트 (Closed Testing)

---

## 🎯 1단계: AAB 파일 빌드

### 1-1. 환경변수 설정 (서명용)

**Windows PowerShell**에서 실행:

```powershell
# 키스토어 경로 설정
$env:KEYSTORE_PATH = "G:/secure/AlcoholicTimer_Secure/alcoholic-timer-upload.jks"  # 실제 경로로 변경

# 키스토어 비밀번호
$env:KEYSTORE_STORE_PW = "your_keystore_password"  # 실제 비밀번호로 변경

# 키 별칭
$env:KEY_ALIAS = "alcoholictimeruploadkey"  # 실제 별칭으로 변경

# 키 비밀번호
$env:KEY_PASSWORD = "your_key_password"  # 실제 비밀번호로 변경

# 환경변수 확인
echo "KEYSTORE_PATH: $env:KEYSTORE_PATH"
echo "KEY_ALIAS: $env:KEY_ALIAS"
```

⚠️ **주의**: 
- 키스토어 파일이 없다면 먼저 생성해야 합니다
- 비밀번호는 안전하게 보관하세요 (분실 시 앱 업데이트 불가능!)

### 1-2. Release AAB 빌드

```powershell
cd G:\Workspace\AlcoholicTimer

# AAB 빌드 (서명 포함)
.\gradlew.bat clean :app:bundleRelease
```

**빌드 성공 메시지**:
```
BUILD SUCCESSFUL in XXs
```

### 1-3. AAB 파일 위치 확인

빌드된 AAB 파일 위치:
```
G:\Workspace\AlcoholicTimer\app\build\outputs\bundle\release\app-release.aab
```

파일 크기 확인 (약 5-15MB):
```powershell
ls G:\Workspace\AlcoholicTimer\app\build\outputs\bundle\release\app-release.aab
```

---

## 🌐 2단계: Play Console 접속 및 앱 선택

### 2-1. Play Console 접속

1. 웹브라우저에서 접속:
   ```
   https://play.google.com/console
   ```

2. Google 계정으로 로그인
   - 개발자 계정이어야 함
   - 등록 비용: $25 (1회만)

### 2-2. 앱 선택

1. **모든 앱** 목록에서 **"AlcoholicTimer"** 또는 **"kr.sweetapps.alcoholictimer"** 선택
2. 왼쪽 메뉴에서 **"테스트"** 섹션 찾기

---

## 🧪 3단계: 비공개 테스트 설정 (최초 1회)

### 3-1. 비공개 테스트 트랙 생성 (최초만)

이미 생성되어 있다면 **4단계**로 건너뛰세요.

1. 왼쪽 메뉴 → **테스트** → **비공개 테스트**
2. **"새 트랙 만들기"** 클릭
3. 트랙 이름 입력: `Closed Testing` (또는 원하는 이름)
4. **"트랙 만들기"** 클릭

### 3-2. 테스터 목록 설정

1. **"테스터"** 탭 클릭
2. **"목록 만들기"** 클릭
3. 목록 이름: `내부 테스터` (또는 원하는 이름)
4. 테스터 이메일 추가:
   ```
   tester1@gmail.com
   tester2@gmail.com
   tester3@gmail.com
   ```
   - 한 줄에 하나씩 입력
   - 또는 쉼표로 구분

5. **"변경사항 저장"** 클릭

### 3-3. 옵트인 URL 복사

1. **"테스터"** 탭에서 **"옵트인 URL 복사"** 클릭
2. URL 예시:
   ```
   https://play.google.com/apps/testing/kr.sweetapps.alcoholictimer
   ```
3. 이 URL을 테스터들에게 전송 (이메일, 카카오톡 등)

**테스터가 해야 할 일**:
1. 옵트인 URL 클릭
2. "테스터 되기" 버튼 클릭
3. Play 스토어에서 앱 다운로드

---

## 📦 4단계: 새 버전 업로드

### 4-1. 새 버전 만들기

1. **테스트** → **비공개 테스트** 메뉴 선택
2. **"새 버전 만들기"** 버튼 클릭
   - 또는 **"출시 관리"** → **"버전 만들기"**

### 4-2. AAB 파일 업로드

1. **"App Bundle 업로드"** 영역 찾기
2. 두 가지 방법 중 하나 선택:

   **방법 A**: 드래그 앤 드롭
   ```
   app-release.aab 파일을 브라우저 창으로 드래그
   ```

   **방법 B**: 파일 선택
   ```
   1. "파일 선택" 버튼 클릭
   2. G:\Workspace\AlcoholicTimer\app\build\outputs\bundle\release\app-release.aab 선택
   ```

3. **업로드 진행 상황 확인** (1-5분 소요)
   - 녹색 체크 표시가 나타나면 성공 ✅

### 4-3. 업로드된 버전 정보 확인

업로드 성공 시 표시되는 정보:
```
✓ app-release.aab
  버전 코드: 2025100801
  버전 이름: 1.0.2
  크기: XX.X MB
  최소 SDK: 21 (Android 5.0)
  대상 SDK: 36 (Android 14)
```

⚠️ **오류 발생 시**:
- 버전 코드가 이전 버전보다 작음 → `build.gradle.kts`에서 증가
- 서명 키 불일치 → 동일한 keystore 사용 확인
- 패키지명 불일치 → applicationId 확인

---

## 📝 5단계: 출시 정보 작성

### 5-1. 출시 노트 작성 (필수)

**"이번 업데이트의 새로운 기능"** 섹션:

```
• TopAppBar와 선택 박스 간격 개선
• In-App Update 기능 추가 - 앱 실행 시 자동 업데이트 알림
• 금주 기록 화면 UI 개선
• 버그 수정 및 성능 향상
```

**언어별 작성**:
- 한국어(ko-KR) 필수
- 영어(en-US) 선택사항

**글자 수 제한**: 최대 500자

### 5-2. 버전 이름 확인

자동으로 표시됨:
```
버전 이름: 1.0.2
```

---

## ⚠️ 6단계: 검토 및 출시 (중요!)

### 6-1. 검토 페이지 확인

1. 모든 정보 입력 후 페이지 하단 **"검토"** 버튼 클릭
2. 다음 정보 확인:
   - ✓ 버전 코드: 2025100801
   - ✓ 버전 이름: 1.0.2
   - ✓ 출시 노트 작성됨
   - ✓ 대상: 비공개 테스트 트랙

### 6-2. 비공개 테스트로 출시

⚠️ **가장 중요한 단계!**

1. **"비공개 테스트로 출시 시작"** 버튼 클릭
   - 버튼 색상: 파란색
   - 위치: 페이지 하단

2. 확인 팝업 표시:
   ```
   "이 버전을 비공개 테스트로 출시하시겠습니까?"
   ```

3. **"출시"** 버튼 클릭 (팝업 내)

4. 출시 진행 메시지:
   ```
   "출시를 검토하고 있습니다..."
   ```

### 6-3. 출시 상태 확인

1. **"출시 개요"** 탭으로 이동
2. 상태 확인:
   - 🟡 **"검토 중"** → 대기 (수 분 ~ 수 시간)
   - 🟢 **"출시됨"** → 완료! ✅

**검토 시간**:
- 일반적: 수 분 ~ 2시간
- 최대: 24시간
- 비공개 테스트는 일반적으로 빠름 (10-30분)

---

## 📧 7단계: 테스터에게 안내

### 7-1. 이메일 또는 메시지 전송

**템플릿**:

```
안녕하세요!

AlcoholicTimer 앱의 새 버전(1.0.2)이 비공개 테스트에 배포되었습니다.

📱 업데이트 방법:

1. 앱을 실행하면 업데이트 다이얼로그가 자동으로 표시됩니다!
   → "업데이트" 버튼 클릭

2. 또는 Play 스토어에서 수동 업데이트:
   - Play 스토어 앱 열기
   - 프로필 아이콘 → "앱 및 기기 관리"
   - "업데이트 가능"에서 AlcoholicTimer 찾기
   - "업데이트" 버튼 클릭

🆕 이번 버전의 변경사항:
• TopAppBar 간격 개선
• 자동 업데이트 알림 기능 추가
• 버그 수정

업데이트가 보이지 않으면 1-2시간 후 다시 확인해주세요.

감사합니다!
```

### 7-2. 옵트인 URL 재전송 (신규 테스터용)

신규 테스터가 있다면:
```
테스터 등록 링크:
https://play.google.com/apps/testing/kr.sweetapps.alcoholictimer

위 링크를 클릭하여 "테스터 되기"를 먼저 해주세요.
```

---

## ✅ 8단계: 배포 확인

### 8-1. Play Console에서 확인

1. **출시 개요** → **비공개 테스트**
2. 확인 항목:
   - ✓ 상태: **출시됨** (녹색)
   - ✓ 버전 코드: 2025100801
   - ✓ 출시 날짜: 2025-10-09
   - ✓ 테스터 수: X명

### 8-2. 테스터 피드백 수집

테스터에게 확인 요청:
- ✓ 앱 실행 시 업데이트 다이얼로그 표시되는지
- ✓ 업데이트 후 TopAppBar 간격 개선되었는지
- ✓ 앱이 정상 작동하는지
- ✓ 충돌(crash) 발생 여부

### 8-3. 충돌 보고서 모니터링

1. **Play Console** → **품질** → **Android 비정상 종료 및 ANR**
2. 새 버전에서 충돌이 없는지 확인
3. 문제 발견 시 즉시 수정 버전 준비

---

## 🔄 다음 버전 배포 시 (1.0.3)

### 1. 버전 코드 증가

**app/build.gradle.kts**:
```kotlin
val releaseVersionCode = 2025100802  // +1 증가 ✅
val releaseVersionName = "1.0.3"      // 버전명 업데이트
```

### 2. 코드 수정 및 빌드

```powershell
# 환경변수 확인
echo $env:KEYSTORE_PATH

# AAB 빌드
.\gradlew bundleRelease
```

### 3. Play Console 업로드

1. **테스트** → **비공개 테스트**
2. **"새 버전 만들기"**
3. AAB 업로드
4. 출시 노트 작성
5. **"검토"** → **"비공개 테스트로 출시 시작"** ← 꼭 클릭!

### 4. 테스터 확인

- 이전 버전(1.0.2) 사용자가 앱 실행 시
- **자동으로 업데이트 다이얼로그 표시!** 🎉
- Play 스토어 수동 확인 불필요

---

## 🆘 문제 해결

### 문제 1: "이 버전의 버전 코드가 이미 사용 중입니다"

**원인**: 동일한 버전 코드로 이미 업로드함

**해결**:
```kotlin
// build.gradle.kts
val releaseVersionCode = 2025100802  // 기존 2025100801보다 큰 값
```

### 문제 2: "서명 키가 일치하지 않습니다"

**원인**: 다른 keystore 파일 사용

**해결**:
- 최초 업로드 시 사용한 keystore 파일 사용
- 키스토어 분실 시: 새 앱으로 등록 (기존 앱 업데이트 불가)

### 문제 3: "테스터가 업데이트를 볼 수 없습니다"

**원인**: "검토 및 출시" 버튼을 누르지 않음

**해결**:
1. **출시 개요** → 상태 확인
2. "초안" 상태면 → **"검토"** → **"출시"** 클릭
3. "출시됨" 상태가 될 때까지 대기

### 문제 4: 테스터가 앱을 찾을 수 없습니다

**원인**: 옵트인 안 함

**해결**:
1. 옵트인 URL 재전송
2. 테스터가 링크 클릭 → "테스터 되기"
3. "이미 테스터입니다" 메시지 확인

### 문제 5: 업데이트 다이얼로그가 안 나타납니다

**원인**: 
- 24시간이 지나지 않음
- Play Store에서 설치하지 않음

**해결**:
1. 앱 삭제 → Play 스토어에서 재설치
2. 24시간 대기
3. 테스터에게 수동 업데이트 안내

---

## 📊 배포 체크리스트

### 배포 전
- [ ] 버전 코드 증가 확인 (`build.gradle.kts`)
- [ ] 버전 이름 업데이트 확인
- [ ] 변경사항 정리 (`CHANGELOG.md`)
- [ ] 로컬 테스트 완료
- [ ] 키스토어 환경변수 설정

### 빌드
- [ ] `.\gradlew bundleRelease` 실행
- [ ] BUILD SUCCESSFUL 확인
- [ ] AAB 파일 존재 확인 (`app\build\outputs\bundle\release\`)

### Play Console
- [ ] AAB 파일 업로드
- [ ] 버전 정보 확인 (코드, 이름)
- [ ] 출시 노트 작성 (한국어 필수)
- [ ] **"검토" 버튼 클릭** ⚠️
- [ ] **"비공개 테스트로 출시 시작" 버튼 클릭** ⚠️
- [ ] 출시 상태 "출시됨" 확인

### 배포 후
- [ ] 테스터에게 안내 메시지 전송
- [ ] 1시간 후 테스터 1명 이상 업데이트 확인
- [ ] 충돌 보고서 확인 (24시간 내)
- [ ] 테스터 피드백 수집

---

## 🎯 예상 타임라인

### Day 1 (오늘)
```
09:00 - AAB 빌드 (10분)
09:15 - Play Console 업로드 (5분)
09:20 - 출시 노트 작성 (5분)
09:25 - 검토 및 출시 클릭 (1분)
09:30 - Google 검토 시작
10:00 - "출시됨" 상태 확인 ✅
10:05 - 테스터에게 안내 전송
11:00 - 테스터 1명 이상 업데이트 확인
```

### Day 2-3
```
- 테스터 피드백 수집
- 충돌 보고서 확인
- 버그 발견 시 수정 계획
```

### Day 7
```
- 비공개 테스트 안정화 확인
- 필요 시 버전 1.0.3 준비
```

---

## 📚 참고 자료

### Play Console 링크
- 개발자 콘솔: https://play.google.com/console
- 도움말: https://support.google.com/googleplay/android-developer

### 관련 문서
- `docs/IN_APP_UPDATE_IMPLEMENTATION.md` - In-App Update 구현
- `docs/TESTER_ROLLOUT_GUIDE.md` - 테스터 배포 가이드
- `docs/IN_APP_UPDATE_POLICY_REVIEW.md` - 정책 검토
- `CHANGELOG.md` - 버전별 변경사항

### Gradle 명령어
```powershell
# Debug 빌드
.\gradlew.bat :app:assembleDebug

# Release AAB 빌드
.\gradlew.bat clean :app:bundleRelease

# 빌드 캐시 클리어
.\gradlew.bat clean

# 의존성 업데이트 확인
.\gradlew.bat dependencyUpdates
```

---

## 🎉 완료!

비공개 테스트 배포가 완료되면:
- ✅ 테스터들이 앱 실행 시 자동으로 업데이트 알림 받음
- ✅ Play 스토어 수동 확인 불필요
- ✅ In-App Update 기능으로 업데이트율 대폭 증가
- ✅ 안정화 후 공개 출시 준비 가능

**축하합니다! 🎊**
