# AlcoholicTimer

AlcoholicTimer는 금주(절주) 습관 형성을 돕는 Android 앱입니다. 핵심 기능: 금주 타이머, 목표 진행률/레벨, 기록 관리(목록/상세/전체보기), 기간별 통계(주/월/년/전체), 주간 성공률 시각화. UI는 Jetpack Compose(Material3) 기반이며 데이터는 로컬(SharedPreferences/JSON) 저장을 사용합니다.

## 업데이트 최소화 원칙 (Daily Minimal Update Policy)
반복적인 소규모 유지보수 시간을 줄이기 위해 아래 원칙을 모든 일일 수정(Daily Update)에 적용합니다.

### 1. 작업 범위(Scope)
- "요청된 문제/기능"에 직접 필요한 변경만 수행 (직접 영향 없는 코드/문서 수정 금지)
- 향후 아이디어/로드맵/옵션 제안 금지 (요청 시에만 별도 목록으로 제공)
- 리팩터링/모듈화/네이밍 개선은 버그 수정 또는 요구 기능 구현에 필수일 때만 최소 범위로 제한

### 2. 금지 항목 (Do NOT)
- 불필요한 코드 스타일 통일 / 대규모 포매팅(Formatter 자동 출력 예외 없이 금지)
- 영향 없는 import 정리, 단순 Kotlin/Gradle 최신화 시도
- 의존성 버전 업그레이드 (보안/CVE 또는 요청된 기능 종속이 아닌 경우)
- 테스트 커버리지 확대 제안 / 신규 테스트 파일 생성 (핵심 변경으로 실패 가능 로직 직접 영향 아닐 시)
- 문서 전반 재구성, 목차/레이아웃 미세 개선, 불필요한 번역/중복 요약 추가
- 성능 미세 최적화/메모리 추측성 변경
- Lint 경고 중: 새로 추가된 변경과 직접 관련 없는 기존 경고 수정

### 3. 허용 항목 (Allowed)
- 직접 수정된 로직의 안정성 위해 필요한 아주 작은 단위 테스트 추가 (없으면 버그 회귀 가능성이 높다는 근거 명확할 때)
- 컴파일/런타임 오류를 유발하는 즉시 주변 코드 최소 보정
- 보안/치명적 크래시(API 변경/Null 가능성) 즉시 발견 시 같은 PR 내 최소 패치 (별도 확장 제안 없이)

### 4. 변경 크기/형태
- 가능한 한 파일/라인 수 최소화 (불가피한 대체보다 국소 수정 우선)
- 커밋 메시지 포맷 (단일 커밋 권장):
  - fix: <문제 요약>
  - feat: <기능 요약>
  - chore: <작업 요약> (동일 범위 내 관리성 조정)
- Diff 내 비기능적 변경(정렬·들여쓰기·주석 개선)은 요청 작업과 직접 연관(이해 위해 필요한)인 경우만 허용

### 5. 검증(Validation)
- 빌드 성공 + 영향 영역 단위 테스트만 (전체 스위트나 Lint 전체 실행 강제 X, 단 요청에서 명시 시 수행)
- designTokenCheck / Lint 실패가 새 변경에서 기인한 경우만 수정

### 6. 의사소통
- "추가로 할 수 있는 것" 제안 문구 금지 (명시적 추가 요청이 오기 전까지)
- 범위 밖 항목을 인지했을 때: 1줄로 "(out-of-scope 발견: 요약) – 미적용" 만 남기고 아무 조치하지 않음

### 7. 예외 처리
- 위 원칙과 충돌하더라도 빌드 불가 / 앱 실행 불가 / 데이터 손상 가능성이 높은 명백한 버그 발견 시: 최소 패치 + 패치 이유 1줄 요약

### 8. 적용 체크리스트 (PR 작성 전 자체 점검)
- [ ] 모든 변경이 요청 설명 또는 직접 파생 영향에 속한다
- [ ] 불필요한 포맷/리팩터링/정리 없음
- [ ] 의존성 / Gradle 세팅 / 전역 구성 변경 없음 (필수 사유 기록 제외)
- [ ] 테스트 추가 시 실패 재현 또는 회귀 방지 근거 1줄 코멘트 존재
- [ ] 커밋 1~2개, 메시지 규칙 준수

요약: "요청된 것만, 필요한 만큼, 단 한 번" 원칙을 따른다. 새 아이디어/확장은 Issue/Backlog 라벨로 분리 후 별도 사이클에서 다룬다.

## 주요 기능
- 금주 기록 생성 및 실시간 진행률 표시 (목표 일수 대비 % 및 레벨)
- 주 / 월 / 년 / 전체 통계 (평균·최대 지속일, 누적 금주일)
- 주간 성공률: 해당 주 7일 중 금주 유지일 비율 (중복 기록 병합 후 산출, 7일 모두 유지 시 100%)
- Adaptive & Monochrome 런처 아이콘 (Android 13 테마 아이콘 대응)

## 버전 / 빌드 전략 (요약)
- Semantic Versioning: `MAJOR.MINOR.PATCH` → `versionName`
- `versionCode`: CI 환경에서 환경변수 `VERSION_CODE` (없으면 임시 YYYYMMDD 대체)
- Release 빌드: R8 코드 난독화 + 리소스 축소 활성화
- Keystore 서명: 환경변수 기반 (미설정 시 unsigned 번들 생성)
  - `KEYSTORE_PATH`, `KEYSTORE_STORE_PW`, `KEY_ALIAS`, `KEY_PASSWORD`

## 빠른 시작 (Windows / Gradle Wrapper)
```bat
REM 클린 & 디버그 빌드
gradlew.bat clean
gradlew.bat :app:assembleDebug

REM Lint 검사 (Debug / Release Vital)
gradlew.bat :app:lintDebug
gradlew.bat :app:lintVitalRelease

REM 유닛 테스트
gradlew.bat :app:testDebugUnitTest

REM 릴리스 번들 (환경변수 설정 후)
SET VERSION_CODE=20251005
SET VERSION_NAME=1.0.0
REM (선택) 서명 키 설정
REM SET KEYSTORE_PATH=keystore.jks
REM SET KEYSTORE_STORE_PW=****
REM SET KEY_ALIAS=****
REM SET KEY_PASSWORD=****
gradlew.bat clean :app:bundleRelease
```
생성 산출물:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`

## 문서
- 기획 사양: [docs/APP_SPEC.md](./docs/APP_SPEC.md)
- UX 흐름: [docs/UX_FLOW.md](./docs/UX_FLOW.md)
- 패키지 구조 리팩터링 계획: [docs/REFACTORING_PACKAGE_STRUCTURE.md](./docs/REFACTORING_PACKAGE_STRUCTURE.md)
- 아이콘 디자인 가이드: [docs/ICON_DESIGN.md](./docs/ICON_DESIGN.md)
- 디자인 토큰: [docs/DESIGN_TOKENS.md](./docs/DESIGN_TOKENS.md)
- 출시 준비 & 배포 체크리스트: [docs/APP_RELEASE_PLAN.md](./docs/APP_RELEASE_PLAN.md)
- MVP 초간단 릴리스 체크리스트 (1인 개발용): [docs/MVP_RELEASE_CHECKLIST.md](./docs/MVP_RELEASE_CHECKLIST.md)
- 개인정보 처리방침: [docs/PRIVACY_POLICY.md](./docs/PRIVACY_POLICY.md)
- 스토어 메타데이터 템플릿: [docs/PLAY_STORE_METADATA_TEMPLATE.md](./docs/PLAY_STORE_METADATA_TEMPLATE.md)
- 변경 이력: [CHANGELOG.md](./CHANGELOG.md)

## 디렉터리 구조 개요
- `app/` : 애플리케이션 본체(Compose UI, Activity, 유틸)
- `docs/` : 사양/UX/배포/디자인 문서
- `scripts/` : 릴리스 자동화/보조 스크립트 (버전 bump, 아카이브)

## 스크립트 사용 (PowerShell)
```powershell
# 버전 증가 (build.gradle.kts fallback 값 치환)
pwsh ./scripts/bump_version.ps1 -VersionName 1.0.1 -VersionCode 20251006

# 릴리스 번들 빌드
$env:VERSION_NAME="1.0.1"; $env:VERSION_CODE="20251006"; ./gradlew.bat clean :app:bundleRelease

# 산출물 아카이브 (release/1.0.1_20251006/ 생성)
pwsh ./scripts/archive_release.ps1 -VersionName 1.0.1 -VersionCode 20251006
```

## 주간 성공률 산식 (요약)
```
주간 성공률 = (해당 주 내 금주 유지 총 일수(중복 병합) / 7) * 100  (반올림, 최대 100)
```
- 여러 기록이 겹칠 경우 겹치는 구간은 한 번만 포함
- 월/년/전체 화면의 "성공률"은 기존 시도별 목표진행률 평균 로직 유지

## 아이콘 (요약)
| 항목 | 값 |
|------|----|
| Inset | 18dp (Adaptive 캔버스 108dp 기준) |
| Foreground Scale | 1.43 (과밀 완화) |
| Monochrome | 단일 path, 테마 아이콘 대비 확보 |
| Colors | FG #C6283A / BG #FFFFFF |
| Round Icon | 별도 리소스 제거 (Adaptive 단일 유지) |

자세한 변경 이력·QA 체크리스트는 [ICON_DESIGN.md](./docs/ICON_DESIGN.md) 참고.

## Elevation 변경 가이드 (검증 5문항)
Elevation 단계는 현재 0 / 2 / 4dp (ZERO / CARD / CARD_HIGH) 고정입니다. 새 단계 도입 또는 변경 전 아래 5문항을 모두 YES 로 답할 수 있는지 확인하세요.
1. 필요성: 기존 0/2/4 중 어느 것도 시각적 계층 또는 상호작용 강조 요구(우선 처리, 드래그 부상, 모달 구분)를 충분히 해결하지 못한다는 근거(스크린샷/비교)가 있는가?
2. 대비 적정성: 새 Elevation 적용 후 주변 컴포넌트와 비교했을 때 그림자/레이어 대비가 과도(시각적 소음)하거나 미미(변경 의미 부족)하지 않음을 QA 캡처로 확인했는가?
3. 재사용 우선: `AppElevation.CARD_HIGH` 재사용으로 목적 달성이 불가함을 기능/패턴(예: 모달, overlay) 관점에서 설명 가능한가? (단순 "더 돋보이게"는 불가)
4. 토큰/정적검사 정합성: DESIGN_TOKENS.md에 근거/표 추가 + designTokenCheck 패턴 업데이트(새 literal 차단) 계획을 포함했는가?
5. 파급 영향: 기존 카드/리스트 항목 레이아웃 스냅샷, 다중 테마(라이트만 지원이지만 향후 다크 고려), 성능(ShadowLayer overdraw) 영향이 수용 가능함을 확인했는가?

모두 YES → Backlog(Elevation)에 제안 후 PR. 하나라도 NO → 기각/재검토.

## 개발 / 품질 참고
| 작업 | 명령 |
|------|------|
| Debug 빌드 | `gradlew.bat :app:assembleDebug` |
| Release 번들 | `gradlew.bat :app:bundleRelease` |
| Lint (Debug) | `gradlew.bat :app:lintDebug` |
| Lint Vital(Release) | `gradlew.bat :app:lintVitalRelease` |
| 유닛 테스트 | `gradlew.bat :app:testDebugUnitTest` |
| 디자인 토큰 검사 | `gradlew.bat :app:designTokenCheck` |

### 디자인 토큰 정적 검사(designTokenCheck)
`app/build.gradle.kts` 에 정의된 커스텀 태스크로 Alpha / Elevation 관련 금지 literal 을 substring 매칭하여 탐지합니다. `:app:check` 실행 시 자동 포함. 위반 시 GradleException 으로 실패하며 해결 방법 메시지를 출력합니다. 세부 패턴은 [DESIGN_TOKENS.md](./docs/DESIGN_TOKENS.md) 참고.

### 테스트 커버리지 확장 (현재 포함)
- DateOverlapUtils (기간 겹침, 경계, 비겹침)
- FormatUtils (일+시간 포맷, 경계 반올림, 잘못된 입력)
- PercentUtils (비율/반올림, 음수/경계)
- SobrietyRecord (퍼센트 계산, 레벨/타이틀, JSON 직렬화)

추가 제안:
- 통계(주/월/연속일) 계산 모듈 추출 & 순수 함수화 후 테스트
- 목표 달성 상태(isCompleted) 파생 로직 분리 테스트

## CI (예시 워크플로 개요)
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - name: Grant execute
        run: chmod +x gradlew
      - name: Build & Test
        run: ./gradlew clean :app:testDebugUnitTest :app:lintDebug --no-daemon
      - name: Assemble Release (dry)
        run: ./gradlew :app:bundleRelease -PVERSION_CODE=$(date +%Y%m%d) -PVERSION_NAME=1.0.0-dry --no-daemon || true
```

## CHANGELOG 정책
`CHANGELOG.md`는 Keep a Changelog 형식 유지. 기능/수정 PR 시 Unreleased 섹션에 항목 추가 후 릴리스 태그 시 섹션 분리.

## 환경변수 주입 방법(Windows Powershell 예)
```powershell
$env:VERSION_CODE=(Get-Date -UFormat %Y%m%d)
$env:VERSION_NAME="1.1.0"
$env:KEYSTORE_PATH="keystore.jks"
$env:KEYSTORE_STORE_PW="****"
$env:KEY_ALIAS="****"
$env:KEY_PASSWORD="****"
./gradlew.bat :app:bundleRelease
```

## 향후 확장 아이디어 (요약)
- 알림/리마인더, 위젯, 클라우드 동기화/백업
- Crash/Analytics 도입, In-App Review, 다국어 지원
- Jacoco 커버리지 리포트 & Detekt 정적 분석

## 라이선스
(추후 명시 예정)

## 기여
내부 프로젝트 기준이나 개선 아이디어(자동화, 분석, 접근성) 제안 환영. 문서/코드 변경 시 일관성 체크 후 PR 권장.

---
이 README는 모듈/아이콘 및 릴리스 품질 전략을 포함한 최신 버전입니다. 세부 디자인/배포/작업 흐름은 docs 디렉터리를 참조하세요.
