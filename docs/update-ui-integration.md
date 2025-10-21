# Update UI Integration Guide

본 문서는 시작 화면에 In‑App Update 게이트, 300ms 지연 오버레이, 업데이트 다이얼로그, 디버그 전용 데모 모드를 통합하는 방법을 설명한다.

## 전제
- Kotlin + Jetpack Compose
- `AppUpdateManager`(Play In‑App Update 래퍼), `AppUpdateDialog` 이미 존재
- 시작 화면 진입점: Activity 또는 Composable

## 목표
- 앱 첫 진입 시 업데이트 확인을 gate로 수행(자동 네비게이션/자동 복구 잠시 보류)
- 300ms 이상 지연 시에만 반투명 오버레이와 Progress 표시
- 업데이트 가능 시 커스텀 다이얼로그 표시, 사용자의 ‘업데이트’에 따라 Flexible/Immediate 플로우 시작
- Flexible 다운로드 완료 시 스낵바로 ‘다시 시작’ 액션 제공
- 데모 모드: DEBUG 빌드에서만 롱프레스/인텐트로 시연, Release에서는 완전 비활성화

## 상태와 게이트
- 상태: `isCheckingUpdate`, `showUpdateDialog`, `updateInfo`, `availableVersionName`, `snackbarHostState`, `showOverlay`
- 게이트: `gateNavigation = isCheckingUpdate || showUpdateDialog`  
  게이트가 내려갈 때까지 다음 화면 자동 이동 금지

## 업데이트 체크
- `checkForUpdate(forceCheck = false)` 호출
- `onUpdateAvailable(info)`: `updateInfo`/`availableVersionName` 업데이트 후 다이얼로그 표시
- `onNoUpdate()`: 게이트 해제
- 설치 상태 리스너: `registerInstallStateListener { ... }`  
  Flexible 완료 시 스낵바(Indefinite)로 ‘다시 시작’ 버튼 제공 → `completeFlexibleUpdate()`
- 수명 종료 시 `unregisterInstallStateListener()`

## 300ms 지연 오버레이
- 확인 시작 시 즉시 표시하지 말고 300ms 지연 후 표시
- 다이얼로그가 표시될 때는 오버레이 숨김
- 터치 차단: `clickable(indication = null, interactionSource = MutableInteractionSource())`

## 데모 모드(디버그 전용)
- 활성화 조건: `BuildConfig.DEBUG`
- 트리거
  - 롱프레스: 제목 `Text`의 `Modifier.combinedClickable(...)`에 `interactionSource`, `indication`, `onClick`, `onLongClick` 모두 지정
  - 인텐트: `demo_update_ui=true`
- 동작
  - 실제 `checkForUpdate` 생략
  - 약 600ms 지연 후 가짜 버전 코드(예: `2025101001`)를 사용하되, 사용자에게는 매핑된 버전명(예: `1.0.7`)으로 표시
  - 다이얼로그의 ‘업데이트’ 버튼은 실제 플로우 대신 스낵바/토스트만
- Release 빌드에서는 롱프레스/인텐트 모두 무시

## Flexible/Immediate 선택 규칙
- 최대 미루기 초과 및 Immediate 허용 시 → Immediate
- 그렇지 않으면 Flexible

## 버전 표기 전략(중요)
- Play In‑App Update API는 대상 버전의 `versionName`을 직접 제공하지 않고 `availableVersionCode()`만 제공합니다.
- 사용자 노출에는 앱 정보(예: 1.0.7)와 동일한 버전명을 보여줘야 하므로, 코드→이름 매핑을 추가합니다.
- 구현: `core/util/UpdateVersionMapper.kt` 에 릴리스별 매핑을 유지하고, 다음과 같이 사용합니다.
  - 실제 체크: `availableVersionName = UpdateVersionMapper.toVersionName(code) ?: code.toString()`
  - 데모 모드: 가짜 코드(예: `2025101001`)를 동일 방식으로 매핑해 표기
- 운영 규칙: 각 릴리스 시 `UpdateVersionMapper`에 `versionCode -> versionName` 쌍을 최신부터 추가하세요. 미매핑 시 폴백으로 코드 문자열을 노출합니다.

## 문자열 리소스
- `res/values/strings.xml`에 다음 추가:
  - `checking_update` = "업데이트 확인 중..."

## 예시 흐름(요약)
- 진입 시 상태 초기화 → `checkForUpdate(false)`
- 300ms 경과 시점에도 확인 중이면 오버레이 노출
- 업데이트 있음 → 다이얼로그 표시(표시 버전: `1.0.x`)
- ‘업데이트’ 클릭 → 규칙에 따라 Flexible/Immediate 시작
- Flexible 다운로드 완료 → 스낵바 ‘다시 시작’ → `completeFlexibleUpdate()`

## 테스트
- 디버그 데모 시연(ADB):
  - `adb shell am start -n <패키지>/<메인액티비티> -e demo_update_ui true`
  - 다이얼로그에 `버전 1.0.x` 형태로 표기되는지 확인(코드값이 보이면 매핑 누락)
- 수동 롱프레스: 시작 화면 상단 제목을 길게 눌러 데모 트리거
- Release 빌드: 데모 트리거 및 인텐트가 무시되는지 확인
- 정상 케이스: 업데이트 없음 → 오버레이가 300ms 미만이면 미표시, 게이트 해제 후 정상 네비게이션

## 주의사항
- `forceCheck=true` 사용 금지. 모든 호출은 `forceCheck=false`로 통일
- `Modifier.combinedClickable` 사용 시 `interactionSource`/`indication` 누락 금지
- 오버레이는 300ms 지연으로 깜빡임 방지
- 스낵바/다이얼로그는 동일한 `SnackbarHostState`/스코프에서 관리
- 버전 표기 매핑은 릴리스마다 갱신 필요(미갱신 시 코드값 노출)

## SDK 30 호환성 패치(중요)
안드로이드 11(API 30) 기기에서 In‑App Update 사용 시 다음을 적용해 크래시를 방지합니다.

1) 패키지 가시성 선언(AndroidManifest.xml)
- <queries> 아래에 Play Store 패키지 선언:
  - com.android.vending
- 목적: SDK 30+에서 패키지 조회 허용(미선언 시 NameNotFoundException 등 유발)

1-b) Pre‑Android 12 스플래시 테마 보강(values/values-v23/values-v29)
- `Theme.AlcoholicTimer.Splash`에 다음 속성 추가:
  - `<item name="splashScreenIconSize">240dp</item>`
- 이유: core-splashscreen의 `splash_screen_view.xml`이 API 31 미만에서 이 속성을 참조하며, 누락 시 InflateException 발생 가능

1-c) 스플래시 설치는 런처 액티비티에서만
- 공통 베이스(예: `BaseActivity`)에서 `installSplashScreen()`을 호출하지 말고, 런처 액티비티의 `onCreate()` 초반에만 호출
- Manifest에서 런처 액티비티에만 `Theme.AlcoholicTimer.Splash` 적용, 그 외 액티비티는 일반 테마 사용

2) Play Store 존재 가드(AppUpdateManager)
- 체크 전 hasPlayStore(context)로 com.android.vending 존재 여부 확인
- 없으면 In‑App Update 스킵(onNoUpdate())으로 degrade
- 예외 처리: Play Core 미가용/네트워크 예외 등은 모두 onNoUpdate()로 처리(앱 크래시 금지)

3) 정책
- Immediate는 “최대 미루기 초과 && Immediate 허용”에서만 사용(기존 정책 유지)
- 그 외는 Flexible 우선

4) 테스트(실기/에뮬레이터)
- Play 설치 유무 확인: `adb shell pm list packages | grep com.android.vending`
- 앱 실행 후: 런처 진입 및 내부 화면(예: Records) 전환 시 크래시 없음 확인
- Play 미탑재 기기에서는 업데이트 다이얼로그가 뜨지 않는 것이 정상 동작

## 변경 산출물 체크리스트
- 시작 화면 파일 수정 및 관련 import 추가
- `UpdateVersionMapper.kt` 생성/갱신
- AndroidManifest `<queries>`에 `com.android.vending` 추가
- `AppUpdateManager`에 Play Store 가드 및 예외 다운그레이드 처리
- 문자열 리소스 `checking_update` 추가
- QA 스모크: 빌드/실행/데모/실 플로우/릴리스 무시 동작 + 버전명 표기 + SDK30 크래시 없음 확인
