> 문서 버전
> - 버전: v1.2.1
> - 최근 업데이트: 2025-10-21
> - 변경 요약: 세 화면(기록/전체기록/레벨/설정)의 하단 여백을 navBarBottom 사용에서 상단과 동일한 16dp 고정으로 전환
>
> 변경 이력(Changelog)
> - v1.2.1 (2025-10-21)
>   - 하단 여백 정책 변경: Records/AllRecords/Level/Settings 화면의 하단 여백을 navBarBottom → 16dp(상단과 동일)로 통일
>   - 예외 유지: 하단 고정 버튼 화면(StandardScreenWithBottomButton)과 일부 CTA가 있는 세부 화면(예: DetailActivity)은 IME/네비 인셋을 고려한 가변 하단 여백을 그대로 사용
> - v1.2.0 (2025-10-20)
>   - Safe Area 규약 섹션 추가(전역 하단 인셋 금지, 수평 safeDrawing, 화면별 국소 적용 원칙)
>   - 하단 여백 최소화: 금연 기록(Records/AllRecords), 금연 레벨(Level), 설정(Settings) 화면의 여분 dp 제거(navBarBottom만 사용)
>   - AI 에이전트용 적용 프롬프트(한국어) 추가로 자동화 적용성 강화
> - v1.1.0 (2025-10-20)
>   - 전역 BaseScreen 인셋 정책: 상단(statusBars)만 AppBar에 적용, 루트는 수평(safeDrawing Horizontal)만 적용(하단 제외)
>   - ModalNavigationDrawer: 드로어 오픈 시 즉시 포커스 해제+키보드 숨김, 시트에 status/navigation bars 패딩 적용, 입력가드+그레이스 타임 반영
>   - 하단 고정 버튼 화면: IME/네비 하단 인셋 중 큰 값으로 버튼/콘텐츠 간 이격 계산
>   - Records 빈 상태 명칭 충돌 해결, Nickname 편집 화면 imePadding 적용
> - v1.0.0 (초안)
>   - Insets/IME 처리 가이드 초안 정리
>
> 버전 규칙
> - Semantic Versioning 준수: MAJOR(호환성 깨짐)/MINOR(기능·정책 추가)/PATCH(오타·경미한 정정)
> - 문서 갱신 시 상단 버전/날짜/요약, 하단 변경 이력을 함께 갱신합니다.

# Window Insets & IME 처리 가이드

목표: 3버튼/제스처 내비게이션과 다양한 키보드(IME) 환경에서 하단 여백 과다/겹침 없이 안정적인 레이아웃을 유지한다.

이 문서는 AlcoholicTimer 앱에 적용한 실제 수정 내역과, 동일한 베이스를 사용하는 다른 앱에 확장 적용할 수 있는 정책/체크리스트/스니펫을 제공한다.

---

## 1) 이번 리포지토리에 적용한 변경 내역 요약

변경 목적: 3버튼 내비 기기에서 "하단 여백 과도" 문제 제거, 입력 화면에서 키보드가 버튼/콘텐츠를 가리는 문제 예방.

핵심 원칙
- 전역(root 컨테이너)에서는 하단(bottom) safe area를 강제로 추가하지 않는다.
- 수평(horizontal) 인셋만 전역 적용하고, 상단(statusBars)은 AppBar에만 한정 적용한다.
- 입력 화면은 IME(키보드) 등장 시 겹침이 없도록 `imePadding()` 또는 IME 하단 인셋을 고려한 계산을 사용한다.

구체 수정(최신)
- app/src/main/java/com/sweetapps/nosmoketimer/feature/records/components/AllRecords.kt
  - 목록 하단 contentPadding을 navBarBottom → 16dp로 변경.
- app/src/main/java/com/sweetapps/nosmoketimer/feature/records/components/RecordsScreen.kt
  - 목록 하단 contentPadding을 navBarBottom → 16dp로 변경.
- app/src/main/java/com/sweetapps/nosmoketimer/feature/level/LevelActivity.kt
  - 루트 Column 하단 Spacer를 navBarBottom → 16dp로 변경.
- app/src/main/java/com/sweetapps/nosmoketimer/feature/settings/SettingsActivity.kt
  - 루트 Column 하단 padding을 navBarBottom → 16dp로 변경.
- 예외(유지):
  - app/src/main/java/com/sweetapps/nosmoketimer/core/ui/StandardScreen.kt
    - 하단 고정 버튼 화면: IME/네비 중 큰 값을 사용하는 가변 하단 여백 로직 유지.
  - app/src/main/java/com/sweetapps/nosmoketimer/feature/detail/DetailActivity.kt
    - CTA/내용 겹침 방지를 위해 navBarBottom 기반 하단 여백 유지(상황에 따라 가변).

빌드/검증
- Debug Kotlin 컴파일 성공. 정책 변경 후에도 타입/문법 오류 없음.

---

## 2) 정책 및 베스트 프랙티스

- Edge-to-Edge 기본 설정
  - `WindowCompat.setDecorFitsSystemWindows(window, false)`를 onCreate에서 한 번만 설정.
- 전역 인셋 정책
  - 상단: AppBar(TopAppBar) 컨테이너에만 `WindowInsets.statusBars` 적용.
  - 하단: 전역 루트에서는 적용하지 않음. 일반 리스트/카드 화면의 하단 여백은 상단과 동일한 16dp 고정 사용.
  - 수평: 전역 루트 컨테이너에 `WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)` 적용.
- 리스트/상세 화면(일반)
  - 루트에 하단 safeDrawing 금지. 스크롤 컨텐츠는 자체 패딩으로 해결(하단 16dp 권장).
- 입력 화면(키보드 등장)
  - 단순: 루트 컨테이너에 `imePadding()`.
  - 하단 고정 버튼이 있는 화면: 네비게이션 바 vs IME 하단 인셋 중 큰 값 사용.
- 금지 패턴
  - 전역(모든 화면)에 `systemBarsPadding()` 또는 `safeDrawingPadding()` 하단을 무차별 적용.
  - 같은 화면에서 서로 다른 레벨의 컨테이너가 각각 하단 인셋을 중복 적용.

### 2-1) 드로어(ModalNavigationDrawer) + IME 정책

상황: TextField에 포커스가 있는 상태에서 드로어를 열면 레이아웃 튐/겹침, 스크림 뒤 배경 입력 누수, 드로어가 키보드에 가려짐 등의 문제가 발생할 수 있다.

권장 정책(라이트 모드/일반 UX 기준)
- 드로어를 여는 순간(버튼 클릭 또는 제스처 시작) 즉시 포커스 해제 + 키보드 숨김.
- 드로어 시트 루트에 `statusBarsPadding()` 및 `navigationBarsPadding()` 적용(시스템 바와 겹침 방지).
- 드로어 애니메이션 시작 프레임부터 전역 입력 가드(overlay) 활성화 → 배경 터치 스루 방지.
- 드로어 닫힌 직후 짧은 그레이스 타임(예: 200ms) 동안 입력 소비로 잔여 탭 스루 예방.

코드 스니펫(Compose, Material3)

```kotlin
val drawerState = rememberDrawerState(DrawerValue.Closed)
val scope = rememberCoroutineScope()
val focusManager = LocalFocusManager.current
val keyboardController = LocalSoftwareKeyboardController.current

// 드로어 상태 변화 감지: 열리기 시작하면 즉시 포커스/키보드 정리 + 입력 가드 on
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

ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ...
        }
    }
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                // 드로어 열기 전에 포커스/키보드 정리
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                scope.launch { drawerState.open() }
            }) { /* ... */ }
        }
    )

    // drawerInputGuardActive가 true�� 때 포인터 이벤트 소비 오버레이를 깔아 클릭 스루 방지
}
```

대안 정책(특수 요구 사항): 키보드를 유지한 채 드로어를 노출해야 한다면, 드로어 컨텐트에 `WindowInsets.ime.asPaddingValues()`를 반영해 하단을 띄우되, 시각적 튐과 공간 부족을 고려할 것(권장 X).

---

## 3) 스니펫 모음

1) 전역 BaseScreen 예시

```
// AppBar
Surface(modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) { ... }

// 콘텐츠 루트(전역)
Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
```

2) 입력 화면(간단)

```
Column(Modifier.fillMaxSize().imePadding()) { ... }
```

3) 하단 고정 버튼이 있는 화면

```
val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
val effectiveBottom = if (navBarBottom > imeBottom) navBarBottom else imeBottom

// 콘텐츠 하단 예약
val buttonSize = 96.dp
val extraGap = 32.dp
val reservedBottom = (buttonSize / 2) + extraGap + effectiveBottom

Column(Modifier.padding(bottom = reservedBottom)) { ... }

// 버튼
Box(Modifier.align(Alignment.BottomCenter).padding(bottom = effectiveBottom + 24.dp)) { ... }
```

4) 리스트 화면(루트 하단 safeDrawing 금지)

```
Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
  LazyColumn(
    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
    // ...
  )
}
```

5) 드로어 + IME 처리

```kotlin
val drawerState = rememberDrawerState(DrawerValue.Closed)
val scope = rememberCoroutineScope()
val focusManager = LocalFocusManager.current
val keyboardController = LocalSoftwareKeyboardController.current

// 드로어 상태 변화 감지: 열리기 시작하면 즉시 포커스/키보드 정리 + 입력 가드 on
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

ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
        ModalDrawerSheet(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ...
        }
    }
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                // 드로어 열기 전에 포커스/키보드 정리
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                scope.launch { drawerState.open() }
            }) { /* ... */ }
        }
    )

    // drawerInputGuardActive가 true일 때 포인터 이벤트 소비 오버레이를 깔아 클릭 스루 방지
}
```

---

## 4) 리포 내 전수 점검 체크리스트

- [ ] `windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom ...))` 사용 제거
- [ ] 전역 루트는 수평만 적용, 하단은 금지
- [ ] AppBar 컨테이너에만 `WindowInsets.statusBars` 적용
- [ ] 입력 화면 루트에 `imePadding()` 적용
- [ ] 하단 고정 버튼 화면에서 IME/네비 비교 후 큰 값 사용
- [ ] 드로어: 오픈 시 포커스 해제 + 키보드 숨김, 드로어 시트에 status/navigation bars 패딩 적용
- [ ] 드로어: 애니메이션 시작부터 입력 가드 활성화, 닫힘 직후 그레이스 타임 소비
- [ ] insets 중복 적용 지점 제거(부모/자식 중복)
- [ ] Debug 빌드 컴파일 확인

검증 커맨드 예시(Windows, Debug Kotlin만 빠르게)

```
./gradlew.bat :app:compileDebugKotlin --console=plain -x lint -x test
```

수동 QA 체크
- 3버튼 기기: 하단 여백 과도 현상 없음
- TextField 포커스 상태에서 드로어 열기: 키보드 즉시 숨김, 드로어가 가려지지 않음, 배경 클릭 스루 없음
- 드로어 닫힘 직후 잘못된 클릭 전달 없음

---

## 5) 다른 앱으로의 이식 절차(요약)

1) 전역 설정 확인: DecorFitsSystemWindows(false) 적용 여부 확인.
2) 전역 BaseScreen 또는 공통 레이아웃에서 하단 safeDrawing 제거, 수평만 유지.
3) AppBar에만 statusBars 인셋 적용.
4) 입력 화면에는 imePadding 적용.
5) 하단 고정 버튼 화면은 IME/네비 합성 하단 여백 사용.
6) 드로어가 있다면: 오픈 시 포커스 해제 + 키보드 숨김, 드로어 시트 패딩, 입력 가드 타이밍 반영.
7) 전수 검색 키워드: `safeDrawing`, `systemBarsPadding`, `windowInsetsPadding`, `imePadding`, `navigationBarsPadding`, `statusBarsPadding`, `ModalNavigationDrawer`, `ModalDrawerSheet`, `DrawerValue.Open`, `LocalSoftwareKeyboardController`.
8) 빌드/스모크 테스트.

---

## 6) 트러블슈팅

- 3버튼 기기에서 여백이 남음: 해당 화면 루트에 Bottom 인셋이 남아있는지 확인(전역/로컬 중복 포함).
- 키보드가 버튼을 가림: 루트에 `imePadding()` 누락 또는 하단 버튼 패딩에서 IME 인셋 미반영.
- 드로어가 키보드에 가려짐/레이아웃 튐: 드로어 오픈 시 포커스/키보드 정리 누락, 드로어 시트 패딩 누락.
- 특정 OEM 키보드에서 늦게 반영: 패딩이 애니메이션 중 갱신될 수 있으므로 실제 기기에서 재확인.

---

## 7) 변경 이력(요약)

- 2025-10-14: 드로어 + IME 안정화 정책 문서화. BaseActivity에 포커스/키보드 정리, 드로어 패딩, 입력 가드 타이밍 반영. 프롬프트 업데이트.
- 2025-10-13: AlcoholicTimer에 전역 인셋 정책 정비, AllRecords 하단 인셋 제거, 하단 버튼/IME 대응 개선, 닉네임 편집 화면 imePadding 추가.
- 2025-10-20: Safe Area 규약 추가 및 하단 여백 최소화 정책 통합(기록/레벨/설정 화면), AI 에이전트용 한국어 프롬프트 제공.
- 2025-10-21: 세 화면(Records/AllRecords/Level/Settings)의 하단 여백 정책을 navBarBottom → 상단과 동일한 16dp로 변경.
