# Analytics Events 정의 (AlcoholicTimer)

목적: 코어 KPI (활성/유지/성장/품질)를 설명하고, 행동 퍼널(설치 → 목표 시작 → 진행 유지 → 완료/재도전) 분석을 가능하게 하는 최소 이벤트 집합 정의.
도구 가정: Firebase Analytics (향후 BigQuery Export)
시간 표기: 모든 타임스탬프는 UTC. `actual_days` 계산은 (end - start)/86400 의 floor.
Naming Rule: `snake_case`, 파라미터 키 24자 이하.

---
## 1. 이벤트 목록 (우선 구현 순)
| 우선 | 이벤트 | 트리거 시점 | KPI 연계 | 비고 |
|------|--------|-------------|----------|------|
| 1 | app_open | 앱 포그라운드 진입 (Splash/첫 Compose) | DAU, MAU | 자동 수집(on) 가능 시 중복 방지 | 
| 2 | timer_start | 사용자가 금주 목표 시작 | Activation, StartedGoals | 기존 활성 목표 있으면 신규 시작 허용 여부 정책 반영 |
| 3 | timer_complete | 목표 기간 달성/완료 처리 | CompletionRate, MASD | 성공 상태 확정 시 단 한 번 |
| 4 | timer_fail | 목표 중단/실패 확정 | Fail Rate | 중복 방지 (complete와 상호 배타) |
| 5 | goal_set | 목표 기본값(일수) 변경 | Activation 품질, 목표 분포 | settings UI 종료 시 |
| 6 | streak_continue | 진행 목표 하루 경과(일 단위 갱신) | MASD/User | 하루 1회 사용자별(중복 방지) |
| 7 | streak_reset | 실패/중단/완료 후 새 목표로 Reset | 재도전/포기 패턴 | reset 조건 충족 즉시 |
| 8 | record_view | 기록 목록/상세 진입 | Engagement (분석용) | 파라미터로 view_type 구분 |
| 9 | settings_change | 설정 변경 (필드별) | 환경 튜닝 영향 | 민감 정보 직접 값 저장 금지 |
|10 | locale_change_detected | 앱 실행 중 로케일 변경 감지 | 번역/국제화 | 낮은 빈도 예상 |

---
## 2. 이벤트 상세 스키마

### 2.1 app_open
- 자동 수집(`screen_view`)과 중복될 수 있어 커스텀은 선택. 필요 시 단순 카운터.
- 파라미터: (없거나 최소)
  - `source` (optional: notification | widget | shortcut | null)

### 2.2 timer_start
| 파라미터 | 타입 | 예 | 설명 |
|----------|------|----|------|
| target_days | int | 30 | 목표 일수 |
| had_active_goal | string("true"/"false") | false | 기존 진행 목표 존재 여부 |
| start_ts | int (epoch_ms) | 1733424000000 | UTC 시작 시각 |

Validation:
- 동일 start_ts 로 2회 이상 전송 금지 (최근 24h dedupe cache)

### 2.3 timer_complete
| 파라미터 | 타입 | 예 | 설명 |
|----------|------|----|------|
| target_days | int | 30 | 설정 목표 |
| actual_days | int | 30 | 실 사용일 (floor) |
| start_ts | int | ... | 시작점 추적(불일치 감지) |
| end_ts | int | ... | 완료 시각 |
| success_type | string | full | 부분 성공/조기완료 구분 필요 시 (현재 `full` 고정) |

Rules:
- actual_days >= target_days 조건 만족 시 한 번 전송.

### 2.4 timer_fail
| 파라미터 | 타입 | 예 | 설명 |
|----------|------|----|------|
| target_days | int | 30 | 원래 목표 |
| actual_days | int | 5 | 진행된 일수 |
| fail_reason | string | manual_stop | manual_stop | relapse_mark | timeout_ui (추가) |
| start_ts | int | ... | 대응 start |
| end_ts | int | ... | 실패 확정 시각 |

Mutual Exclusion:
- 같은 start_ts 에 대해 `timer_complete` 와 동시 존재 금지.

### 2.5 goal_set
| 파라미터 | 타입 | 예 | 설명 |
| target_days | int | 45 | 새 기본 목표 |
| prev_target_days | int | 30 | 이전 값 |
| change_ts | int | ... | 변경 시각 |

### 2.6 streak_continue
| 파라미터 | 타입 | 예 | 설명 |
| streak_days | int | 12 | 현재 연속 진행 일수 |
| goal_target | int | 30 | 해당 목표 총 일수 |
| epoch_day | int | 20251005 | UTC 기준 일 (yyyyMMdd) |

Send Policy:
- 사용자 + epoch_day 기준 1회.

### 2.7 streak_reset
| 파라미터 | 타입 | 예 | 설명 |
| streak_days | int | 18 | 종료 직전 유지 일수 |
| reset_reason | string | completed | completed | failed | manual_stop |
| next_goal_in_hrs | int | 2 | 종료 후 새 목표 시작까지 시간 (재도전 분석용) |

### 2.8 record_view
| 파라미터 | 타입 | 예 | 설명 |
| view_type | string | month_list | month_list | year_list | all_list | detail |
| item_count | int | 12 | 리스트 로딩된 항목 수 (list 일 때) |
| record_id | string? | abcd1234 | detail일 때만 |

### 2.9 settings_change
| 파라미터 | 타입 | 예 | 설명 |
| field | string | theme_mode | 필드명 (goal_days, theme_mode 등) |
| change_type | string | update | update/reset |

(민감값은 직접 기록 금지: 예를 들어 복잡한 cost, 메모 텍스트 등)

### 2.10 locale_change_detected
| 파라미터 | 타입 | 예 | 설명 |
| new_locale | string | en_US | 변경된 로케일 |
| prev_locale | string | ko_KR | 이전 로케일 |

---
## 3. 파생 KPI 계산 규칙
- StartedGoals = count(timer_start)
- CompletedGoals = count(timer_complete)
- CompletionRate = CompletedGoals / StartedGoals
- FailRate = count(timer_fail) / StartedGoals
- ReattemptRate(72h) = distinct(users with timer_start within 72h after timer_complete) / distinct(users with timer_complete)
- Activation(24h) = distinct(users with timer_start within 24h install) / new_installs (Play Console ‘사용자 획득’)
- D7 Retention = (install_day 사용자 중 day+7에 app_open ≥1) / installs_day0
- MASD/User (향후) = Σ(streak_continue.streak_days 증가분) / MAU (또는 일 단위 누적 후 월간 집계)

---
## 4. 구현 체크리스트
| 단계 | 항목 | 상태 |
|------|------|------|
| 1 | Firebase SDK 추가 (Gradle) | TODO |
| 2 | 기본 이벤트 중복 검토 (app_open) | TODO |
| 3 | timer_start API 래퍼 구현 | TODO |
| 4 | 상태머신: start→(complete|fail) 단일 전이 보장 | TODO |
| 5 | dedupe 캐시 (start_ts) | TODO |
| 6 | streak_continue 일배치 or 앱 진입 시 계산 | TODO |
| 7 | QA: 동일 목표 중복 complete 방지 테스트 | TODO |
| 8 | BigQuery Export 활성화 (선택) | TODO |

---
## 5. 예시 Kotlin 래퍼(간단)
```kotlin
class AnalyticsLogger(private val fa: FirebaseAnalytics) {
    private fun log(name: String, bundle: Bundle.() -> Unit) {
        val b = Bundle().apply(bundle)
        fa.logEvent(name, b)
    }

    fun timerStart(targetDays: Int, hadActive: Boolean, startTs: Long) = log("timer_start") {
        putInt("target_days", targetDays)
        putString("had_active_goal", hadActive.toString())
        putLong("start_ts", startTs)
    }

    fun timerComplete(targetDays: Int, actualDays: Int, startTs: Long, endTs: Long) = log("timer_complete") {
        putInt("target_days", targetDays)
        putInt("actual_days", actualDays)
        putLong("start_ts", startTs)
        putLong("end_ts", endTs)
        putString("success_type", "full")
    }

    fun timerFail(targetDays: Int, actualDays: Int, reason: String, startTs: Long, endTs: Long) = log("timer_fail") {
        putInt("target_days", targetDays)
        putInt("actual_days", actualDays)
        putString("fail_reason", reason)
        putLong("start_ts", startTs)
        putLong("end_ts", endTs)
    }
}
```

---
## 6. 데이터 품질 수칙
- Complete & Fail 상호 배타: 상태 enum (ACTIVE, COMPLETED, FAILED) 단일 전이.
- 재전송 방지: 앱 종료/재시작 후 동일 상태 재로드 시 이미 완료 상태면 이벤트 재로깅 금지.
- 시계(Time) 불일치 보호: end_ts < start_ts 시 discard & error 로그.
- 오프라인 큐잉: Firebase 내부 큐 사용, 별도 재시도 로직 불필요.

---
## 7. 향후 확장 후보
| 후보 | 목적 |
|------|------|
| notification_action | 알림 참여도 | 
| share_action | 바이럴 채널 가설 검증 |
| backup_export | 장기 데이터 보존 기능 채택률 |
| level_up | 레벨 시스템 도입 후 진척 |

---
## 8. 버전 이력
| 날짜 | 변경 |
|------|------|
| 2025-10-05 | 최초 작성 |


