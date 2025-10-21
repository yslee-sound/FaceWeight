# 테스터에게 새 버전 배포 가이드

## 배포 전 체크리스트

### 1. 버전 코드 업데이트 확인
- [ ] `app/build.gradle.kts`의 `releaseVersionCode` 증가 확인
- [ ] `releaseVersionName` 업데이트 확인
- [ ] 현재 버전: **1.0.2 (2025100801)**

### 2. AAB 빌드 및 업로드
```bash
# Windows PowerShell
cd G:\Workspace\AlcoholicTimer
.\gradlew bundleRelease
```

### 3. Play Console 배포 절차

#### A. AAB 업로드
1. Play Console → **테스트** → **비공개 테스트** (또는 내부 테스트)
2. **새 버전 만들기** 클릭
3. `app/build/outputs/bundle/release/app-release.aab` 업로드
4. 변경사항 작성

#### B. 검토 및 출시 (중요!)
1. 변경사항 저장 후 **검토** 버튼 클릭
2. **비공개 테스트로 출시 시작** 버튼 클릭
3. 확인 팝업에서 **출시** 버튼 클릭
   - ⚠️ **이 단계를 건너뛰면 테스터가 업데이트를 받을 수 없습니다!**

#### C. 출시 상태 확인
1. **출시 개요** 탭에서 상태 확인
2. "검토 중" → "출시됨"으로 변경될 때까지 대기 (보통 수 분~수 시간)
3. 이메일 알림 확인

### 4. 테스터 설정 확인

#### A. 테스터 목록 확인
1. Play Console → **테스트** → 해당 트랙 → **테스터** 탭
2. 등록된 이메일 목록 확인
3. 새 테스터 추가 시:
   - 이메일 리스트 추가
   - 또는 Google 그룹 사용

#### B. 옵트인 URL 공유
1. **테스터** 탭에서 **옵트인 URL 복사**
2. 테스터들에게 URL 전달
3. 예시 메시지:
```
안녕하세요,
AlcoholicTimer 앱의 새 버전(1.0.2)이 테스트 트랙에 배포되었습니다.

1. 아래 링크를 클릭하여 테스터 등록을 확인해주세요:
   [옵트인 URL]

2. Play 스토어에서 앱을 검색하거나 "앱 및 기기 관리"에서 업데이트해주세요.

3. 업데이트가 보이지 않으면:
   - Play 스토어 캐시 삭제
   - 기기 재부팅
   - 수 시간 후 재시도

감사합니다.
```

### 5. 테스터 트러블슈팅

#### 문제: "업데이트" 버튼이 보이지 않음

**해결 방법:**
1. **Play Console에서 출시 완료 확인**
   - 출시 개요 → 상태: "출시됨"
   
2. **테스터 옵트인 재확인**
   - 테스터가 옵트인 URL을 다시 클릭
   - "이미 테스터입니다" 메시지 확인

3. **Play 스토어 캐시 삭제**
   ```
   설정 → 앱 → Google Play 스토어 → 저장공간 → 캐시 삭제
   ```

4. **Google Play 서비스 업데이트**
   - Play 스토어에서 "Google Play 서비스" 검색
   - 최신 버전으로 업데이트

5. **시간 대기**
   - 배포 후 전파까지 최대 24시간 소요 가능
   - 보통 1-2시간 내 업데이트 가능

6. **기기 호환성 확인**
   - minSdk: 21 (Android 5.0 이상)
   - 테스터 기기가 요구사항을 충족하는지 확인

#### 문제: "기기와 호환되지 않음"

**해결 방법:**
1. **Play Console** → **출시** → **출시 개요** → 해당 버전 클릭
2. **기기 카탈로그** 탭에서 지원 기기 확인
3. 테스터 기기가 목록에 있는지 확인
4. 없다면: `build.gradle.kts`의 호환성 설정 검토

#### 문제: "앱을 찾을 수 없음"

**해결 방법:**
1. **applicationId 확인**: kr.sweetapps.alcoholictimer
2. 테스터가 옳바른 Google 계정으로 로그인했는지 확인
3. 테스터 목록에 해당 계정이 등록되어 있는지 확인

### 6. 배포 후 확인사항

- [ ] Play Console에서 "출시됨" 상태 확인
- [ ] 테스터 1명 이상에게 업데이트 가능 확인
- [ ] 충돌 보고서 모니터링 (Play Console → 품질 → Android 비정상 종료 및 ANR)
- [ ] 사용자 피드백 수집

## 빠른 참조

### 현재 버전 정보
- **applicationId**: kr.sweetapps.alcoholictimer
- **versionCode**: 2025100801
- **versionName**: 1.0.2
- **minSdk**: 21
- **targetSdk**: 36

### 다음 버전 업데이트 시
1. `app/build.gradle.kts` 수정
   ```kotlin
   val releaseVersionCode = 2025100802  // 1 증가
   val releaseVersionName = "1.0.3"      // 버전명 업데이트
   ```

2. `CHANGELOG.md` 업데이트

3. AAB 빌드 및 업로드
   ```powershell
   .\gradlew.bat clean :app:bundleRelease
   ```

4. **반드시 "검토 및 출시" 버튼 클릭!**

## 추가 리소스

- [Google Play Console 도움말](https://support.google.com/googleplay/android-developer)
- [앱 배포 가이드](https://developer.android.com/studio/publish)
- [테스트 트랙 관리](https://support.google.com/googleplay/android-developer/answer/9845334)
