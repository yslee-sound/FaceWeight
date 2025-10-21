# 디자인 토큰 가이드 (Design Tokens)

본 문서는 AlcoholicTimer 앱 전역에서 사용하는 핵심 UI 디자인 토큰과 사용 원칙을 정의합니다. 현재 앱은 라이트 모드만 지원한다는 전제를 두고 최소 단순화된 단계만 노출합니다. 향후 다크 모드/테마 확장 시 이 문서를 업데이트합니다.

---
## 1. Alpha (투명도) 정책
| 토큰 | 값 | 용도 | 비고 |
|------|----|------|------|
| `AppAlphas.SurfaceTint` | `0.1f` | 약한 강조 배경 / 아이콘 배경 tint | 이 값 외 임의 장식 alpha 금지 |

허용 예외: Disabled 상태 표현, 시스템 라이브러리 컴포넌트 내부(예: `ContentAlpha.disabled`)는 기능적 의미가 있으므로 허용.

금지 예 (검출 대상)
- `alpha = 0.9f`
- `alpha = 0.95f`
- 임의의 `surface.copy(alpha = …)` 패턴

대체 방법: 확실한 대비가 필요하면 별도의 컬러 토큰을 도입하거나 명도/채도 조정 컬러를 정의합니다.

---
## 2. Elevation 단계
| 토큰 | 값 | 목적 | 사용 기준 |
|------|----|------|-----------|
| `AppElevation.ZERO` | `0.dp` | 평면 배경, 내부 레이아웃 컨테이너 | 기본 배경 / 섹션 구분 없음 |
| `AppElevation.CARD` | `2.dp` | 보조 카드, 작은 선택/필터/옵션 그룹 | 시각적 구획 필요하지만 주목도 낮음 |
| `AppElevation.CARD_HIGH` | `4.dp` | 주요 콘텐츠 카드, 리스트 아이템, 클릭 가능한 주요 엔티티 | 상호작용/포커스 대상 강조 |

> 변경 메모(2025-10-08): 3dp 실험 제거, 최종 스케일 0 / 2 / 4 고정. 주요 원형 액션(FAB 유사, 강조 단일 버튼)은 별도 3dp 도입 없이 `AppElevation.CARD_HIGH` 재사용. 새 단계가 필요하다고 판단되면 우선 디자인 근거(시각적 계층 충돌, 그림자 대비 한계 등)를 문서화 후 Backlog(Elevation) 섹션에 제안하세요.

향후 확장 예약: `AppElevation.DIALOG (8.dp)` / `BOTTOM_SHEET`, 필요 시 추가 (현재 요구 없음 → 미도입).

금지 예 (검출 대상)
- `CardDefaults.cardElevation(defaultElevation = 1.dp)`
- `... 6.dp`, `... 8.dp` (표준 단계 외 사용)
- `CardDefaults.cardElevation(defaultElevation = 4.dp)` (직접 literal 사용) → 반드시 `AppElevation.CARD_HIGH`.

---
## 3. Shape & Padding
| 요소 | 값 | 비고 |
|------|----|------|
| 기본 카드 모서리 | `RoundedCornerShape(16.dp)` | `AppCard` 내부 기본값 |
| 카드 내부 패딩 | `16.dp` | 큰 여백 통일, 세로 공간 절약 요구 생기면 세부 컴포넌트화 고려 |

Shape/Padding 도 반복 사용이 늘어나면 `AppShapes`, `AppSpacing` 등으로 승격 가능.

---
## 4. Color 사용 가이드
현재 라이트 모드 고정:
- `MaterialTheme.colorScheme.surface` + `onSurface` 조합을 기본으로 사용.
- `Color.White` 직접 사용은 (라이트 모드 한정 UI) 허용하지만 다크 모드 확장 시 surface 기반으로 전환 필요.
- 장식/약한 강조 배경 tint 필요 시: 표면색 + `AppAlphas.SurfaceTint` 적용된 별도 Color 생성 (직접 `copy(alpha=...)` 대신 명시적 토큰 사용 권장).

---
## 5. 공통 카드 컴포저블 `AppCard`
```kotlin
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    elevation: Dp = AppElevation.CARD_HIGH,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

사용 예 (비클릭 / 클릭):
```kotlin
// 비클릭 정보 카드
AppCard(elevation = AppElevation.CARD) {
    Text("총 기록 12회")
}

// 클릭 가능한 요약 카드
AppCard(onClick = { navigateDetail() }) {
    Column { /* ... */ }
}
```

선언적 이점:
- elevation literal 제거 (토큰 강제)
- shape / padding / 색상 일관성
- onClick nullable 로 Card 두 종류 통합

---
## 6. 정적 검사 (designTokenCheck)
Gradle 커스텀 태스크 `designTokenCheck` 가 `app` 모듈에 정의되어 있으며 `check` 파이프라인에 포함됩니다.
검출 패턴 (부분 문자열 기반):
- `alpha = 0.9f`
- `alpha = 0.95f`
- `CardDefaults.cardElevation(defaultElevation = 1.dp`
- `CardDefaults.cardElevation(defaultElevation = 6.dp`
- `CardDefaults.cardElevation(defaultElevation = 8.dp`
- `CardDefaults.cardElevation(defaultElevation = 4.dp` (토큰 강제 목적)
- `.surface.copy(alpha =`

위반 시 Gradle 빌드 실패: `./gradlew :app:designTokenCheck` 또는 `./gradlew :app:check`.

한계:
- 간단한 substring 매칭 (주석/문자열 내부도 탐지 가능) → 필요 시 정교한 Regex / 커스텀 Lint 전환.

---
## 7. 향후 확장 제안 (Backlog)
| 영역 | 제안 | 우선순위 |
|------|------|----------|
| Shapes | `AppShapes.CardLarge = RoundedCornerShape(16.dp)` | Low |
| Spacing | `AppSpacing.M / L` 등 정의 | Low |
| Color | 다크 모드 대응 토큰 정리 | Mid |
| Elevation | Dialog / Sheet / FAB 전용 단계 추가 | Mid |
| Lint | 커스텀 Lint Rule 로 substring false positive 감소 | Mid |
| Theme | Dynamic color (Android 12+) 고려 | Low |

---
## 8. 적용 현황 (작성 시점)
- `AppCard` 도입 및 `DetailStatCard` 치환 완료
- 추후: `RecordSummaryCard`, `SettingsCard`, 빈 상태 카드 등 점진 치환 예정

---
## 9. 기여 규칙
PR 생성 시:
1. 새 디자인 값(색상/크기/Alpha/Elevation) 추가 필요 → 먼저 토큰 정의 후 사용.
2. 기존 literal 발견 시 가능하면 이번 PR 에서 함께 정리.
3. designTokenCheck 실패 시 안내 메시지에 따라 AppElevation/AppAlphas 사용으로 교체.

---
문의/수정 필요 시: `core/ui/DesignTokens.kt` 와 본 문서를 동기화하세요.
