# Changelog

모든 눈에 띄는 변경 사항은 이 파일에 기록됩니다.
형식: [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/) & Semantic Versioning.

## [Unreleased]
### Added
- (예정) Crashlytics / Analytics 도입
- (예정) 날짜/통계 계산 단위 테스트 확대
- 문서: `docs/MODAL_BARRIER_AND_INPUT_GUARD.md` (드로어/모달 “완전한 모달 배리어” 설계·구현·QA 가이드)
- 프롬프트: `docs/MODAL_BARRIER_PROMPT.txt` (다른 앱에서 재사용 가능한 입력 가드 구현 지시문)
- 문서: `docs/IN_APP_UPDATE_TROUBLESHOOTING.md` (In‑App Update 데모/다이얼로그/스플래시 관련 트러블슈팅 및 QA 체크리스트)
- 문서: `docs/TARGET_DAYS_PICKER.md` (목표 일수 3자리 가로 다이얼 전환 가이드)

### Changed
- 접근성 개선 (터치 타깃/콘트라스트)
- Base 화면: 드로어 열림/애니메이션/닫힘 직후 입력 가드 적용(그레이스 타임 포함)으로 모달 배리어 일관성 강화
- Start 화면: In‑App Update 데모 트리거(제목 탭/롱프레스, 인텐트 `demo_update_ui`) 연결, `AppUpdateDialog` 실제 렌더링 추가, 업데이트 정보 상태(`updateInfo`, `availableVersionName`) 보관
- Start 화면: 목표 일수 입력 방식을 “숫자 입력”에서 “가로 3자리 다이얼 바텀시트(백/십/일)”로 전환

### Fixed
- 상세 화면: 기록 삭제가 되지 않던 버그(JSON 키 불일치) 수정
- In‑App Update(API 30): 업데이트 다이얼로그 전후로 스플래시가 한 번 더 보이던 현상 제거(API<31 경로에서 Compose 오버레이/지연 제거 + windowBackground 즉시/지연 제거 병행)
- In‑App Update: Composable 문맥 밖 `stringResource(...)` 호출로 인한 컴파일 오류(@Composable invocations) 수정
- 리소스: `strings.xml` 말줄임표/Lint 경고 정리 및 깨진 한글 복구

## [1.0.1] - 2025-10-14
### Added
- 문서: `docs/INSETS_AND_IME_GUIDE.md`에 "드로어 + IME" 안정화 정책 및 스니펫 추가
- 프롬프트: `docs/INSETS_AND_IME_PROMPT.txt`에 드로어 오픈 시 포커스 해제/키보드 숨김, 드로어 시트 패딩, 입력 가드 타이밍 반영

### Changed
- BaseActivity: 드로어 오픈(버튼/제스처) 시 즉시 포커스 해제 + 키보드 숨김, 드로어 시트에 status/navigation bars 패딩 추가, 입력 가드 타이밍 보강
- About 화면: 흰색 카드로 그룹화, 회색 배경 대비 강화

### Fixed
- 입력 중 드로어 오픈 시 레이아웃 튐/겹침/배경 클릭 스루 가능성 축소

## [1.0.0] - 2025-10-05
### Added
- 금주(금주 시작~현재) 기록 생성/조회 기본 플로우
- 주/월/년/전체 통계 화면(성공률, 목표 진행률 등)
- 목표 진행률/성공률 계산 로직(기초)
- 기본 SplashScreen 적용
- Jetpack Compose UI 구조(Material3)

### Changed
- Release 빌드: R8 minify / resource shrink 활성화
- Gradle: configuration cache, build cache, parallel build 활성화

### Security
- 서명정보 환경변수 기반 로딩(로컬 미설정 시 unsigned 빌드 허용)

## 형식 가이드
버전 태그 예: v1.0.0
- Added: 새 기능
- Changed: 기존 기능 변경(비 호환 여부 PR에서 명시)
- Deprecated: 곧 제거될 항목
- Removed: 제거된 항목
- Fixed: 버그 수정
- Security: 취약점 관련 또는 보안 영향 변경

[Unreleased]: https://example.com/compare/v1.0.1...HEAD
[1.0.1]: https://example.com/releases/v1.0.1
[1.0.0]: https://example.com/releases/v1.0.0
