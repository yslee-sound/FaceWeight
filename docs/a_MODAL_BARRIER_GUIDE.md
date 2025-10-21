> 문서 버전
> - 버전: v1.0.0
> - 최근 업데이트: 2025-10-21
> - 변경 요약: 완전한 모달 배리어 가이드 초안(목표/수용 기준/구현 개요/코드 위치/QA 체크리스트/트러블슈팅 포함)
>
> 변경 이력(Changelog)
> - v1.0.0 (2025-10-21)
>   - 초기 작성: 드로어 입력 가드와 다이얼로그 닫힘 정책을 정리
>   - 상태 구독(snapshotFlow)과 AndroidView 오버레이, semantics 차단 스니펫 수록
>   - QA 체크리스트와 트러블슈팅 팁 추가
>
> 버전 규칙
> - Semantic Versioning 준수: MAJOR(호환성 깨짐)/MINOR(가이드·정책 추가)/PATCH(오타·경미한 정정)
> - 문서 갱신 시 상단 버전/날짜/요약, 하단 변경 이력을 함께 갱신합니다.

# 완전한 모달 배리어 가이드 (Jetpack Compose)

이 문서는 드로어(ModalNavigationDrawer)와 다이얼로그(Dialog)에서 "완전한 모달 배리어"를 구현하고 검증하는 방법을 정리합니다. 본 레포지토리의 구현 상태와 코드 위치를 기준으로 설명합니다.

- 적용 버전: Compose Material3 기반, AlcoholicTimer(NoSmokeTimer) Android 앱
- 주요 변경 파일:
  - `app/src/main/java/com/sweetapps/nosmoketimer/core/ui/BaseActivity.kt`
  - `app/src/main/java/com/sweetapps/nosmoketimer/core/ui/components/AppUpdateDialog.kt`

## 목표
- 드로어/모달이 열려 있거나 애니메이션 중일 때 배경 상호작용(탭, 더블탭, 롱프레스, 스크롤, 플링)을 전혀 허용하지 않습니다.
- 스크림(scrm) 탭으로 닫기가 허용된 경우 닫히되, 그 탭이 배경의 어떤 액션(onClick/onLongClick/scroll)도 트리거하지 않습니다.
- 닫힌 직후 짧은 그레이스 기간(기본 200ms) 동안 입력을 계속 소비해 click-through를 방지합니다.
- 접근성 활성화 상태에서 드로어/모달이 열려 있는 동안 배경 포커스 이동(탐색)이 불가합니다.

## 수용 기준 (Acceptance Criteria)
- 스크림 탭 시 드로어는 닫히지만, 배경의 onClick/onLongClick/scroll 콜백은 호출되지 않습니다.
- 애니메이션 중 및 닫힘 직후 그레이스 기간 동안 입력이 배경으로 절대 누수되지 않습니다.
- TalkBack 등 접근성 사용 시, 드로어/모달이 열려 있는 동안 배경 요소에 포커스가 가지 않습니다.

## 구현 개요
### 1) 드로어 입력 가드(모달 배리어)
- DrawerState의 `isAnimationRunning / currentValue / targetValue`를 `snapshotFlow`로 관찰합니다.
- 조건: 열림 상태이거나 애니메이션 중이면 가드를 활성화합니다. 닫힘 도달 직후에도 200ms 그레이스 후 비활성화합니다.
- 가드 활성 시, 콘텐츠 계층(content slot)에 전체 화면 오버레이를 추가하여 모든 입력을 소비합니다.
- 접근성: `clearAndSetSemantics {}`로 해당 오버레이 자체에 의미를 없애 배경 포커스 이동을 차단합니다.
- 주의: 오버레이는 `ModalNavigationDrawer`의 content 계층 안에 두어 스크림 위에 그려지지 않도록 합니다. 이 레이아웃에서 스크림 탭은 정상적으로 드로어를 닫으면서, 배경과의 상호작용은 차단됩니다.

권장 스니펫(pointerInput 기본형):
```kotlin
if (drawerInputGuardActive /* || globalInputLocked */) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { }
            .pointerInput(drawerInputGuardActive) {
                while (true) {
                    awaitPointerEventScope {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    )
}
```

본 레포 실제 적용(AndroidView 대체):
```kotlin
if (drawerInputGuardActive) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { },
        factory = { context ->
            android.view.View(context).apply {
                isClickable = true
                isFocusable = true
                setOnTouchListener { _, _ -> true } // 모든 MotionEvent 소비
            }
        }
    )
}
```

이 방식은 Compose 포인터 API 버전 호환성 이슈 없이 모든 터치를 안정적으로 소비합니다.

상태 구독(요약):
```kotlin
var drawerInputGuardActive by remember { mutableStateOf(false) }
val drawerGuardGraceMs = 200L
LaunchedEffect(drawerState) {
    snapshotFlow { Triple(drawerState.isAnimationRunning, drawerState.currentValue, drawerState.targetValue) }
        .collect { (isAnimating, current, target) ->
            if (isAnimating || target != DrawerValue.Closed || current != DrawerValue.Closed) {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                drawerInputGuardActive = true
            } else {
                drawerInputGuardActive = true
                delay(drawerGuardGraceMs)
                drawerInputGuardActive = false
            }
        }
}
```

### 2) 다이얼로그 닫힘 정책
- 정책 변수 `canDismiss`에 따라 `DialogProperties`를 설정합니다.
  - `dismissOnBackPress = canDismiss`
  - `dismissOnClickOutside = canDismiss`
- `onDismissRequest`에서도 동일 정책으로 분기하여 실제 닫힘만 허용하거나 차단합니다.

핵심 로직(요약): AppUpdateDialog.kt
```kotlin
Dialog(
    onDismissRequest = { if (canDismiss) onDismiss() },
    properties = DialogProperties(
        dismissOnBackPress = canDismiss,
        dismissOnClickOutside = canDismiss
    )
) { /* content */ }
```

## 코드 위치
- 드로어 모달 배리어: `BaseActivity.kt`
  - 입력 가드 오버레이: AndroidView 기반 전체 화면 View(모든 터치 소비) + `clearAndSetSemantics { }`
  - 상태 구독(`snapshotFlow`) 및 그레이스 타임: `LaunchedEffect` 블록
- 다이얼로그 정책: `AppUpdateDialog.kt`
  - `DialogProperties`와 `onDismissRequest`를 `canDismiss`와 동기화

## QA 체크리스트
- 개발자 옵션에서 Show taps, Pointer location 활성화 후 아래 시나리오 수행 시 배경 반응이 없어야 합니다.
  - 단일 탭, 빠른 더블탭, 롱프레스, 느린 탭(Up 지연), 수평/수직 스크롤, 플링
- 스크림 탭으로 닫기: 닫힌 뒤 약 200ms 이내에 배경 클릭/스크롤이 발생하지 않아야 합니다.
- 스크롤 가능한 화면(리스트/지도 등)에서도 드로어/모달이 열려 있는 동안 배경 움직임 없음.
- 접근성(TalkBack) 활성화 시, 드로어/모달 열림 동안 배경 포커스 이동 불가.
- `canDismiss=false` 다이얼로그: 백 버튼 및 바깥 탭으로 닫히지 않음.

## 트러블슈팅 & 팁
- Compose 포인터 API 버전 차이로 `awaitPointerEventScope` 사용이 곤란한 경우, 본 레포처럼 AndroidView 대체 방식을 사용하세요.
- 스크림 탭으로 닫기 동작이 먹지 않는 경우는 오버레이 계층이 스크림보다 위에 올라간 상황일 수 있습니다. 오버레이는 반드시 `ModalNavigationDrawer`의 content 내부(스크림 아래)에 두세요.
- 포커스/IME: 드로어 열림 시작 시 `clearFocus()` 및 `keyboardController.hide()` 호출로 IME를 내리고 포커스를 비워 의도치 않은 입력을 방지합니다.

## 변경 이력 (본 레포)
- Unreleased
  - Added: 본 문서 `docs/MODAL_BARRIER_GUIDE.md` 추가
  - Fixed: 드로어 배경 상호작용 차단 강화를 위한 입력 가드 오버레이 적용(AndroidView 기반 + semantics 차단), 닫힘 직후 200ms 그레이스 포함
  - Changed: 앱 업데이트 다이얼로그에 `DialogProperties`를 `canDismiss`에 맞춰 명시

## 참고
- Material3 ModalNavigationDrawer, Dialog (Jetpack Compose)
- 본 가이드는 앱 구조와 파일 배치에 최적화되어 있으며, 다른 프로젝트에 이식 시 레이아웃 계층 및 상태 스코프를 재점검하세요.
