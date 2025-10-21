# In‑App Update 데모/다이얼로그/스플래시 트러블슈팅

최종 수정일: 2025-10-14
담당: 앱 시작(Start) 화면, In‑App Update(Play Core), Compose UI

## TL;DR
- Pixel 4a(API 30) 등 API 30 이하에서 업데이트 다이얼로그 전에 스플래시가 한 번 더 보인 원인은 테마 windowBackground(스플래시 아이콘)와 Compose 오버레이(임시 스플래시)가 연달아 노출되던 중복 구조였기 때문입니다.
- 해결: API<31 경로에서 Compose 오버레이/지연을 제거하고, setContent 직전 흰 배경으로 덮은 뒤 첫 프레임 직후 windowBackground를 제거하는 방식으로 이중 노출을 없앴습니다.
- 컴파일 오류(@Composable 호출 위치)는 stringResource(...)를 코루틴 블록에서 호출한 것이 원인으로, Composable 스코프에서 미리 평가하여 해결했습니다.

## 영향 범위
- 파일: `app/src/main/java/com/example/alcoholictimer/feature/start/StartActivity.kt`
- 리소스: `app/src/main/res/values/strings.xml`
- 문서: 본 파일, `CHANGELOG.md`

## 증상 상세
- API 30 기기(예: Pixel 4a)에서 “목표 기간 설정” 흐름 중 업데이트 체크/다이얼로그 앞뒤로 스플래시(아이콘)가 다시 보이는 체감 현상 발생.
- 일부 시나리오에서 @Composable invocations 오류 발생(코루틴 내 stringResource 호출).

## 원인 분석
- 스플래시 중복: API 30 이하 경로에서
  1) 테마 windowBackground(스플래시 아이콘)가 노출됨
  2) Compose 임시 스플래시 오버레이(최소 표시시간/페이드)가 추가로 노출됨
  → 두 레이어가 순차로 나타나며 “한 번 더 보이는” 현상 유발.
- @Composable 오류: `stringResource(...)` 호출이 코루틴 내부(Composable 컨텍스트 바깥)에서 발생.

## 적용한 해결책 (요약)
- API<31 경로
  - Compose 오버레이(usesComposeOverlay)를 비활성화.
  - setContent 지연 제거(즉시 렌더).
  - setContent 전 `AndroidColor.WHITE.toDrawable()`로 창 배경 덮고, 첫 프레임 직후 `window.setBackgroundDrawable(null)`로 제거.
- 문자열 리소스 호출
  - `val restartPromptText = stringResource(R.string.update_downloaded_restart_prompt)`
  - `val actionRestartText = stringResource(R.string.action_restart)`
  - 위 값을 Composable 스코프에서 미리 평가 후 코루틴에서 사용.
- 데모/실사용 흐름 정리
  - Start 화면에서 `AppUpdateDialog`를 실제 렌더링.
  - 데모 트리거: 제목 탭/롱프레스, 또는 인텐트 `demo_update_ui=true`.
  - 실사용: `AppUpdateManager.checkForUpdate()` → 다이얼로그 → Flexible Update.

## 변경 포인트 (핵심 스니펫 수준)
- `StartActivity.onCreate()`
  - API<31: 오버레이 꺼짐, setContent 즉시, windowBackground 즉시/지연 제거 병행.
- `StartScreenWithUpdate(...)`
  - `usesComposeOverlay` 파라미터 반영.
  - `restartPromptText`, `actionRestartText`를 Composable에서 미리 평가.
  - 데모/실사용 onUpdateClick 분기(데모는 스낵바 시연, 실사용은 Flexible Update 시작).

## QA 체크리스트
1) 공통
- 앱 cold start 시 깜빡임/아이콘 재노출 여부 관찰.
- 업데이트 감지 시 다이얼로그 표시/해제 동작.
- 스낵바(다운로드 완료) 노출/Action 처리.

2) API 30 (Pixel 4a)
- 스플래시가 업데이트 다이얼로그 전·후로 다시 보이지 않는지 확인.
- 제목 탭/롱프레스로 데모 다이얼로그 즉시 노출 확인.

3) API 31+
- 시스템 SplashScreen만 보이고 Compose 오버레이가 보이지 않는지 확인.

4) 데모 모드
- 제목 탭/롱프레스 시 다이얼로그 표시.
- 다음 ADB 명령으로 자동 시연:
```
adb shell am start -n com.example.alcoholictimer/.feature.start.StartActivity -e demo_update_ui true
```

5) 실사용 모드 (Play Store 있는 기기)
- 업데이트 감지 → 다이얼로그 → 업데이트(다운로드) → 스낵바 “다시 시작” → 설치 완료.

## 자주 하는 실수 / 주의사항
- 코루틴 내부에서 `stringResource(...)` 호출 금지(Composable 스코프에서 미리 평가).
- API<31에서 windowBackground 제거 타이밍 누락 시 깜빡임/잔상 가능.
- 데모 전용 트리거(디버그 빌드 한정)와 실사용 흐름 혼동 주의.

## 롤백/토글 가이드
- API<31에서도 최소 오버레이 연출이 필요할 경우:
  - `usesComposeOverlay = true`로 켜되, 최소 표시시간은 0 또는 매우 짧게 설정해 중복 체감을 최소화.
  - windowBackground 제거 타이밍(첫 프레임 직후) 유지 필수.

## 관련 문서 / 이력
- `docs/IN_APP_UPDATE_IMPLEMENTATION.md` (기존 설계/구현 가이드)
- `CHANGELOG.md` (Unreleased 섹션에 요약 기록)

## 부록: 디버깅 팁
- 로그캣 태그: AppUpdateManager (업데이트 체크/진행/상태 리스너)
- 재현 전 초기화:
```
adb shell pm clear com.example.alcoholictimer
adb uninstall com.example.alcoholictimer
```
- 설치 후 데모 트리거로 빠른 시연:
```
adb shell am start -n com.example.alcoholictimer/.feature.start.StartActivity -e demo_update_ui true
```

