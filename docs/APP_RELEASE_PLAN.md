# AlcoholicTimer 출시 준비 및 배포 구현안

> 본 문서는 AlcoholicTimer 앱을 Google Play (또는 기타 스토어) 에 정식 배포하기 위한 "종합 체크리스트 + 실행 절차"를 단계별로 정리한 것입니다. 실제 운영 중 반복 재사용 가능하도록 **프로세스화** / **자동화 포인트** / **위험 요소** 를 함께 명시합니다.

---
## 1. 릴리스 목표 정의
| 항목 | 목표 |
|------|------|
| 대상 플랫폼 | Android (minSdk / targetSdk 는 app 모듈 build.gradle.kts 기준) |
| 배포 형태 | aab (App Bundle) 기본, 필요시 apk (QA / 사내 배포) |
| 첫 상용 버전 | v1.0.0 (Semantic Versioning) |
| 핵심 기능 동작 | 금주 기록 생성/조회/통계(주·월·년·전체), 목표 진행률/성공률, UI 정합성 |
| 안정성 지표(초판) | 크래시 없는 기본 플로우 / ANR 0 / 치명적 버그 0 |
| 성능 기준 | 첫 Cold Start < 2.5s (중저사양 기기), 스크롤 Drop Frame 5% 미만(프로파일링 참고) |
| 보안/프라이버시 | 외부 전송 민감 데이터 없음, 추후 Analytics 도입 시 동의 플로우 추가 예정 |

---
## 2. 버전/브랜치 전략
1. Git 브랜치
   - `main` : 항상 배포 가능한 상태(태그 기준).
   - `develop` : 통합 개발.
   - `feature/*` : 단위 기능.
   - `hotfix/*` : 출시 후 긴급 패치.
2. 태그 규칙
   - `vX.Y.Z` (예: `v1.0.0`).
   - 사전배포(내부 테스트): `v1.0.0-rc.1` 형식.
3. Version Code / Name
   - versionName = `X.Y.Z`.
   - versionCode = 날짜 + 증가(예: `20251005` → YYYYMMDD) 또는 누적 빌드번호 CI 증가.
4. 자동 업데이트 흐름 고려
   - CI 파이프라인에서 Git Tag push 트리거 → signed bundle 생성 → Play Console 업로드 API (추후 도입).

---
## 3. Gradle / 빌드 설정 점검
(실제 `app/build.gradle.kts` 를 참고하여 필요한 항목을 최종 확정)

체크리스트:
- [x] applicationId, minSdk, targetSdk, compileSdk 최신화 (compileSdk=36, targetSdk=36, minSdk=21)
- [x] 릴리스 빌드타입에 `minifyEnabled true`, `shrinkResources true` 적용
- [x] ProGuard / R8 rules (`proguard-rules.pro`) 불필요 경고 최소화 (2025-10-05 `--warning-mode all` 실행 결과 추가 경고 없음)
- [x] Kotlin / Compose Compiler 버전 최신 안정 (Kotlin 2.2.20 / Compose BOM 2025.09.01)
- [x] Jetpack Compose Metrics / Reports 분리 여부 결정 (미사용 유지 결정 2025-10-05)
- [x] Lint 설정: `./gradlew :app:lintVitalRelease` 통과 (2025-10-05 성공)
- [x] reproducible build (configuration-cache, build cache, parallel) 활성화

---
## 4. 서명(KeyStore) & 보안
| 항목 | 액션 |
|------|------|
| keystore 생성 | Android Studio Wizard 또는 keytool 사용 |
| 파일 보관 | 로컬(암호화 디스크) + 비밀 저장소(예: 사내 비밀 관리 Vault) |
| 서명정보 | `keystore.jks`, alias, storePassword, keyPassword (CI 에선 환경변수) |
| Gradle 설정 | `gradle.properties` 에 민감정보 두지 말 것 / `signingConfigs.release` 에서 System.getenv 사용 |
| 재발급 전략 | 분실 대비 백업 2곳 이상, 문서화 |

예시 (환경변수 사용) - build.gradle.kts (개념):
```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_PATH") ?: "")
        storePassword = System.getenv("KEYSTORE_STORE_PW")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

---
## 5. 코드 품질 & 정적 분석
| 도구 | 활동 |
|------|------|
| Lint | `gradlew.bat lintVitalRelease` → 오류 0 (미실행) |
| Detekt / ktlint (도입시) | 스타일 & 냄새 점검 |
| Dependency updates | 불필요 SNAPSHOT 제거, 안정 버전 잠금 (현재 모두 안정 버전) |
| 바이너리 크기 | bundle / mapping 파일 아카이브 |

미사용 리소스 / 중복 의존성 확인: `gradlew.bat :app:dependencies` / Android Studio Analyzer.

> 2025-10-05 업데이트
> - 시스템 바 처리: deprecated `statusBarColor`, `navigationBarColor`, contrastEnforced, dividerColor 직접 접근 제거 → `WindowCompat.setDecorFitsSystemWindows(false)` + `enableEdgeToEdge(SystemBarStyle.auto)` 로 대체.
> - Clipboard: `LocalClipboardManager` 는 deprecate 경고 존재. 현재 Compose 버전에서 새 `LocalClipboard` API 사용 시 메서드 미해결 문제로 보류. 추후 Compose 업그레이드(신규 API 포함 버전) 후 교체 & deprecation suppress 제거 예정.

---
## 6. 테스트 전략
1. 단위 테스트: `gradlew.bat :app:testDebugUnitTest` (DateOverlapUtils / FormatUtils / PercentUtils / SobrietyRecord 커버)
2. UI 간단 수동 시나리오 (금주 기록 플로우):
   - 기록 추가 → 리스트 반영 → 주간/월간 탭 전환 → 성공률, 평균/최대 지속일 검증 → 삭제/수정(있다면) → 재시작 후 상태 유지.
3. 경계 조건 수동:
   - 날짜 교차(주跨월), 0일짜리 잘못된 기록 없음, 목표일 < 실제일 처리.
4. 다국어/로케일 (현재 한국어 기준) → 12/24시, 타임존 변경 후 계산치 오류 여부.
5. 메모리/성능(선택): Android Studio Profiler 로 첫 실행, 화면 전환, GC 빈도 확인.
6. 크래시 로그(사전): Logcat clean state, StrictMode(선택)로 UI Thread Disk/Network 체크.

추가 예정: 주간 성공률/통계 집계 순수 함수화 & 단위 테스트.

---
## 7. 기능/UX 최종 점검 체크리스트
| 영역 | 확인 항목 | 상태 |
|------|-----------|------|
| 온보딩/첫 실행 | 초기 빈 상태 문구 정확 |  |
| 기록 생성 | 현재 시간/날짜 포맷 올바름 |  |
| 주간 성공률 | 7일 covering 시 100% 표시 |  |
| 월/년 통계 | 목표 진행률/평균/최대 정상 |  |
| 기간 선택 BottomSheet | 주/월/년 변경 반영 |  |
| 다크모드 (있다면) | 대비/가독성 |  |
| 접근성 | 터치 타겟 > 48dp, 텍스트 스케일 1.3배 레이아웃 깨짐 여부 |  |
| 회전/프로세스 킬 | 상태 복원 허용 범위 내 정상 |  |
| 에러 처리 | 예외 상황 LogCat 경고/크래시 없음 |  |

### 7.1 QA 실행 결과 기록 (릴리스 직전 채움)
| 항목 | 기기/환경 | 결과 | 메모 |
|------|-----------|------|------|
| 기본 플로우 (기록 추가~통계) | Pixel 6 / API 34 |  |  |
| 타임존 변경 (UTC+0) | Emulator |  |  |
| 회전/프로세스 킬 복원 | Pixel 6 |  |  |
| 접근성 폰트 확대 1.3x | Emulator |  |  |
| 성능 Cold Start <2.5s | 실제기기 |  |  |

---
## 8. 개인정보 / 정책 / 라이선스
| 항목 | 필요 여부 | 메모 |
|------|-----------|------|
| 개인정보 처리방침 | (분석/수집 없으면 간소화 문서) | 추후 Analytics 도입 전 기본 페이지 준비 권장 |
| 오픈소스 라이선스 화면 | 사용 라이브러리 표기(필요 시) | Gradle script로 generation 가능 |
| 광고 / IAP | 없음(현재) | 추가 시 Consent Flow 필요 |

---
## 9. 스토어 소재(메타데이터) 준비
| 항목 | 내용 |
|------|------|
| 앱 이름 | AlcoholicTimer (국문 현지화 고려) |
| 짧은 설명 | 금주 기록/성공률 트래커 |
| 전체 설명 | 주요 기능 bullet + 개인정보 미수집 명시 |
| 스크린샷 | 5~8장 (핵심 플로우: 홈, 주간 통계, 기록 상세, 추가 화면) |
| 아이콘 | Adaptive Icon 512x512 / Foreground, Background 준비 |
| 피처 그래픽(선택) | 1024x500 |
| 카테고리/콘텐츠 등급 | 건강 & 피트니스 (예시) |

---
## 10. 빌드 생성 & 산출물 아카이브
1. Clean & Bundle
   ```bash
   gradlew.bat clean :app:bundleRelease
   ```
2. 산출물 위치: `app/build/outputs/bundle/release/app-release.aab`
3. (옵션) APK 확인용:
   ```bash
   gradlew.bat :app:assembleRelease
   ```
4. 난독화 매핑 보관: `app/build/outputs/mapping/release/mapping.txt` → 내부 백업 저장소 업로드.
5. 해시 기록: `sha256sum app-release.aab` (Windows PowerShell: `Get-FileHash`).
6. 사이즈/메서드 수(선택): Android Studio Analyzer.

빌드 로그 스냅샷:
```
BUILD SUCCESSFUL in 1m 4s
69 actionable tasks: 67 executed, 2 up-to-date
Configuration cache entry stored.
```

---
## 11. 업로드 & 테스트 트랙 전략
| 트랙 | 목적 | 조건 |
|------|------|------|
| Internal Testing | 팀원 즉시 검증 | 1~10 Tester, 빠른 반복 |
| Closed (Alpha) | 소규모 확장 | 크래시 모니터링 |
| Open (Beta) | 공개 피드백 | 메트릭 안정화 후 |
| Production | 정식 배포 | 주요 KPI 통과 |

롤아웃 방식:
1. 10% staged → 24~48h 모니터링.
2. 크래시/리텐션/리뷰 모니터.
3. 문제 없으면 100% 확장.

---
## 12. 모니터링 & 피드백 루프
| 도구 | 초기 | 향후 |
|------|------|------|
| Crashlytics / Sentry | 도입 고려 (현재 미사용) | v1.1.x 에 편입 |
| Analytics (Firebase) | 금주 성공 흐름 이벤트 | 개인정보 고지 필요 |
| In-App Review | v1.0.1+ (안정화 후) | 시점: 3회 성공 또는 14일 유지 |

메트릭(초판 수집 후보):
- 활성 사용자(DAU/WAU), 금주 목표 달성률, 평균 세션 길이(선택), 재방문 주기.

---
## 13. 위험 요소 & 완화 방안
| 위험 | 영향 | 완화 |
|------|------|------|
| 날짜 계산/타임존 버그 | 성공률/통계 왜곡 | 단위 테스트 + 다른 타임존(UTC+0) 기기 수동 테스트 |
| 다중 기록 중복 로직 회귀 | 성공률 100% 오표시 | 주간 성공률 계산 함수 별도 분리/테스트 |
| Keystore 분실 | 업데이트 불가 | 2중 백업 + 접근권한 최소화 |
| 릴리스 빌드 ProGuard 누락 | 앱 크기 증가 | 빌드 스크립트 점검 CI enforced |
| 크래시 미탐지 | 사용자 이탈 | Crashlytics 조기 도입(선택) |

---
## 14. CI/CD (선택 도입 설계 초안)
1. GitHub Actions / GitLab CI 파이프라인 예시 단계:
   - checkout → JDK 세팅 → 캐시 복원 → `./gradlew lint test assembleDebug` (PR)
   - 태그 push (`v*`): `./gradlew bundleRelease` + 서명 + AAB 업로드 (Play Developer Publishing API)
   - mapping.txt & aab 아카이브 → Release Assets 업로드
2. Secrets: KEYSTORE_BASE64, KEYSTORE_STORE_PW, KEY_ALIAS, KEY_PASSWORD, PLAY_API_JSON.
3. 워크플로 실패 기준: 테스트 실패, lint 에러, assemble 실패.

---
## 15. 반복 가능한 "릴리스 실행 스크립트" 초안 (수동)
1. Git: `git switch main && git pull`.
2. 버전 증가 (build.gradle.kts): versionName / versionCode 수정.
3. CHANGELOG.md 업데이트 (없다면 생성) → 변경사항 bullet.
4. 커밋 & 태그: `git commit -am "chore: release v1.0.0"` → `git tag v1.0.0` → `git push && git push --tags`.
5. 빌드: `gradlew.bat clean bundleRelease`.
6. 결과물 검증: 사이즈 / 실행 / 기본 시나리오.
7. Play Console 업로드 (Internal Testing Track).
8. Reviewer QA 통과 → 트랙 승격.
9. mapping.txt, aab, 태그 링크 문서화.
10. 배포 후 24~48시간 모니터링 (크래시 / 리뷰 / 평점 / 사용자 피드백).

---
## 16. 향후 개선 로드맵 (v1.1.x+ 제안)
| 우선순위 | 개선 | 기대 효과 |
|----------|------|-----------|
| 높음 | Crashlytics & Analytics 통합 | 안정성/사용 패턴 파악 |
| 높음 | 단위 테스트 커버리지 확대(날짜/통계 계산) | 회귀 방지 |
| 중간 | 다크 모드 대응 (이미 있다면 품질 개선) | UX 향상 |
| 중간 | 접근성 (TalkBack 라벨, 콘트라스트) | 포용성, 스토어 품질 지표 향상 |
| 낮음 | 다국어(영/일) 지원 | 사용자 폭 확대 |
| 낮음 | In-App Review Flow | 리뷰 수 증가 |
| 낮음 | Clipboard 신규 API(LocalClipboard) 도입 | Deprecated 경고 제거 & suspend 지원 |

---
## 17. 최종 릴리스 체크 요약 (CONDENSED)
(모든 항목 OK 시에만 배포)
- [ ] Git main 최신 & tag 예정 버전 반영
- [x] versionCode / versionName 업데이트 (1.0.0 / 2025100502)
- [x] Lint / Test / Build 성공 (서명 빌드 후 재확인 완료)  
- [x] 릴리스 AAB 서명 & 실행 검증 *(jarsigner verified)*
- [x] ProGuard mapping 보관 *(final mapping.txt 백업 완료)*
- [ ] QA 수동 시나리오 패스 (기본 플로우 기기 확인 후 체크)
- [x] 스토어 메타데이터 / 스크린샷 업로드 (사용자 보고 완료)
- [x] 정책(privacy)/아이콘/카테고리 설정 완료 (사용자 보고 완료)
- [ ] 태그 & CHANGELOG 게시
- [ ] 모니터링 계획 수립 (v1.0.0 배포 직후 간단 관찰 계획 추가)

> 최종 Signed AAB SHA256: `07f85c47e88ab216c48a6d721d94b2584dbbdbfc7b158a825958ef51abd751bf`  
> versionCode 전략: yyyymmdd + 2자리 시퀀스 → 2025100502 (다음 재빌드 시 2025100503)

### 내부 테스트 (Minimal)
1. 서명된 AAB Internal Testing 업로드
2. 설치 → 목표 설정 → 진행 화면 진입 OK
3. 강제 종료 후 재실행(진행 상태 유지) 확인
4. 크래시/에러 로그 없음

---
## 18. 부록: 기본 명령 모음 (Windows)
```bash
# 클린 + 릴리스 번들
gradlew.bat clean :app:bundleRelease

# 릴리스 APK (사이드로드)
gradlew.bat :app:assembleRelease

# 유닛 테스트
gradlew.bat :app:testDebugUnitTest

# Lint (릴리스 중요 이슈)
gradlew.bat :app:lintVitalRelease

# 전체 의존성 트리 확인
gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath
```

---
## 19. 문서 유지보수 규칙
| 변경 유형 | 액션 |
|----------|------|
| 빌드 스크립트 구조 변경 | 본 문서 Gradle 관련 섹션 갱신 |
| 새 배포 자동화 도입 | CI/CD 절 추가/수정 |
| 정책/분석 기능 추가 | 개인정보/스토어 메타데이터 섹션 갱신 |

---
**마무리**

위 체크리스트를 기준으로 첫 배포(v1.0.0)를 실행한 뒤, 문제가 없으면 이 문서를 저장소에 커밋하여 향후 반복 배포 시 재사용하세요. 추가 개선이나 자동화가 필요하면 로드맵 섹션을 기반으로 우선순위를 정해 진행하면 됩니다.
