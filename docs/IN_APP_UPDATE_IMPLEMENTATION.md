# In-App Update 구현 완료 가이드

## ✅ 구현 완료

In-App Update 기능이 성공적으로 구현되었습니다!

---

## 구현된 내용

### 1. 라이브러리 추가

**gradle/libs.versions.toml**:
- `app-update-ktx`: 2.1.0 (Google Play Core KTX)
- `kotlinx-coroutines-play-services`: 1.7.3 (코루틴 지원)

**app/build.gradle.kts**:
```kotlin
implementation(libs.app.update.ktx)
implementation(libs.kotlinx.coroutines.play.services)
```

### 2. 핵심 클래스 생성

#### AppUpdateManager.kt
**위치**: `app/src/main/java/com/example/alcoholictimer/core/util/AppUpdateManager.kt`

**기능**:
- 앱 시작 시 자동 업데이트 확인
- 24시간마다 1회만 확인 (과도한 알림 방지)
- Flexible Update 지원 (사용자 선택 가능)
- Immediate Update 지원 (긴급 업데이트용)
- 업데이트 연기 횟수 제한 (최대 3회)

**주요 메서드**:
```kotlin
// 업데이트 확인
suspend fun checkForUpdate(
    forceCheck: Boolean = false,
    onUpdateAvailable: (AppUpdateInfo) -> Unit,
    onNoUpdate: () -> Unit
)

// Flexible Update 시작
fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo)

// 업데이트 완료
fun completeFlexibleUpdate()
```

#### AppUpdateDialog.kt
**위치**: `app/src/main/java/com/example/alcoholictimer/core/ui/components/AppUpdateDialog.kt`

**기능**:
- Material Design 3 스타일 업데이트 다이얼로그
- 사용자 친화적 UI
- 업데이트 내용 표시
- 버튼 구성: "나중에"(항상 표시, `enabled = canDismiss`), "업데이트"
  - `canDismiss=false`(연기 불가)일 때는 버튼을 숨기지 않고 비활성화로 표기
  - 다이얼로그 바깥 터치로 닫기(onDismissRequest)는 `canDismiss`가 true일 때만 동작

### 3. StartActivity 통합

**위치**: `app/src/main/java/com/example/alcoholictimer/feature/start/StartActivity.kt`

**동작 방식**:
1. 앱 시작 시 자동으로 업데이트 확인
2. 새 버전이 있으면 다이얼로그 표시
3. 사용자가 "업데이트" 선택 시:
   - 백그라운드에서 다운로드
   - 다운로드 완료 후 스낵바 표시
   - "다시 시작" 버튼으로 설치 완료
4. `canDismiss` 전달 규칙: `!appUpdateManager.isMaxPostponeReached() || demoActive`
   - 연기 한도(3회) 초과 시 닫기 불가 + "나중에" 비활성화 표시

---

## 작동 원리

### 업데이트 플로우

```
앱 시작
   ↓
업데이트 확인 (24시간마다 1회)
   ↓
새 버전 있음? ─ 아니오 → 정상 실행
   ↓ 예
다이얼로그 표시
   ↓
사용자 선택
   ├─ "나중에" → 연기 카운트 +1 (최대 3회) [버튼은 항상 표시, 필요 시 비활성화]
   └─ "업데이트" → 백그라운드 다운로드
                  ↓
              다운로드 완료
                  ↓
              스낵바 표시
                  ↓
           "다시 시작" 선택
                  ↓
              앱 재시작
                  ↓
           새 버전 설치 완료! ✅
```

### 업데이트 확인 조건

1. **시간 제한**: 마지막 확인 후 24시간 경과
2. **연기 제한**: 최대 3번까지 연기 가능(초과 시 닫기 불가 + "나중에" 비활성화)
3. **Play Store 연동**: Google Play에 배포된 버전만 감지

---

## 테스트 방법

### 1. 내부 테스트 트랙에서 테스트

**단계**:
1. 현재 버전 (1.0.2)을 내부 테스트 트랙에 업로드
2. 테스터에게 배포
3. 테스터가 앱 설치
4. 새 버전 (1.0.3)을 업로드 및 배포
5. 테스터가 앱 실행 → 업데이트 다이얼로그 자동 표시 ✅

### 2. 로컬 테스트 (제한적)

⚠️ **주의**: In-App Update API는 **Play Store를 통해 설치된 앱**에서만 작동합니다.

- 로컬 빌드 (Android Studio 직접 설치)에서는 업데이트 감지 안 됨
- 내부 테스트 트랙에서만 실제 테스트 가능

### 3. 강제 업데이트 테스트

긴급 업데이트가 필요한 경우:

```kotlin
// AppUpdateManager에서 startFlexibleUpdate 대신
appUpdateManager.startImmediateUpdate(appUpdateInfo)
```

- 앱 사용 차단
- 업데이트 완료까지 대기
- 보안 패치, 치명적 버그 수정 시에만 사용

---

## 다음 버전 배포 시 확인사항

### 1. 버전 코드 증가

**app/build.gradle.kts**:
```kotlin
val releaseVersionCode = 2025100802  // 1 증가 ✅
val releaseVersionName = "1.0.3"      // 버전명 업데이트 ✅
```

### 2. AAB 빌드 및 업로드

```bash
# Windows PowerShell
cd G:\Workspace\AlcoholicTimer
.\gradlew bundleRelease
```

### 3. Play Console 배포

1. AAB 업로드
2. **검토 및 출시** 버튼 클릭 (중요!) ⚠️
3. "출시됨" 상태 확인

### 4. 테스터 확인

24시간 이내에:
1. 테스터가 앱 실행
2. 업데이트 다이얼로그 자동 표시 ✅
3. Play 스토어 수동 확인 불필요!

---

## 업데이트 메시지 커스터마이징

### 현재 메시지
```
"새로운 기능과 개선사항이 포함되어 있습니다."
```

### 변경 방법

**StartActivity.kt** 수정:
```kotlin
AppUpdateDialog(
    isVisible = showUpdateDialog,
    versionName = availableVersionName,
    updateMessage = "TopAppBar 간격이 개선되었습니다.", // 여기 수정 ✅
    onUpdateClick = { /* ... */ },
    onDismiss = { /* ... */ }
)
```

### Play Console에서 변경사항 작성

Play Console → 해당 버전 → 출시 노트:
```
• TopAppBar와 선택 박스 간격 개선
• In-App Update 기능 추가
• 버그 수정 및 성능 개선
```

---

## 주의사항

### ✅ 해야 할 것

1. **24시간 제한 존중**
2. **명확한 안내**
3. **사용자 선택권**
   - Flexible 기본, Immediate는 긴급 시에만
   - "나중에"는 항상 표시하고, 연기 불가 시 비활성화 처리

### ❌ 하지 말아야 할 것

1. **과도한 강제**
   - 사소한 업데이트에 Immediate Update 사용 금지
   - 3번 연기 후에도 강제하지 않음

2. **잘못된 정보**
   - 업데이트 내용 과장 금지
   - 거짓 정보로 유도 금지

3. **테스트 환경 혼동**
   - 로컬 빌드에서는 작동 안 함 (정상)
   - 반드시 Play Store 트랙에서 테스트

---

## 정책 준수 확인

✅ **Google Play 정책 100% 준수**:
1. 공식 API 사용 (play-core-ktx)
2. 사용자 선택권 보장 (Flexible Update)
3. 명확한 안내 제공
4. Play Store만 사용 (외부 APK 다운로드 없음)

자세한 내용: `docs/IN_APP_UPDATE_POLICY_REVIEW.md`

---

## 문제 해결

### 문제: 업데이트 다이얼로그가 나타나지 않음

**원인 1**: 24시간이 지나지 않음
- **해결**: 24시간 대기 또는 `forceCheck = true` 사용

**원인 2**: Play Store에서 설치하지 않음
- **해결**: 내부 테스트 트랙에서 설치

**원인 3**: Play Console에서 출시 안 함
- **해결**: "검토 및 출시" 버튼 클릭

### 문제: 로컬 빌드에서 테스트하고 싶음

**불가능**: In-App Update API는 Play Store 전용
- **대안**: 내부 테스트 트랙 사용 (즉시 배포 가능)

### 문제: 업데이트 후에도 이전 버전 표시

**원인**: 앱이 재시작되지 않음
- **해결**: `completeFlexibleUpdate()` 호출 시 자동 재시작

---

## 예상 효과

### Before (이전)
- ❌ 테스터가 Play 스토어 수동 확인 필요
- ❌ 업데이트 알림 없음
- ❌ 업데이트율 낮음 (예상 40-60%)

### After (현재)
- ✅ 앱 실행 시 자동 알림
- ✅ 원터치 업데이트 ("업데이트" 버튼 1번)
- ✅ 업데이트율 증가 (예상 80-95%)
- ✅ 사용자 경험 개선

---

## 다음 단계

### 1. 테스트 버전 배포

현재 버전 (1.0.2)를 내부 테스트에 배포:

```bash
# 1. AAB 빌드
cd G:\Workspace\AlcoholicTimer
.\gradlew.bat clean :app:bundleRelease

# 2. Play Console 업로드
# - 수동으로 app/build/outputs/bundle/release/app-release.aab 업로드
# - "검토 및 출시" 클릭

# 3. 테스터 설치 대기 (1-2시간)
```

### 2. 새 버전 생성 및 테스트

```kotlin
// app/build.gradle.kts
val releaseVersionCode = 2025100802  // 증가
val releaseVersionName = "1.0.3"
```

```bash
# 빌드 및 업로드
.\gradlew bundleRelease
# Play Console에 업로드 및 출시

# 테스터가 앱 실행 → 업데이트 다이얼로그 확인 ✅
```

### 3. 모니터링

Play Console → 통계:
- 업데이트 수락률 확인
- 사용자 피드백 수집
- 충돌 보고서 모니터링

---

## 결론

✅ **In-App Update 기능 구현 완료!**

**핵심 장점**:
1. 테스터가 앱 실행만 하면 업데이트 알림 자동 표시
2. Play 스토어 수동 확인 불필요
3. Google Play 정책 100% 준수
4. 사용자 친화적 UI
5. 업데이트율 대폭 증가 예상

**다음 배포 시**:
1. 버전 코드 증가
2. AAB 업로드
3. "검토 및 출시" 클릭
4. 테스터가 자동으로 업데이트 알림 받음! 🎉

---

## 참고 문서

- `docs/IN_APP_UPDATE_POLICY_REVIEW.md` - 정책 검토
- `docs/TESTER_ROLLOUT_GUIDE.md` - 테스터 배포 가이드
- [Android Developers - In-app updates](https://developer.android.com/guide/playcore/in-app-updates)
