# 정확 프롬프트(복제 앱 반영용)

- 목적: 시작 화면에 업데이트 확인 게이트, 300ms 지연 오버레이, 업데이트 다이얼로그, 데모 모드(롱프레스/인텐트) 추가. 릴리스 빌드에서 데모 완전 비활성화.
- 전제: Kotlin + Jetpack Compose, Play In-App Update 래퍼 AppUpdateManager 및 AppUpdateDialog 사용. 시작 화면은 Activity 또는 Composable 진입점 존재.

---

## 프롬프트 본문

다음 작업을 현재 워크스페이스에 적용해 주세요. 

### 목표 

- 앱 첫 화면(시작/홈) 진입 시, In‑App Update 확인을 gate로 수행한다. 
- forceCheck=false로 설정해 마지막 확인 후 24시간 정책을 따른다. 
- 확인이 300ms 이상 지속될 때만 반투명 오버레이(+Progress, “업데이트 확인 중…”)를 표시한다. 
- 업데이트가 있으면 커스텀 AppUpdateDialog를 띄우고, 사용자가 ‘업데이트’를 누르면 Flexible(미룰 수 있음) 또는 Immediate(최대 미루기 초과 시) 플로우를 시작한다. 
- Flexible 다운로드 완료 시 스낵바로 “다시 시작” 액션을 제공해 설치를 완료한다. 
- ‘나중에’ 버튼은 항상 표시하되, 연기 불가 상태(canDismiss=false)에서는 비활성화(enabled=false)로 처리한다(숨기지 않음).

### 데모 모드 
- BuildConfig.DEBUG일 때만 활성화. 
- 트리거 A: 시작 화면의 지정된 타이틀 텍스트(없으면 최상단 제목 Text)에 long-press 리스너를 달아 오버레이(600ms) 후 가짜 버전(예: 2025101001)으로 업데이트 다이얼로그를 띄운다. 
- 트리거 B: 인텐트 extra `demo_update_ui=true` 로 앱 시작 시 자동 시연.
- 데모 모드에서는 실제 업데이트를 시작하지 않고 토스트/스낵바만 안내. 
- 릴리스(Release) 빌드에서는 데모 모드 완전 비활성화(롱프레스/인텐트 모두 무시). 

### 구현 지시 

1) 시작 Activity(또는 해당 진입 Composable)를 찾아 아래 상태/이펙트/오버레이/다이얼로그/스낵바 로직을 추가한다. 
- 상태: isCheckingUpdate, showUpdateDialog, updateInfo, availableVersionName, snackbarHostState, showOverlay. 
- 게이트: 업데이트 확인/다이얼로그 중에는 다음 화면 자동 이동 금지. 
- 오버레이: 확인 시작 후 300ms 지연 노출, 다이얼로그 표시 시 숨김, 터치 차단(clickable with indication=null, MutableInteractionSource()). 
- LaunchedEffect로 checkForUpdate(forceCheck=false) 호출. onUpdateAvailable에서 다이얼로그 표시, onNoUpdate에서 게이트 해제. 
- 설치 상태 리스너 등록(registerInstallStateListener) 후 Flexible 완료 시 스낵바(Indefinite)와 ‘다시 시작’ 액션으로 completeFlexibleUpdate() 호출. onDispose에서 해제.

2) 데모 모드 
- DEBUG에서만 활성화: val demoActive = BuildConfig.DEBUG && (intent.getBooleanExtra("demo_update_ui", false) || 롱프레스 트리거) 
- 롱프레스: 제목 Text의 Modifier에 combinedClickable을 부착. Compose 버전에 맞춰 필수 파라미터(interactionSource, indication, onClick, onLongClick) 모두 지정. 
- demoActive일 때는 실제 checkForUpdate를 건너뛰고, 600ms 지연 후 가짜 버전으로 다이얼로그 표시. ‘업데이트’ 버튼은 실제 업데이트 시작 대신 스낵바만. 

3) AppUpdateDialog 수정(중요)
- ‘나중에’ 버튼을 항상 표시하고, 매개변수 `canDismiss:Boolean`을 받아 `enabled = canDismiss`로 연결한다. 
- 다이얼로그 바깥 터치(onDismissRequest)는 `canDismiss==true`일 때에만 닫히도록 유지한다.
- 호출부(Start/홈 화면)에서는 `canDismiss = !appUpdateManager.isMaxPostponeReached() || demoActive`를 전달한다.

4) 문자열 리소스 추가: `checking_update` = "업데이트 확인 중..." 

5) 기존 업데이트 호출부가 있다면 forceCheck=true를 모두 제거하고 forceCheck=false로 통일. 

6) 규칙 
- Release 빌드: 데모 트리거/인텐트 완전 무시. 
- Flexible/Immediate 선택 규칙: 최대 미루기 초과 시 Immediate 허용이면 Immediate, 아니면 Flexible. 
- ‘나중에’는 숨기지 않고 비활성화로 노출한다(정책에 따라 UX 투명성 확보).

7) 변경 후 컴파일/런 스모크 확인. 

### 버전 표기 개선(필수) – 최소 단계 프롬프트
- 목적: 다이얼로그에 `availableVersionCode()`(예: 2025101001)가 아닌 사용자용 버전명(예: 1.0.7) 표시

Step 1. 매핑 유틸 추가
- 파일 생성: `core/util/UpdateVersionMapper.kt`
- 내용: `object UpdateVersionMapper { private val map = mapOf(2025101001 to "1.0.7"); fun toVersionName(code:Int)=map[code] }`
- 운영: 새 릴리스마다 `versionCode -> versionName` 쌍을 최신부터 추가

Step 2. 시작 화면 적용
- `onUpdateAvailable(info)`에서 `val code = info.availableVersionCode()` 후 `availableVersionName = UpdateVersionMapper.toVersionName(code) ?: code.toString()`로 설정
- 데모 모드에서도 동일 매핑 사용(가짜 코드 → 버전명)

Step 3. 테스트
- 디버그 데모: `adb shell am start -n <패키지>/<메인액티비티> -e demo_update_ui true`
- 다이얼로그에 `버전 1.0.x` 형태로 표기되는지 확인(코드값이 보이면 매핑 누락)

### SDK 30 호환 패치(필수) – 최소 단계 프롬프트
- 목적: Android 11(API 30) 기기에서 In‑App Update 사용 시 크래시 방지

Step 1. AndroidManifest 패키지 가시성 선언
- `app/src/main/AndroidManifest.xml` 최상단 `<manifest>` 바로 아래에 추가:
  - `<queries><package android:name="com.android.vending"/></queries>`

Step 1-b. Pre‑Android 12 스플래시 테마 보강
- `res/values*/themes.xml`의 `Theme.AlcoholicTimer.Splash`에 다음 추가:
  - `<item name="splashScreenIconSize">240dp</item>`
- 대상 파일: `values/themes.xml`, `values-v23/themes.xml`, `values-v29/themes.xml`

Step 1-c. 스플래시 설치는 런처 액티비티에서만
- 공통 베이스 액티비티에서 `installSplashScreen()` 호출 금지
- 런처 액티비티의 `onCreate()` 초반에서만 `installSplashScreen()` 호출
- Manifest에서 런처 액티비티에만 스플래시 테마 적용(`Theme.AlcoholicTimer.Splash`), 다른 액티비티는 일반 테마

Step 2. Play Store 존재 가드 추가(AppUpdateManager)
- 체크 전 `hasPlayStore()`로 `com.android.vending` 존재 확인 → 없으면 `onNoUpdate()`로 스킵
- 모든 예외는 크래시 대신 `onNoUpdate()`로 다운그레이드 처리

Step 3. 테스트
- Play 존재 확인: `adb shell pm list packages | grep com.android.vending`
- 앱 실행: SDK 30 기기에서 런처/내부 화면 전환 시 크래시가 없어야 함(Play 미탑재 기기는 업데이트 다이얼로그 미노출이 정상)

### 산출물 
- 수정된 시작 화면 파일과 관련 import. 
- 수정된 `AppUpdateDialog`(‘나중에’ 버튼 항상 노출 + enabled 바인딩). 
- 새 문자열 리소스. 
- 간단 ADB 명령: adb shell am start -n <패키지>/<메인액티비티> -e demo_update_ui true - 테스트 시나리오 요약. 

### 주의 
- Modifier.combinedClickable 사용 시 interactionSource/indication 누락 금지. 
- 오버레이는 300ms 지연 노출로 깜빡임 방지.
