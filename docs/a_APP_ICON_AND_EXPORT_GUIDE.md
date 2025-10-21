# 앱 아이콘 · 내보내기(Export) 가이드

본 문서는 Android 런처 아이콘(Adaptive Icon)과 Google Play 스토어용 아이콘을 어떻게 제작·내보내고, 본 프로젝트에 교체하는지 정리합니다.

—

## 1) 용어·대상 구분
- 런처 아이콘(앱 리소스): 기기 홈 화면/런처에 표시되는 아이콘. Android 리소스(dp 단위)와 밀도별(px)로 동작하며 Adaptive Icon(배경/전경/모노크롬 레이어) 구조를 사용.
- 스토어 아이콘(마케팅 이미지): Google Play Console에 업로드하는 고해상도 PNG. 앱 패키지 안에는 포함되지 않음.

—

## 2) 프로젝트 현황(파일 구조)
프로젝트는 Adaptive Icon을 사용합니다.
- Adaptive 설정 파일: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - background: `@drawable/ic_launcher_background`
  - foreground: `@drawable/ic_launcher_foreground_inset` (전경 인셋 래퍼)
  - monochrome: `@drawable/ic_launcher_monochrome_inset` (모노크롬 인셋 래퍼)
- 전경/모노크롬 인셋: `app/src/main/res/drawable-anydpi-v26/ic_launcher_foreground_inset.xml`, `ic_launcher_monochrome_inset.xml`
  - 사방 18dp 인셋 지정
- 전경/모노크롬 원본 드로어블: `app/src/main/res/drawable-anydpi-v26/ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`
- 배경 드로어블: `app/src/main/res/drawable/ic_launcher_background.xml`
- 레거시/라운드 비트맵: `app/src/main/res/mipmap-*/ic_launcher.webp`, `ic_launcher_round.webp`
- 매니페스트 아이콘 참조: `app/src/main/AndroidManifest.xml` → `android:icon="@mipmap/ic_launcher"`

—

## 3) 런처 아이콘 규격(Adaptive Icon)
- 전체 캔버스: 108dp × 108dp
- 안전영역: 72dp × 72dp (핵심 그래픽 배치 영역)
- 인셋(마진): 사방 18dp (본 프로젝트는 전경/모노크롬에 이미 적용)
- 권장 포맷: 가능하면 벡터(VectorDrawable, SVG 임포트). 라스터(PNG) 사용 시 xxxhdpi 기준 432×432 px 소스로 제작.

밀도별(px) 환산(참고):
- 108dp → mdpi 108, hdpi 162, xhdpi 216, xxhdpi 324, xxxhdpi 432
- 72dp  → mdpi 72,  hdpi 108, xhdpi 144, xxhdpi 216, xxxhdpi 288
- 18dp  → mdpi 18,  hdpi 27,  xhdpi 36,  xxhdpi 54,  xxxhdpi 72

디자인 주의:
- 다양한 런처 마스크(원형/스쿨/티어드롭 등)에서 가장자리가 잘릴 수 있으므로 핵심 요소는 안전영역 내 배치.
- 배경은 불투명 단색/심플 패턴 권장. 전경 PNG는 투명 배경 유지.
- 모노크롬은 "단색 벡터"만 허용(그라디언트·라스터·다중 색상 금지, 시스템 틴트 적용 대상).

—

## 4) Figma(또는 디자인 툴) 내보내기 가이드
권장 해상도/포맷
- Foreground: 432×432 px PNG(투명) 또는 SVG(권장)
- Background: 432×432 px PNG(불투명) 또는 SVG
- Monochrome: SVG(단색, path 하나로 병합)
- Google Play 고해상도 아이콘: 512×512 px, 32-bit PNG(알파 가능), 모서리 라운딩/그림자 직접 적용 금지
- 원본 SVG 자산 보관 경로: `docs/assets/app_icon/` (Foreground.svg, Background.svg, Monochrome.svg)

[빠른 치트시트] 108dp → Figma px
- 프레임(Canvas): 432 × 432 px
- 안전영역(Safe area): 288 × 288 px (사방 72 px 마진)
- 인셋 표현: 사방 18dp = 사방 72 px(@xxxhdpi)
- SVG 임포트 팁: Vector Asset로 가져오면 width/height=108dp, viewportWidth/Height=108 유지 권장(벡터 스케일 손실 방지)
- PNG Export: 432 px 그대로 1×로 내보내기

Figma 단계별 팁
1) 프레임 생성: Foreground 432×432, Background 432×432, Monochrome(벡터)
2) Foreground 안전영역 가이드: 마진 72px(사방) 또는 288×288 사각형을 중앙 정렬해 핵심 그래픽을 그 안에 맞추기
3) Foreground는 투명 배경 유지, Background는 전체 불투명 채움
4) Monochrome은 모든 도형을 병합(Union)해 하나의 path, Stroke는 Outline으로 변환, 단색만 사용
5) Export: PNG 1×(432) 또는 SVG
6) Play 스토어용은 별도 512×512 프레임에서 PNG로 Export(모서리 라운딩/그림자 금지)

—

## 5) 프로젝트 반영 방법(파일 교체)
가급적 파일명·구조를 유지하면 추가 수정 없이 반영됩니다.

A. 벡터(SVG)로 교체(추천)
- 전경: Android Studio → New → Vector Asset → Local file(SVG) → `drawable-anydpi-v26/ic_launcher_foreground.xml` 덮어쓰기
- 모노크롬: 같은 방식으로 `drawable-anydpi-v26/ic_launcher_monochrome.xml` 덮어쓰기
- 배경: 단색이라면 `drawable/ic_launcher_background.xml`에서 색만 변경. 이미지 배경이면 Vector/Bitmap 드로어블로 교체
- 인셋은 유지: `ic_launcher_foreground_inset.xml`, `ic_launcher_monochrome_inset.xml`의 18dp 값 그대로 둠

B. PNG로 교체(Asset Studio 사용)
- Android Studio → File → New → Image Asset → Launcher Icons (Adaptive and Legacy)
  - Foreground: 432×432 PNG 지정(투명)
  - Background: 432×432 PNG 또는 단색 지정
  - Legacy and Round Icons 체크 → `mipmap-*/ic_launcher*.webp` 자동 생성
  - Monochrome(테마 아이콘) 탭/옵션에서 단색 벡터 등록

확인 포인트
- 매니페스트는 이미 `@mipmap/ic_launcher`를 참조 → 추가 수정 불필요
- 라이트/다크 테마, 다양한 런처 마스크에서 시각 확인

—

## 6) Google Play 스토어 자산
- 고해상도 앱 아이콘: 512×512 px, 32-bit PNG(알파 가능), 모서리 라운딩/그림자 직접 적용 금지
- Feature Graphic(필요 시): 1024×500 px PNG/JPG
- 참고: 1024×1024 px은 Android 런처 아이콘 규격이 아니라(혼동 주의), iOS나 다른 스토어 자산 문맥에서 쓰이는 경우가 많음

—

## 7) QA 체크리스트
- [ ] Foreground 핵심 그래픽이 안전영역(288×288 px @xxxhdpi) 안에 들어오는가
- [ ] Background가 전체 캔버스(432×432 px @xxxhdpi)를 불투명하게 채우는가
- [ ] Monochrome가 단색 벡터로만 구성되어 있는가(그라디언트/라스터 불가)
- [ ] 다양한 런처 마스크에서 테두리/문구가 잘리지 않는가
- [ ] Legacy/라운드 아이콘이 모두 생성됐는가(`mipmap-*/ic_launcher*.webp`)
- [ ] 실제 기기/에뮬에서 라이트/다크, 해상도/배율별 표시 확인
- [ ] Android 13+ 테마 아이콘(홈 설정 > 테마 아이콘) 활성화 시 색 틴트/가독성/여백이 적절한가

—

## 8) 자주 하는 실수
- Foreground에 배경색을 섞어 넣음(→ 배경은 Background 레이어에서 처리)
- Monochrome를 컬러/그라디언트/비트맵으로 만듦(→ 단색 벡터만 허용)
- 안전영역 밖에 텍스트/얇은 테두리를 배치(→ 마스크에서 쉽게 잘림)
- Play 스토어 512×512 PNG에 직접 라운딩/그림자 적용(→ 스토어가 처리함)

—

## 부록 A) Monochrome(테마 아이콘) 상세
한줄 정의
- Monochrome 아이콘은 Android 13+의 "테마 아이콘(Themed icons)"에 사용되는 단색 벡터 레이어입니다. 런처가 시스템/벽지에서 추출한 색으로 자동 틴트합니다.

사용처
- 홈 화면에서 테마 아이콘을 켰을 때 적용(Android 13+)
- 일부 시스템 화면(설정/검색/단축아이콘 등)에서도 단색 버전이 활용될 수 있음
- OS<13 또는 테마 아이콘 OFF일 때는 풀컬러 전경 아이콘 사용

규격/제약
- 반드시 단색(VectorDrawable)만: 그라디언트/여러 색/라스터 불가
- 투명 배경 + 단일 Fill 권장(Stroke는 Outline 변환 권장)
- 크기 개념은 Adaptive와 동일: 108dp 캔버스, 72dp 안전영역
- 본 프로젝트는 전경/모노크롬 모두 18dp 인셋 적용(= @xxxhdpi 72 px). 인셋 래퍼가 자동 적용됨

프로젝트 연결(파일 경로)
- 참조 XML: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` → `monochrome="@drawable/ic_launcher_monochrome_inset"`
- 인셋 래퍼: `app/src/main/res/drawable-anydpi-v26/ic_launcher_monochrome_inset.xml` (사방 18dp)
- 실제 그래픽: `app/src/main/res/drawable-anydpi-v26/ic_launcher_monochrome.xml` (여기에 단색 벡터 추가/교체)

Figma 제작 팁
- 프레임 432×432 px(=108dp @xxxhdpi)로 작업, 안전영역 288×288 px(사방 72 px) 안에 형태 배치
- 로고/도형을 단색으로 통일 후 모든 도형을 병합(Union) → path 1개로 정리
- Stroke는 Outline(윤곽선)으로 변환해 두께 이슈 방지, 너무 얇은 선은 피하기
- Export: SVG 권장(검정 #000 등 단색). Android에서 시스템 색으로 자동 틴트됨
- Android Studio: New → Vector Asset → Local file(SVG)로 임포트해 `ic_launcher_monochrome.xml`로 저장

미제공 시 동작
- 일부 런처가 전경을 단색으로 추정 변환할 수 있으나, 품질 저하/미적용 가능성이 있어 권장하지 않음. 전용 Monochrome 제공 권장

테스트 방법
- Android 13+ 기기/에뮬 → 홈 설정에서 테마 아이콘 ON
- 밝은/어두운 테마 및 다양한 런처 마스크(원형/스쿨/티어드롭)에서 잘림/가독성 확인

—

## 9) 요약
- 런처 아이콘: 108dp(= 432 px @xxxhdpi), 전경/모노크롬 18dp 인셋, 72dp 안전영역
- 제작·내보내기: Foreground/Background 432 px, Monochrome는 단색 SVG, 가능하면 벡터 우선
- 스토어 아이콘: 512×512 px PNG 별도 업로드
- 본 프로젝트는 파일명·구조만 맞춰 교체하면 매니페스트 수정 없이 반영됩니다.
