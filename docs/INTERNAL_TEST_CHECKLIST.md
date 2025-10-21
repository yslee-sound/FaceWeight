// filepath: g:\Workspace\AlcoholicTimer\docs\INTERNAL_TEST_CHECKLIST.md
# 내부 테스트 최소 체크리스트 (AlcoholicTimer)

목적: Google Play Console "내부 테스트" 트랙에서 첫 빌드를 신속·정확하게 배포하고, 프로덕션 승격 전 필수 품질 신호(크래시 0, 핵심 퍼널 정상)를 확보하기 위한 **최소 실행 절차**를 정의.

상세/전체 릴리스 플로우는 `PLAY_STORE_RELEASE_WORKFLOW.md` 참조. 여기 문서는 "불필요한 것 걷어낸 10분 수행 카드" 용도.

---
## 0. 범위 & 전제
- 트랙: Internal Testing (최대 ~100명)
- 산출물: AAB (`:app:bundleRelease`)
- 서명: Play App Signing 사용 (업로드 키 준비 완료 가정)
- 시간 목표: 첫 빌드 업로드 ~ 테스터 설치 가능 상태까지 15~30분

---
## 1. TL;DR (슈퍼 최소 6단계)
1) versionCode 증가 & AAB 빌드  
2) 내부 테스트 새 릴리스 생성 & AAB 업로드  
3) 테스터 그룹 추가 & 링크 공유  
4) 앱 콘텐츠/정책 필수 항목 제출 상태(경고 0) 확인  
5) QA 핵심 시나리오 6항목 PASS  
6) 크래시/이벤트 기본 정상 → 승격 판단

---
## 2. 사전 점검 (실행 전 1분)
| 항목 | 확인 포인트 | OK 기준 |
|------|-------------|---------|
| applicationId | `kr.sweetapps.alcoholictimer` | 일치 |
| 버전 | `versionCode` 증가 | 이전 빌드보다 큼 |
| 타임존 | 빌드 PC 시간 | 시스템 시간 정상 |
| 정책 섹션 | Play Console > 앱 콘텐츠 | 빨간/노란 경고 없음 (또는 즉시 해결 가능) |
| 테스트 기기 | 1대 이상 | 설치/로그 확인 가능 |

---
## 3. 버전 & 빌드
`app/build.gradle.kts` 수정 (예: 날짜 기반 `yyyyMMddNN`).  
Windows (cmd / PowerShell 동일 실행 가능):
```bat
gradlew.bat clean :app:bundleRelease
```
산출물 경로:
```
app\build\outputs\bundle\release\app-release.aab
```

선택: 스크립트 존재 시 (예시)
```powershell
pwsh -File .\scripts\bump_version.ps1
```

---
## 4. 내부 테스트 릴리스 생성
1. Play Console → 테스트 → 내부 테스트 → 새 릴리스
2. AAB 업로드 (경고/에러 메시지 즉시 확인)
3. 릴리스 노트(간단):
```
초기 내부 테스트 – 핵심 타이머 플로우 검증
```
4. 저장 (아직 게시 전)

---
## 5. 테스터 구성
- 권장: Google 그룹 1개 (e.g. `alcoholictimer-testers@googlegroups.com`)
- 그룹/이메일 추가 → 저장 → "테스터에게 표시할 링크" 복사 → 공유
- 각 테스터: 링크 열기 → 참여(Opt-in) → Play 스토어 업데이트/설치 대기

---
## 6. 앱 콘텐츠 & 정책 필수 항목
| 섹션 | 상태 | 메모 |
|------|------|------|
| 개인정보처리방침 URL | 제출 | PRD와 일치 |
| 데이터 안전 (Data safety) | 초안이라도 제출 | 빈칸 경고 제거 |
| 타겟 연령/콘텐츠 등급 | 완료 | 경고 없음 |
| 광고 표시 여부 | 실제 상태대로 | 허위 미기재 금지 |

미완료 시: 내부 테스트 설치가 차단되거나 경고 발생 → 반드시 우선 해결.

---
## 7. 게시 & 활성화
- "검토" → "게시" 클릭
- 상태: 처리 중 → 활성 (수 분~수 시간; 첫 앱: 약간 더 지연 가능)
- 활성 후 테스터 기기 Play 스토어에서 설치(또는 업데이트) 노출 확인

---
## 8. 최소 QA (6 Step / 약 5~10분)
| Step | 체크 | 통과 기준 |
|------|------|-----------|
| 1 | 첫 실행 기본 목표 표시 | 크래시/레이아웃 깨짐 없음 |
| 2 | 목표 시작 (timer_start) | 진행 화면 정상 전환 |
| 3 | 앱 완전 종료 후 재실행 | 진행 상태 보존 |
| 4 | 다크 모드 전환 | 대비/색상 오류 없음 |
| 5 | 기기 언어 EN 변경 | 크래시 없음 (미번역 허용) |
| 6 | 기록/설정 화면 진입 | 주요 네비게이션 모두 동작 |

결과 기록 예:
```
QA PASS - 2025-10-05 versionCode=2025100503 Device=Pixel6(15)
```

---
## 9. 크래시 & 로그
즉시: `adb logcat | findstr E/` (Windows) 로 치명 오류 탐색.  
수 시간 후: Play Console > 품질 > 크래시 & ANR (표본 적어도 1~2 사용자).  
Crashlytics/Firebase 사용 시 DebugView 로 이벤트/크래시 확인.

---
## 10. Analytics (있을 경우)
ADB 디버그 모드 활성:
```bat
adb shell setprop debug.firebase.analytics.app kr.sweetapps.alcoholictimer
```
검증 이벤트 (ANALYTICS_EVENTS.md 참조):  
- timer_start (start_ts 유니크)  
- timer_complete 또는 timer_fail (둘 중 하나)  
- goal_set (설정 변경 시)  
중복 전송 / 상호 배타 규칙 위반 없는지 확인.

---
## 11. 테스터 피드백 포맷
테스터에게 안내 (복붙 템플릿):
```
[이슈 제목]
재현 단계: 1) ... 2) ... 3) ...
기대 결과: (정상 동작)
실제 결과: (관찰된 문제)
기기/OS: Pixel 6 / Android 15
스크린샷 or 로그: (선택)
빌드: versionCode=____
```
간단/명확/재현 가능성 확보 목적.

---
## 12. 승격(프로덕션 준비) 결정 기준 (GO / HOLD)
| 항목 | GO 조건 | HOLD 시 액션 |
|------|---------|--------------|
| 크래시 | 0 (또는 비치명 1건 미재현) | 원인 분석 & 핫픽스 |
| 핵심 퍼널 | 시작→완료/실패 플로우 정상 | 논리 오류/중복 이벤트 → 수정 |
| 목표 상태 복원 | 재실행 후 유지 | 세션 복원 실패 → 로컬 스토리지 점검 |
| UI 주요 깨짐 | 없음 (라이트/다크/EN) | 레이아웃 수정 후 재빌드 |
| Analytics 규칙 | 상호배타/중복 OK | 이벤트 가드 로직 패치 |

---
## 13. 다음 단계 (선택)
| 옵션 | 가치 | 트리거 시점 |
|------|------|--------------|
| Closed/Open 트랙 확장 | 표본 확대, Pre-launch report | 내부 QA PASS 후 |
| 단계적 프로덕션(10%→100%) | 위험 분산 | Crash/ANR 안정 시 |
| CHANGELOG / Git 태그 | 추적성 | 첫 내부 릴리스 직후 |
| BigQuery Export | 심화 분석 | 이벤트 안정 후 |

---
## 14. 명령 모음 (Windows 기준)
```bat
:: AAB 빌드
gradlew.bat :app:bundleRelease

:: Lint (필요 시)
gradlew.bat :app:lintVitalRelease

:: 테스트 (있을 경우)
gradlew.bat test

:: Firebase 디버그 뷰 활성
adb shell setprop debug.firebase.analytics.app kr.sweetapps.alcoholictimer
```

---
## 15. 문제 해결 Quick Guide
| 상황 | 즉시 조치 | 후속 |
|------|-----------|------|
| AAB 업로드 정책 경고 | 경고 메시지 클릭→필수 폼 보완 | 재업로드 불필요 (폼 저장 후) |
| 테스터 설치 안 보임 | Opt-in 링크 다시 열기 / 캐시 지우기 | versionCode 재확인 |
| 크래시 즉시 발생 | Logcat 스택 추출 → 근본 수정 → 새 빌드 | versionCode +1 권장 |
| 이벤트 중복 | Dedup 로직(메모리/로컬 캐시) 점검 | 재현 테스트 재수행 |

---
## 16. 문서 변경 이력
| 날짜 | 변경 | 작성자 |
|------|------|--------|
| 2025-10-05 | 최초 작성 (최소 내부 테스트 체크리스트) | - |

---
## 17. 역할 분리 (Developer vs Tester) 요약
| 단계 | Developer (개발자) | Tester (테스터) |
|------|--------------------|-----------------|
| 빌드/업로드 | versionCode 증가, AAB 빌드 & 업로드 | - |
| 정책 확인 | 앱 콘텐츠 경고 제거 | - |
| 설치 | 필요 시 링크 공유 | 링크 통해 Opt-in & 설치 |
| 최소 QA 실행 | (스모크 사전 확인) | 시나리오 1~6 실행 |
| 크래시 즉시 로그 | adb logcat 필터 실행 | (앱 튕기면 재현 단계만 메모) |
| Analytics 디버그 | 이벤트/중복 검사 | - |
| 피드백 양식 정리 | 템플릿 제공/취합 | 템플릿 채워 제출 |
| 승격 판단 | GO/HOLD 결정 | 체감 의견 전달 |
| 핫픽스 | 코드 수정 & 새 빌드 | 재테스트 |

간단 원칙:
- 테스터에게 ADB / 콘솔 접근 요구하지 않는다.
- 테스터는 "무엇을 했을 때 어떤 현상"만 빠르고 간결하게.
- 개발자는 증상 로그화를 자동화(필터 명령)하고, 승격 결정 근거 한 줄 기록.

---
## 18. 트랙 승급 개념 & FAQ (Internal → Closed → Open → Production)
**1) 승급(Promote)이란?**  이미 업로드·서명된 동일 AAB(같은 versionCode)를 다른 트랙으로 복사(재사용)하는 동작. 새 빌드 업로드 아님.

**2) 왜 계속 "버전 승급" 버튼이 보이나?**  대상 트랙(Closed, Open, Production)에 아직 그 versionCode가 *게시(Active)* 상태로 없거나, 다른 상위 트랙으로 더 올라갈 수 있기 때문. 정상.

**3) 실제 사용자에게 언제 노출되나?**  Production(프로덕션) 트랙에 게시(Active)된 뒤부터. 그 전(Internal/Closed/Open)은 초대 또는 링크 참여자로 제한.

**4) 트랙 우선순위 (한 사용자가 여러 트랙 참여 시)**  Production > Open > Closed(비공개) > Internal. 상위 트랙에 동일 앱이 있으면 하위 트랙 버전은 덮어써짐(사용자는 상위 트랙 빌드 받음).

**5) 반복 승급이 위험한 경우**  (a) Draft(임시) 릴리스 여러 개 쌓여 혼동, (b) Closed/Open 승급 후 즉시 새 Internal 빌드(versionCode+1) 올려 테스터가 서로 다른 versionCode 섞어서 피드백, (c) Production 직전 QA PASS 로그 없이 승급.

**6) 같은 versionCode를 여러 트랙에 둬도 되나?**  예. Internal → Closed → Production 순차적으로 동일 versionCode를 재사용(승급)하는 것은 정상. 단, *새 코드 수정 후에는 반드시 versionCode 증가*.

**7) 승급 vs 새 릴리스 차이**  새 릴리스 = 새로운 AAB 업로드(또는 다른 versionCode 선택). 승급 = 기존 AAB 재사용.

**8) 언제 승급을 멈추고 Production 가야 하나?**  QA PASS, S0/S1=0, 크래시 0, 정책 경고 0, 테스터 “No issues” 수령 → 즉시 Production 단계적(10%) 권장. 더 많은 기기 표본 필요하면 Closed → Pre-launch report 후 Production.

**9) Draft(임시) 릴리스가 남아 있으면?**  대상 트랙 승급 시 막힘. Draft 삭제(Discard) 또는 게시 후 진행.

**10) 승급 후 철회는?**  해당 트랙 릴리스 "중단" or 롤백 가능(Production은 이전 안정 릴리스로 롤백). Internal/Closed는 새 빌드 올려 대체.

**11) 자주 하는 실수 요약**  (1) versionCode 재사용(Play 거절), (2) Closed에 오래 머물며 피드백 없이 시간 소비, (3) Draft 방치로 승급 실패, (4) Production 직후 100% 즉시 배포(롤백 위험↑).

**12) 권장 기본 플로우(최소)**  Internal QA PASS → (선택) Closed 1회 표본 확대 → Production 10% → 24~48h 모니터 → 100%.
