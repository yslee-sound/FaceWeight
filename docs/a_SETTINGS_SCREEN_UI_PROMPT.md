> 문서 버전
> - 버전: v1.0.0
> - 최근 업데이트: 2025-10-21
> - 변경 요약: 설정 화면 복제 프롬프트 템플릿 초안 작성(간격/컴포넌트 스타일/스크롤 정책, 예시 구현/검수 체크리스트/포팅 가이드 포함)
>
> 변경 이력(Changelog)
> - v1.0.0 (2025-10-21)
>   - 초기 작성: 프롬프트 템플릿과 디자인/레이아웃 스펙 정리
>   - 예시 구현 코드, 검수 체크리스트, 포팅 가이드 수록
>
> 버전 규칙
> - Semantic Versioning 준수: MAJOR(호환성 깨짐)/MINOR(가이드·정책 추가)/PATCH(오타·경미한 정정)
> - 문서 갱신 시 상단 버전/날짜/요약, 하단 변경 이력을 함께 갱신합니다.

# 설정 화면(UI) 복제 프롬프트 템플릿

목적: 이 프롬프트를 복사해 다른 앱의 LLM/생성형 워크플로우나 인수인계 문서에서 그대로 사용하면, 현재 앱의 설정 화면과 동일한 "간격, 컴포넌트 스타일, 스크롤 정책"을 재현할 수 있습니다.

---

## 프롬프트(그대로 복사해 사용)

너는 Jetpack Compose(Material 3)를 사용하는 Android UI 엔지니어다. 아래 "절대 준수사항"과 "디자인/레이아웃 스펙"을 만족하는 설정 화면(SettingsScreen)을 구현하라. 화면은 3개의 그룹 카드(금연 비용, 금연 빈도, 금연 시간)로 구성되며, 현재 앱과 시각적/행동적 일치가 목표다.

### 절대 준수사항(Non-negotiable)
- 간격 불변: 그룹 간 간격은 12dp, 상/하 여백은 각각 8dp다. 좌우 여백은 토큰 H_PADDING를 사용하고, 토큰이 없다면 16.dp로 대체한다.
- 스크롤 정책: 콘텐츠가 실제로 뷰포트를 넘칠 때만 세로 스크롤을 허용한다. 넘치지 않으면 스크롤 제스처/오버스크롤 효과가 없어야 한다.
- 오버스크롤: 스크롤 불필요 시 LocalOverscrollFactory = null을 사용해 오버스크롤 이펙트를 끈다.
- 컴포넌트 스타일: 카드 모서리 16dp, 헤어라인 보더, 낮은 카드 elevation, 내부 패딩 12dp, 타이틀은 titleMedium + Bold.
- 접근성/터치 표면: 각 옵션 아이템은 최소 높이 48dp, Row 전체가 포커스/클릭 가능한 영역이며 RadioButton Role을 준수한다.
- 텍스트/컬러: 선택 상태에 따른 타입·컬러 변화를 일관되게 적용한다.

### 디자인/레이아웃 스펙
- 컨테이너
  - Scaffold 상단에 앱바가 있다고 가정. 콘텐츠 루트는 Box.
  - Box Modifier: fillMaxSize() + padding(start = H_PADDING, end = H_PADDING, top = 8.dp, bottom = 8.dp)
- 리스트
  - LazyColumn 사용, verticalArrangement = spacedBy(12.dp), contentPadding = PaddingValues(0.dp)
  - 스크롤 허용 여부는 "실측 기반"으로 판정한다.
    - Box.onSizeChanged로 viewportHeight 저장.
    - 각 카드 아이템 외곽에 Box(onSizeChanged)로 개별 높이 측정.
    - allowScroll = (costH + freqH + durH + 12dp*2) > viewportHeight
    - LazyColumn(userScrollEnabled = allowScroll)
  - allowScroll == false일 때는 CompositionLocalProvider(LocalOverscrollFactory provides null)로 감싼다.
- 카드(Card)
  - shape: RoundedCornerShape(16.dp)
  - elevation: AppElevation.CARD (없으면 2.dp 수준으로 대체)
  - border: BorderStroke(AppBorder.Hairline, color = R.color.color_border_light) (없으면 1dp 약한 중립색)
  - 내부: Column(padding = 12.dp) + 타이틀(Text) 하단 8dp 여백
- 그룹(옵션 묶음)
  - Column(verticalArrangement = spacedBy(6.dp))
- 옵션 아이템
  - Row(fillMaxWidth, heightIn(min=48.dp), clickable(role=RadioButton))
  - RadioButton 색: selected = R.color.color_accent_blue, unselected = R.color.color_radio_unselected
  - 레이블(Text) 스타일: bodyLarge, 선택 시 SemiBold + 강조 컬러(R.color.color_indicator_days), 비선택 시 본문 컬러(R.color.color_text_primary_dark)

### 토큰/리소스 매핑(다른 앱으로 포팅 시)
- H_PADDING -> 프로젝트 좌우 화면 기본 패딩 (권장 16.dp)
- AppElevation.CARD -> 없으면 2.dp
- AppBorder.Hairline -> 없으면 1.dp
- 색 리소스
  - color_indicator_money(타이틀-금연 비용), color_progress_primary(타이틀-금연 빈도), color_indicator_hours(타이틀-금연 시간)
  - color_border_light(카드 보더), color_accent_blue(선택 라디오), color_radio_unselected(비선택 라디오), color_indicator_days(선택 텍스트), color_text_primary_dark(비선택 텍스트)
  - 리소스가 없으면 비슷한 팔레트로 대체한다(예: onSurfaceVariant, primary, tertiary 등).

### 상태/저장(예시)
- SharedPreferences 키 예시: selected_cost, selected_frequency, selected_duration
- 앱 상황에 맞게 DataStore/Repository로 치환 가능(화면 동작엔 영향 없음).

### 품질 게이트
- 빌드/미리보기: Compose Preview와 디버그 빌드가 모두 성공해야 한다.
- 시각 검증: 상단 8dp, 하단 8dp 여백이 보이고, 카드 사이 간격 12dp가 일정해야 한다.
- 동작 검증: 긴 화면에서만 스크롤이 되고, 짧은 화면에선 드래그/바운스가 발생하지 않아야 한다.

---

## 예시 구현(가이드 코드)
> 아래는 구조를 보여주는 참고 코드다. 프로젝트 토큰/리소스 이름에 맞춰 교체하라.

```kotlin
@Composable
fun SettingsScreen() {
    // 상태 준비(예: SharedPreferences)
    val context = LocalContext.current
    var selectedCost by remember { mutableStateOf("중") }
    var selectedFrequency by remember { mutableStateOf("주 1~2회") }
    var selectedDuration by remember { mutableStateOf("보통") }

    // 실측 기반 스크롤 판정
    val density = LocalDensity.current
    val gapPx = with(density) { 12.dp.roundToPx() }
    var viewportH by remember { mutableStateOf(0) }
    var costH by remember { mutableStateOf(0) }
    var freqH by remember { mutableStateOf(0) }
    var durH by remember { mutableStateOf(0) }
    val allowScroll by remember { derivedStateOf { (costH + freqH + durH + gapPx * 2) > viewportH } }
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = H_PADDING, end = H_PADDING, top = 8.dp, bottom = 8.dp)
            .onSizeChanged { viewportH = it.height }
    ) {
        val listContent: @Composable () -> Unit = {
            LazyColumn(
                state = listState,
                userScrollEnabled = allowScroll,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(Modifier.onSizeChanged { costH = it.height }) {
                        SettingsCard(title = "금연 비용", titleColor = colorResource(id = R.color.color_indicator_money)) {
                            SettingsOptionGroup(
                                selectedOption = selectedCost,
                                options = listOf("저", "중", "고"),
                                labels = listOf(
                                    "저 (하루 한갑 이하)", "중 (하루 한갑 정도)", "고 (하루 한갑 이상)"
                                ),
                                onOptionSelected = { selectedCost = it }
                            )
                        }
                    }
                }
                item {
                    Box(Modifier.onSizeChanged { freqH = it.height }) {
                        SettingsCard(title = "금연 빈도", titleColor = colorResource(id = R.color.color_progress_primary)) {
                            SettingsOptionGroup(
                                selectedOption = selectedFrequency,
                                options = listOf("주 1~2회", "주 3~4회", "매일"),
                                labels = listOf("주 1~2회", "주 3~4회", "매일"),
                                onOptionSelected = { selectedFrequency = it }
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.onSizeChanged { durH = it.height }) {
                        SettingsCard(title = "금연 시간", titleColor = colorResource(id = R.color.color_indicator_hours)) {
                            SettingsOptionGroup(
                                selectedOption = selectedDuration,
                                options = listOf("짧음", "보통", "길게"),
                                labels = listOf("짧음 (10분 이하)", "보통 (10분 정도)", "길게 (10분 이상)"),
                                onOptionSelected = { selectedDuration = it }
                            )
                        }
                    }
                }
            }
        }

        if (allowScroll) listContent() else CompositionLocalProvider(LocalOverscrollFactory provides null) { listContent() }
    }
}

@Composable
fun SettingsCard(title: String, titleColor: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = titleColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsOptionGroup(selectedOption: String, options: List<String>, labels: List<String>, onOptionSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEachIndexed { i, option ->
            SettingsOptionItem(
                isSelected = selectedOption == option,
                label = labels[i],
                onSelected = { onOptionSelected(option) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOptionItem(isSelected: Boolean, label: String, onSelected: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.RadioButton, onClick = onSelected)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodyLarge
        )
    }
}

---

## 검수 체크리스트(복제 성공 여부)
- [ ] 상단 여백 8dp, 하단 여백 8dp가 카드 외부에서 균등하게 적용된다.
- [ ] 카드 사이 간격 12dp가 일정하다.
- [ ] 콘텐츠가 뷰포트를 넘지 않으면 스크롤/바운스가 전혀 없다.
- [ ] 콘텐츠가 넘치면 자연스럽게 스크롤이 가능하고, 오버스크롤 효과는 시스템 기본을 따른다.
- [ ] 옵션 아이템의 터치/접근성 표면이 충분하다(최소 48dp).
- [ ] 선택 상태 시 타이포그래피와 컬러가 강조된다.

---

## 포팅 가이드/주의사항
- 디자인 토큰/리소스가 없을 경우 위의 대체값으로 치환하되, 레이아웃 간격 스펙은 절대 변경하지 않는다(상/하 8dp, 그룹 간 12dp, 그룹 내 6dp, 옵션 높이 48dp).
- App 바 아래 Divider가 있는 앱에서는 상단 시각적 여백이 더 커 보일 수 있다. 이때도 외부(컨테이너) 상단 패딩은 8dp를 유지하고, 앱바/Divider의 스타일은 별도 조정한다.
- 글자 수/줄바꿈으로 카드 높이가 달라져도 스크롤 판정이 정확히 동작해야 한다(실측 기반 판정 로직 사용).
