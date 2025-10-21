# 앱 아이콘 빠른 규격(디자이너용 치트시트)

> 문서 버전
> - 버전: v1.0.0
> - 최근 업데이트: 2025-10-21
> - 변경 요약: 초판 작성 — Figma 제작/내보내기 핵심 규격 요약, Adaptive Icon 레이어 구조, 체크리스트/실수 주의 수록
>
> 변경 이력(Changelog)
> - v1.0.0 (2025-10-21)
>   - 최초 작성: Android Adaptive Icon 규격 핵심, Figma 치트시트, 프로젝트 반영 경로/주의사항 정리

---

## 0) 목적/대상
- 목적: 디자이너가 Figma에서 앱 아이콘을 제작·내보내고, 개발 프로젝트에 정확히 반영할 수 있도록 “필수 스펙만” 빠르게 확인하는 용도
- 대상: Android 런처 아이콘(Adaptive Icon) + Google Play 스토어 아이콘

---

## 1) Adaptive Icon 핵심 규격(한눈에)
- 전체 캔버스: 108dp × 108dp (= xxxhdpi 기준 432 × 432 px)
- 안전영역(Safe area): 72dp × 72dp (= 288 × 288 px @xxxhdpi)
- 전경 인셋: 사방 18dp (= 72 px @xxxhdpi) — 전경/모노크롬은 인셋 래퍼가 적용됨
- 레이어 구성: Background(불투명) + Foreground(투명) + Monochrome(단색, Android 13+ 테마 아이콘)
- 권장 포맷: SVG(VectorDrawable 변환 전제) — PNG 사용 시 xxxhdpi 432 × 432 px 원본
- 마스크 주의: 다양한 런처 마스크(원형/스쿨/티어드롭)에서 가장자리가 잘릴 수 있으므로 핵심 그래픽은 안전영역 안에 배치

밀도별 참고(px)
- 108dp → mdpi 108 / hdpi 162 / xhdpi 216 / xxhdpi 324 / xxxhdpi 432
- 72dp  → mdpi 72 / hdpi 108 / xhdpi 144 / xxhdpi 216 / xxxhdpi 288
- 18dp → mdpi 18 / hdpi 27 / xhdpi 36 / xxhdpi 54 / xxxhdpi 72

---

## 2) Figma 치트시트(제작/내보내기)
- 프레임 설정:
  - Foreground: 432 × 432 px(투명) — 핵심 그래픽은 288 × 288 px 안전영역 안에 배치
  - Background: 432 × 432 px(불투명 단색 또는 심플 패턴)
  - Monochrome: 단색 벡터(SVG, path 1개 권장), 그라디언트/라스터 금지
- 가이드 설정:
  - 안전영역: 중앙 288 × 288 px(사방 72 px 마진)
  - 인셋 개념: 사방 18dp = 72 px(@xxxhdpi) — 개발 측에서 인셋 래퍼로 적용됨
- 내보내기(권장):
  - Foreground: SVG(권장) 또는 PNG 432 px(투명)
  - Background: SVG 또는 PNG 432 px(불투명)
  - Monochrome: SVG(단색, 병합된 단일 path)
- 임포트 팁(Android Studio): Vector Asset → Local file(SVG)
  - Foreground → `drawable-anydpi-v26/ic_launcher_foreground.xml`
  - Monochrome → `drawable-anydpi-v26/ic_launcher_monochrome.xml`
  - Background가 이미지면 Vector/Bitmap 드로어블로 교체, 단색이면 색만 변경

---

## 3) 프로젝트 반영 포인트(파일 경로)
- Adaptive 설정: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - background: `@drawable/ic_launcher_background`
  - foreground: `@drawable/ic_launcher_foreground_inset` (사방 18dp 인셋 래퍼)
  - monochrome: `@drawable/ic_launcher_monochrome_inset` (사방 18dp 인셋 래퍼)
- 실제 그래픽 교체 대상:
  - 전경 원본: `app/src/main/res/drawable-anydpi-v26/ic_launcher_foreground.xml`
  - 모노크롬 원본: `app/src/main/res/drawable-anydpi-v26/ic_launcher_monochrome.xml`
  - 배경: `app/src/main/res/drawable/ic_launcher_background.xml`(단색) 또는 적합한 드로어블
- 유지해야 할 것:
  - 인셋 래퍼(`ic_launcher_foreground_inset.xml`, `ic_launcher_monochrome_inset.xml`)의 18dp 값은 변경하지 않음
  - 매니페스트 아이콘 참조: `android:icon="@mipmap/ic_launcher"` 그대로 유지

---

## 4) Google Play 스토어 아이콘(마케팅 자산)
- 규격: 512 × 512 px, 32-bit PNG(알파 가능)
- 금지: 직접 라운딩/그림자 적용(스토어가 후처리), Android 런처 아이콘 규격(108dp)과 혼동 금지

---

## 5) QA 체크리스트(제작 검수)
- [ ] Foreground 핵심 그래픽이 안전영역(288 × 288 px @xxxhdpi) 안에 들어오는가
- [ ] Background가 432 × 432 px 프레임을 불투명하게 채우는가
- [ ] Monochrome가 단색 SVG이며 path 1개로 병합되어 있는가(그라디언트/라스터 금지)
- [ ] 다양한 런처 마스크에서 잘림 없이 가독성이 확보되는가
- [ ] 인셋(18dp = 72 px) 래퍼가 유지되는 전제에서 여백·균형이 자연스러운가
- [ ] 실제 기기에서 라이트/다크, 해상도/배율별 표시가 적절한가

---

## 6) 자주 하는 실수(주의)
- Foreground에 배경색을 섞어 넣음 → 배경은 Background 레이어에서 처리
- Monochrome를 컬러/그라디언트/라스터로 만듦 → 단색 벡터만 허용
- 안전영역 밖에 텍스트/얇은 테두리를 배치 → 마스크에서 쉽게 잘림
- 스토어 512 px PNG에 직접 라운딩/그림자 적용 → 금지

---

## 7) 원본 자산 보관 경로(권장)
- `docs/assets/app_icon/`
  - Foreground.svg, Background.svg, Monochrome.svg

---

부록) 관련 문서
- 상세 가이드: `docs/a_APP_ICON_AND_EXPORT_GUIDE.md`
- UI 스타일 버전/이력 포맷 참고: `docs/a_FLAT_UI_BASE_PROMPT.md`

