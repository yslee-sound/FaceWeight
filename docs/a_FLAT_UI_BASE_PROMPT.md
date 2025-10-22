> 문서 버전
> - 버전: v1.1.0
> - 최근 업데이트: 2025-10-22
> - 변경 요약: 레벨 화면 ‘전체 레벨’ 섹션 컨테이너에 헤어라인(0.5dp) + 0dp 적용 지침 추가, 리스트 아이템 규칙 재명시, 검증 항목 보강
>
> 변경 이력(Changelog)
> - v1.1.0 (2025-10-22)
>   - 레벨 화면: ‘전체 레벨’ 섹션 컨테이너에 헤어라인(0.5dp) + 0dp 적용 지침 추가
>   - 리스트 아이템: 기본 회색 테두리/그림자 제거, 현재/달성 항목만 1dp 색 테두리 유지 재명시
>   - 검증 체크리스트 보강(‘전체 레벨’ 섹션 헤어라인 확인)
> - v1.0.0 (2025-10-21)
>   - 초기 작성: 디자인 토큰(AppElevation/AppBorder/color_border_light) 정의
>   - AppCard 기본값(0dp + Hairline) 제안 및 화면별 적용 가이드 수록
>   - 수용 기준/리스크/검증 체크리스트 추가
>
> 버전 규칙
> - Semantic Versioning 준수: MAJOR(호환성 깨짐)/MINOR(가이드·정책 추가)/PATCH(오타·경미한 정정)
> - 문서 갱신 시 상단 버전/날짜/요약, 하단 변경 이력을 함께 갱신합니다.

// filepath: g:\Workspace\NoSmokeTimer\docs\a_FLAT_UI_BASE_PROMPT.md
플랫(Flat) UI 전역 스타일 적용 프롬프트

목표
- 반사광/그림자보다 얇은 헤어라인 테두리와 0dp 고도의 평면 카드(UI 컨테이너)로 전 앱 화면을 일관되게 정리한다.
- 기존 배경(MaterialTheme.colorScheme.background 등)과 자연스럽게 어울리는, 보이는 듯-안 보이는 듯한 경계감을 만든다.
- 강조가 필요한 원형 주요 버튼만 2dp 고도를 유지한다.

역할 지시(Role)
당신은 Android Jetpack Compose UI 리팩터링 전문가다. 아래 요구사항을 만족하도록 디자인 토큰을 정의/갱신하고, 공통 카드(AppCard)와 화면별 UI에 플랫한 외곽 스타일을 전역 적용하라. 변경 후 빌드와 기본 테스트가 통과하고, 시각 확인 체크리스트를 만족해야 한다.

적용 범위(Scope)
- 디자인 토큰 추가/갱신
- 공통 카드(AppCard) 기본값 변경
- 화면별 카드/컨테이너 외곽 스타일 통일
- 다크 모드 대비/톤 가이드 연결
- 품질 게이트(빌드/테스트/시각 확인) 통과

디자인 토큰(필수)
- Elevation
  - AppElevation.CARD = 0.dp  // 기본 카드·컨테이너는 평면
  - AppElevation.CARD_HIGH = 2.dp  // 주목도 있는 원형 주요 버튼(시작/중지 등)
- Border
  - AppBorder.Hairline = 0.5.dp  // 전역 테두리 두께 표준
- Color
  - color_border_light = #EEF1F5  // 은은한 라이트 테두리(톤 업)
  - 다크 모드: outlineVariant 또는 적절한 low-contrast outline 계열에 매핑

공통 카드(AppCard) 기본값(필수)
- elevation = AppElevation.CARD (0.dp)
- border = BorderStroke(AppBorder.Hairline, colorResource(R.color.color_border_light))
- shape는 제품 컨벤션 유지(예: 12~20.dp 범위에서 제품 표준값 사용)
- ripple/interaction은 기존 정책 유지

화면별 적용 가이드
- 레벨(Level)
  - 상단 “현재 레벨” 카드: 헤어라인 테두리 + 평면(0dp) 적용
  - “전체 레벨” 섹션 컨테이너: 헤어라인 테두리 + 0dp 적용(AppBorder.Hairline, color_border_light)
  - 리스트 아이템: 기본 회색 테두리/그림자 제거; 현재/달성 항목만 색상 포커스(색 테두리 1dp) 유지
- 시작(Start) / 실행(Run) / 중지(Quit)
  - 화면 내 카드·컨테이너: 헤어라인 테두리 + 0dp
  - 원형 큰 시작/중지 버튼: 2dp 고도 유지(AppElevation.CARD_HIGH)
- 기록(RecordsScreen / RecordSummary / PeriodSelection)
  - 모든 카드·셀: 헤어라인 테두리 + 0dp로 통일
- 상세(Detail)
  - 컨테이너/카드: 헤어라인 테두리 + 0dp
- 라이선스(AboutLicenses)
  - 항목/카드: 헤어라인 테두리 + 0dp

스타일 원칙
- 배경과의 관계: 기존 배경과 어울리도록 경계 대비를 낮춘다(연한 color_border_light).
- 그림자/반사광 축소: 불필요한 elevation/shadow 제거(0dp 기본). 단, 핵심 CTA 원형 버튼은 2dp 유지.
- 색상 포커스: 정보 상태 강조가 필요한 곳(예: 레벨의 현재/달성 항목)은 색 테두리 1dp를 유지한다.

다크 모드 가이드(권장)
- color_border_light는 outlineVariant 또는 유사 저대비 아웃라인에 매핑하여 야간 대비 최적화
- 필요 시 라이트/다크 팔레트 각각에 border 색을 정의하고, alpha 조정으로 존재감 최소화

디자인 토글 포인트(미세 조정)
- 테두리 진하기: color_border_light를 #F2F4F7(더 흐림) ~ #E5E8EC(살짝 진함) 범위로 조정
- 테두리 두께: AppBorder.Hairline 0.5dp → 0.75dp 또는 1dp
- 완전 플랫: 특정 카드에선 border = null로 경계 제거
- 모서리 반경: 제품 표준에 맞춰 12~20.dp로 통일 조정 가능

개발 가이드(컴포넌트 사용)
- 기본: AppCard를 사용하면 플랫 스타일이 기본 적용된다.
- 직접 Card 사용 시
  - elevation = AppElevation.CARD
  - border = BorderStroke(AppBorder.Hairline, colorResource(R.color.color_border_light))
- 강조 버튼/플로팅 액션: elevation = AppElevation.CARD_HIGH

권장 구현 순서(Checklist)
1) 디자인 토큰 추가/갱신
   - AppElevation, AppBorder, color_border_light 정의
   - 다크 모드 매핑(outlineVariant 등) 준비
2) 공통 컴포넌트 업데이트
   - AppCard 기본값: elevation 0dp + Hairline Border
   - 원형 주요 버튼: elevation 2dp 유지 확인
3) 화면 적용
   - 레벨 화면: 상단 카드 플랫/헤어라인, 전체 레벨 섹션 컨테이너 헤어라인 + 0dp 적용, 리스트 아이템 경계·그림자 제거(현재/달성 1dp 색 테두리 유지)
   - Start/Run/Quit, Records, Detail, AboutLicenses: 전부 헤어라인 + 0dp 통일
4) 코드 정리
   - 중복 elevation/shadow 제거
   - 임시 색상/하드코딩 값 토큰으로 치환
5) 빌드/테스트
   - assembleDebug 성공(릴리스 키스토어 경고는 무시 가능)
   - JVM 단위 테스트 통과 확인
6) 시각 확인
   - 레벨/기록/설정 주요 화면에서 카드 외곽이 얇고 은은하게만 보이는지
   - “전체 레벨” 섹션 컨테이너 외곽에 0.5dp 헤어라인이 배경과 저대비로 은은하게 보이는지
   - “전체 레벨” 박스는 회색 경계·그림자 없이 평면 컨테이너로 보이는지
   - 원형 시작/중지 버튼은 2dp 고도 유지로 주목도 확보되는지

수용 기준(Acceptance Criteria)
- 전역 카드/컨테이너가 elevation 0dp, Hairline 테두리로 통일됨
- 레벨 화면의 “전체 레벨” 섹션 컨테이너가 헤어라인(0.5dp) 테두리 + 0dp 고도로 적용됨
- 레벨 리스트의 현재/달성 항목은 색 테두리 1dp가 유지됨(의도된 예외)
- 원형 주요 버튼은 elevation 2dp 유지됨
- 빌드와 JVM 테스트 통과
- 라이트 모드에서 경계가 과도하게 튀지 않음, 다크 모드에서도 대비 과하지 않음

리스크/엣지 케이스
- 너무 옅은 경계: 배경과의 대비 부족으로 컨테이너 구분이 어려울 수 있음 → color_border_light 미세 조정
- 밀도 높은 리스트: 테두리가 많아 보일 수 있음 → 카드 간 간격을 약간 확대(예: 16dp → 20dp)
- 터치 영역/리플: elevation 제거 후 상호작용 피드백 약화 체감 가능 → ripple 색/alpha 점검

간단 코드 예시(Compose, 개념 참고)
- Border/Color 토큰
  - val AppBorder = object { val Hairline = 0.5.dp }
  - val AppElevation = object { val CARD = 0.dp; val CARD_HIGH = 2.dp }
  - color_border_light = #EEF1F5 (라이트), 다크는 outlineVariant 매핑
- AppCard 기본값
  - AppCard(
      elevation = AppElevation.CARD,
      border = BorderStroke(AppBorder.Hairline, colorResource(R.color.color_border_light)),
      shape = AppShapes.Medium
    ) { /* content */ }

검증 방법(요약)
- 디바이스/에뮬레이터로 주요 화면 확인
- 특히 “전체 레벨” 섹션 컨테이너의 헤어라인(0.5dp)이 과도하지 않게 보이는지, “전체 레벨” 박스가 회색 테두리/그림자 없이 평면 컨테이너로 보이는지, 원형 시작/중지 버튼이 2dp 고도로 떠 보이는지 확인

변경 로그 템플릿(권장)
- feat(ui): 전역 플랫 스타일 적용 – 0dp 카드 + 0.5dp 헤어라인, border 톤 업(#EEF1F5), 주요 원형 버튼 2dp 유지
- feat(ui-level): 레벨 화면 ‘전체 레벨’ 섹션 컨테이너에 헤어라인(0.5dp) + 0dp 적용
- chore: Card 사용부를 AppCard로 통일; 그림자 제거; 다크 모드 outlineVariant 매핑
- fix: 레벨 리스트 예외(현재/달성 1dp 색 테두리) 유지

PR 체크리스트(복붙용)
- [ ] AppElevation/AppBorder/color_border_light 토큰 추가/갱신
- [ ] AppCard 기본값(0dp + Hairline) 적용
- [ ] 레벨 화면 “전체 레벨” 섹션 컨테이너 헤어라인(0.5dp) + 0dp 적용
- [ ] 레벨/Start/Run/Quit/Records/Detail/AboutLicenses 화면 반영
- [ ] 원형 시작/중지 버튼 2dp 유지
- [ ] 빌드/테스트 통과
- [ ] 시각 확인 스크린샷 첨부(라이트/다크)

미래 개선(선택)
- 다크 모드에서 outlineVariant 미세 튜닝(톤/알파)
- 리스트 하단 여백 소폭 확대로 플랫 스타일의 공간감 강화
- 경계 두께/톤을 스크린샷 기준으로 1~2 단계 더 튜닝

사용 방법
- 본 문서를 프롬프트로 붙여넣고, 대상 앱의 테마/컴포넌트/화면 코드를 분석·수정하도록 지시하라.
- 파일/패키지 구조가 다를 수 있으므로, 토큰/컴포넌트 정의 위치는 대상 앱의 컨벤션을 따른다.
