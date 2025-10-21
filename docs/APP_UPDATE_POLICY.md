# App Update Policy (In-App Updates)

문서 목적: AlcoholicTimer 앱의 인앱 업데이트 정책을 명확히 정의하고, 구현/운영 시 일관성을 유지합니다. 정책은 Google Play 정책을 준수하며, 사용자 경험을 최우선으로 합니다.

---

## 1) 범위(Scope)
- 대상 기능: Google Play In-App Updates API (play-core-ktx)
- 적용 화면: `StartActivity` (앱 시작 시점)
- 적용 버전: 1.0.4부터

참고 구현:
- 매니저: `core/util/AppUpdateManager.kt`
- UI/호출부: `feature/start/StartActivity.kt`
- 다이얼로그: `core/ui/components/AppUpdateDialog.kt`

---

## 2) 업데이트 유형 정책
- 기본: Flexible Update
  - 사용자 선택 가능, 백그라운드 다운로드, 설치는 재시작 시점 사용자 동의 필요
- 조건부: Immediate Update
  - 다음 조건을 모두 만족할 때만 시도
    1. 사용자가 업데이트를 3회 연기(MAX_POSTPONE_COUNT=3)
    2. Google Play가 Immediate를 허용함(`AppUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)`)
  - 위 조건 중 2가 거짓이면 Flexible Update로 폴백(닫기 불가 상태 유지)

근거: Immediate는 Google Play에서 업데이트 우선도/신선도(staleness) 등으로 허용 여부를 판단합니다. 앱은 허용 시에만 요청합니다.

---

## 3) 사용자 프롬프트/연기 정책
- 다이얼로그 구성: 버전/메시지 + "나중에" "업데이트"
- ‘나중에’ 버튼 정책(중요):
  - 버튼은 항상 노출한다.
  - 연기 불가 상태(canDismiss=false)에서는 버튼을 "비활성화"로 표시한다(숨기지 않음).
  - 다이얼로그 바깥 터치로 닫기는 canDismiss=false일 때 차단한다.
- 연기(Postpone) 정책:
  - 사용자가 "나중에" 선택 시 연기 횟수를 +1 저장(SharedPreferences)
  - 연기 횟수 < 3: 다이얼로그는 닫기 가능(canDismiss=true)
  - 연기 횟수 ≥ 3: 다이얼로그는 닫기 불가(canDismiss=false)
    - Immediate 허용 시: 즉시 업데이트 요청
    - Immediate 미허용 시: Flexible 진행(닫기 불가)

- 다운로드 완료 후 설치: 스낵바에 "다시 시작" 액션을 제공하고, 사용자가 눌렀을 때만 `completeFlexibleUpdate()`로 설치 완료(자동 재시작)

---

## 4) 업데이트 확인 주기(체크 주기)
- 앱 시작 시 강제 확인(`forceCheck = true`)
  - 사유: 진행 중인 업데이트 재개, Play 정책 기반 허용/차단 반영을 즉시 수용
  - 사용자 피로 방지: 업데이트가 실제로 "사용 가능"할 때에만 다이얼로그 노출(연기 정책/닫기 불가 조건 포함)
- 서버/스토어 제어: 어떤 플로우(Flexible/Immediate)가 가능한지는 Google Play가 제공하는 `isUpdateTypeAllowed`로 제어됨

주의: 과도한 방해를 피하기 위해, 중요하지 않은 릴리스는 Play Console에서 낮은 우선도로 설정하여 Immediate 허용을 제한합니다.

---

## 5) UX/접근성 요구사항
- 명확성: 다이얼로그와 스낵바 메시지는 간결하고 정확해야 함
- 사용성: Flexible 기본, Immediate는 최소화(Play 허용 시에만)
- 접근성: 버튼 대비/크기 준수, 키보드 탐색/스크린리더 읽기 가능
- 시스템 바 부작용 방지: Preview/미리보기 시 시스템 바 스타일 변경 비활성화(`applySystemBars=false`)

---

## 6) 수명주기/리스너 관리
- 설치 상태 리스너 등록: `registerInstallStateListener { ... }`
- 화면 종료 시 해제: `unregisterInstallStateListener()` (Compose `DisposableEffect`에서 호출)

---

## 7) 데이터 보존/초기화
- SharedPreferences 키: `app_update_prefs`
  - `last_update_check_time`: 마지막 확인 시간(정책상 강제 확인 사용 시 참조하지 않음)
  - `update_postponed_count`: 사용자 연기 횟수(최대 3)
- 초기화: 필요 시 `resetPostponeCount()`로 0으로 리셋(예: 긴급 업데이트 완료 후)

---

## 8) 오류 처리/엣지 케이스
- Play 설치가 아님(Sideload): 업데이트 정보 없음 → 다이얼로그 미노출
- 업데이트 진행 중(`DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS`): 즉시 재개 시도
- 다운로드 실패: 리스너에서 오류 로그, 사용자 방해 없이 앱 사용 계속
- 용량/네트워크 제약: Flexible 우선으로 사용자 경험 보호

---

## 9) QA 체크리스트(내부 테스트 트랙 권장)
- [ ] 업데이트 있음 → 다이얼로그 노출 확인
- [ ] "업데이트" 선택 시 Flexible 다운로드 진행 확인
- [ ] 다운로드 완료 → 스낵바 노출 및 "다시 시작" 클릭 시 설치 완료 확인
- [ ] "나중에" 3회 반복 → 4회차에 닫기 불가 확인(버튼은 비활성화로 표기)
- [ ] Immediate 허용 시(Play에서 설정): 4회차에 Immediate 플로우 진입 확인
- [ ] Immediate 미허용 시: 닫기 불가 Flexible로 폴백 확인
- [ ] 리스너 등록/해제 누수 없음(화면 이동/종료 시 로그 확인)

---

## 10) 정책 준수 매핑(Google Play)
- 공식 API 사용: ✅ play-core-ktx(AppUpdateManager)
- 사용자 선택권: ✅ Flexible 기본, 스낵바 액션으로 자발적 설치 완료
- 강제 업데이트 사용 최소화: ✅ Play가 Immediate 허용하는 경우로 제한
- 투명성: ✅ 명확한 메시지/버전 정보 제공
- 외부 업데이트 금지: ✅ Play 경로 이외 미사용

참고:
- 개발자 문서: https://developer.android.com/guide/playcore/in-app-updates
- 정책 가이드: https://support.google.com/googleplay/android-developer/answer/9898842

---

## 11) 롤아웃/운영 가이드
1) 버전 증가: `app/build.gradle.kts`의 `versionCode`, `versionName` 갱신
2) 번들 빌드: Android App Bundle(AAB) 생성 및 Play Console 업로드
3) 내부 테스트 트랙 출시: "검토 및 출시" 완료 상태 확인
4) 실기기 검증: 테스트 기기에서 앱 실행 → 다이얼로그/스낵바 플로우 확인
5) 모니터링: 업데이트 수락률, 크래시, 피드백 모니터링

---

## 12) 부록(관련 문서)
- 구현 상세: `docs/IN_APP_UPDATE_IMPLEMENTATION.md`
- 정책 리뷰: `docs/IN_APP_UPDATE_POLICY_REVIEW.md`
- 테스터 배포: `docs/TESTER_ROLLOUT_GUIDE.md`
