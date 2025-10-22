# FaceWeight MVP 개발 세부 구현 계획서

> 문서 버전
> - 버전: v1.0.0
> - 최근 업데이트: 2025-10-21
> - 변경 요약: 초판 작성 — 범위/아키텍처/데이터 스키마/매칭 파이프라인/UI 플로우/의존성/스프린트/성능/테스트/보안/리스크/리포 구조/백로그 수록
>
> 변경 이력(Changelog)
> - v1.0.0 (2025-10-21)
>   - 최초 작성

---

## 0) 문서 목적/대상
- 목적: 기획안(닮은꼴 체형 매칭 기반 가상 체중 제시)을 Android 온디바이스 MVP로 구현하기 위한 단계별 실무 계획을 제공
- 대상: Android 앱 개발자, 기획자, QA, 디자이너(아이콘·자산 연계)

---

## 1) 범위(스코프)와 목표
- 온디바이스 처리(네트워크 불필요), 기본적으로 사진은 디스크에 저장하지 않음(옵션 토글 제공 가능)
- 단일 사용자·단일 얼굴만 지원, 정면 또는 약간 기울어진 얼굴
- 결과는 사전 구축된 샘플 얼굴 데이터베이스와의 유사도 기반 “가상 체중” 제시
- 시뮬레이션: ±5kg/±10kg 버튼으로 인접 샘플 이미지로 전환(초기: 크로스페이드, 차기: 워핑)
- 법적/커뮤니케이션 포지셔닝: 엔터테인먼트 목적의 가상 수치 제시(의학적 판단 아님)

성공 기준(KPI)
- 처리 시간: 1MB 기준 End-to-End 800ms 이내(중급 기기), 매칭 계산 < 10ms(@N≤1,000)
- 얼굴 검출 성공률: 실사용 샘플에서 90% 이상(가이드 제공 포함)
- Top-1 매칭 정확도(내부 GT 기준): ≥ 70%

---

## 2) 아키텍처 및 패키지 구조
- 모듈: app 단일 모듈(MVP)
- 패키지 구조(예시)
  - core/data: 샘플 리포지토리, 모델, 로더
  - core/ml: 얼굴 검출/랜드마크 정규화/벡터화/매칭
  - core/ui: 공통 UI 구성요소, 테마
  - feature/capture: 사진 촬영/선택
  - feature/match: 매칭 진행(로딩)
  - feature/result: 결과/시뮬레이션
  - feature/about: 소개/라이선스
- 네비게이션(초기): 단일 액티비티 + Compose Navigation 또는 3 액티비티 분리
  - Capture → Match(처리) → Result(결과)
- DI: Hilt(리포지토리/매처/설정 주입)

---

## 3) 샘플 데이터베이스 설계
저장 위치(앱 번들 자산)
- 인덱스: `app/src/main/assets/face_samples/index.json`
- 썸네일: `app/src/main/assets/face_samples/images/*.jpg`

JSON 스키마(요지)
- id: String
- weightKg: Int
- meta: { gender?: String, ageRange?: String }
- landmarks: List<[x, y]> — 정규화 전 원시 좌표 또는 정규화 좌표 중 하나로 통일
- thumbnailPath: String

정규화 규칙(권장)
- 평행이동: 양안(눈) 중심을 원점(0,0)으로 이동
- 스케일: 양안 간 거리로 정규화(기준 거리 = 1)
- 회전: 눈 중심을 연결하는 선을 수평(θ=0)으로 회전 보정

특징 벡터(Feature)
- 정규화된 (x,y) 평탄화 → FloatArray(length = 2 × N)
- 선택 파생 특징: 턱선 각도, 볼 곡률 등(차기)

성능 메모리 추정
- N=1,000, 점수=100 → 200 float/샘플 ≈ 800B → 전체 ≈ 0.8MB (+ 썸네일 지연 로드)

---

## 4) 매칭 파이프라인(온디바이스)
입력 → 처리 → 출력
1) 입력: 비트맵(촬영/갤러리)
2) 얼굴 검출: ML Kit Face Detection(랜드마크/컨투어 활성화)
3) 전처리: 단일 얼굴 선택, 기울기 보정, 필요 시 마진 크롭
4) 랜드마크 추출: 주요 포인트 좌표 수집(실패 시 사용자 가이드 노출)
5) 정규화: 평행이동/스케일/회전 보정 일괄 적용
6) 벡터화: FloatArray로 변환
7) 매칭: 코사인 유사도 Top‑K(기본 5) → 조건 가중치(성별/연령대 선택) → 최종 1개
8) 시뮬레이션 후보: 최종 매치 주변의 ±5/±10kg 샘플 미리 계산
9) 출력: {bestMatch, similarity(0..1), weightKg, neighbors}

오류 처리/엣지 케이스
- 얼굴 미검출: 재촬영 유도(빛/정면/카메라 거리 팁)
- 다중 얼굴: 가장 큰 얼굴 선택(면적 기준)
- 과도한 기울기: 재촬영 가이드
- 랜드마크 일부 누락: 보간 또는 실패 처리

---

## 5) UI 플로우/화면 설계(Compose 기준)
- Capture 화면
  - CameraX 미리보기/촬영, 갤러리 선택(ActivityResultContracts)
  - 권한: CAMERA, READ_MEDIA_IMAGES(33+), 하위는 READ_EXTERNAL_STORAGE
  - 실패/가이드: 얼굴 미검출 시 안내 토스트/다이얼로그
- Match 화면
  - 로딩 인디케이터, 처리 프로그레스(스켈레톤/로거)
- Result 화면
  - 텍스트: "XX kg 샘플과 YY% 유사"
  - 시각: 사용자 사진 vs 샘플 썸네일 비교 또는 탭 전환
  - 시뮬: ±5kg/±10kg 버튼 → 인접 샘플로 크로스페이드 전환
  - 액션: 재시도, 공유(옵션), About 이동
- About 화면
  - 버전/오픈소스 라이선스 안내

접근성/현지화
- 콘텐츠 설명(콘텐츠 설명자), 폰트 스케일 대응, 다크 모드 지원, ko 기본/다국어 확장 여지

---

## 6) 기술 스택/의존성
- ML: ML Kit Face Detection
- 카메라: CameraX
- UI: Jetpack Compose(Material3)
- JSON: Kotlinx Serialization 또는 Moshi
- DI: Hilt
- 테스트: JUnit, Robolectric, Instrumentation

빌드/설정
- minSdk 24+, target 최신
- ProGuard/R8: ML Kit/JSON 모델 관련 keep 규칙 추가
- 디버그: 샘플 100장 제한, 릴리스: 전체 샘플 활성화

---

## 7) 단계별 구현 계획(스프린트)
스프린트 1: 프로젝트 골격/입력
- 의존성 추가, 권한 플로우
- Capture 화면: 촬영/갤러리 선택, 이미지 리사이즈(최대 1080px)
- 샘플 데이터 로더(index.json 파서, 메모리 캐시)
- DoD: 이미지 입력 성공, 인덱스 로드 성공, 기본 네비게이션 연결

스프린트 2: 얼굴 검출/정규화
- ML Kit 적용, 단일 얼굴 선택 정책
- 정규화(평행이동/스케일/회전) + 벡터화 유닛 테스트
- DoD: 동일 이미지 재시도 시 유사 벡터 재현, 처리 400ms 이내(중급 기기)

스프린트 3: 매칭/결과 UI
- 코사인 유사도 Top‑K + 조건 가중치 + 최종 선택
- Result 기본 UI(유사도/체중/썸네일)
- DoD: 내부 테스트 셋 Top‑1 ≥ 70%, 안정적 결과 표시

스프린트 4: 시뮬레이션/폴리싱
- ±5/±10 버튼, 인접 샘플 탐색/크로스페이드 애니메이션
- 오류/엣지 UX, 접근성, 크래시/ANR 점검, 간단 i18n
- DoD: 오프라인 E2E 800ms 이내, 전 플로우 품질 기준 충족

---

## 8) 성능/메모리 가이드
- 입력 이미지는 1080px로 다운스케일 후 처리
- 매칭은 O(N) 코사인(NEON 최적화는 차기), N≤1,000에서 <10ms 목표
- 메모리: 특징 벡터 ~1MB 이내, 썸네일은 지연 로드/해제
- 스레딩: 얼굴 검출/전처리 I/O와 분리(코루틴/디스패처)

---

## 9) 품질/테스트 전략
- 유닛 테스트: 정규화/유사도 계산(회전·스케일 불변성), JSON 파싱
- 계측 테스트: 실제 기기 얼굴 검출 성공률, 처리 시간 로깅
- 스냅샷 테스트: Compose UI(Result 상태들)
- 리그레션: 샘플 업데이트 시 Top‑K 결과 안정성 확인
- 수동 QA 체크리스트: 입력 실패/권한 거부/다중 얼굴/어두운 환경/고해상도 이미지

---

## 10) 개인정보/보안 정책(MVP)
- 모든 처리 온디바이스, 기본적으로 사진 디스크 미저장(옵션 제공 시 명시)
- 공유 기능 사용 시 워터마크/고지("엔터테인먼트 목적")
- 크래시 리포팅 도입 시 이미지/바이너리 업로드 금지

---

## 11) 리스크와 대응
- 얼굴 검출 실패 → 가이드 UI/재촬영 유도/갤러리 대안
- 데이터 편향 → 다양한 샘플 구성, 조건 가중치(선택)
- 시뮬레이션 품질 낮음 → V1 크로스페이드, V2 워핑(삼각형 분할/MediaPipe Face Mesh)
- 성능 병목 → 이미지 리사이즈/비용 높은 연산 백그라운드/캐시 최적화

---

## 12) 리포 구조/파일 계획(예시)
- `app/src/main/assets/face_samples/index.json` — 샘플 인덱스
- `app/src/main/java/com/sweetapps/faceweight/core/ml/FaceFeatureExtractor.kt`
- `app/src/main/java/com/sweetapps/faceweight/core/ml/FaceMatcher.kt`
- `app/src/main/java/com/sweetapps/faceweight/core/data/FaceSampleRepository.kt`
- `app/src/main/java/com/sweetapps/faceweight/feature/capture/CaptureActivity.kt`
- `app/src/main/java/com/sweetapps/faceweight/feature/match/MatchActivity.kt`
- `app/src/main/java/com/sweetapps/faceweight/feature/result/ResultActivity.kt`

---

## 13) 차기 버전 백로그(V2+)
- 얼굴 워핑(삼각형 분할 기반 이미지 워핑)
- MediaPipe Face Mesh 채택(더 조밀한 특징점)
- 온디바이스 임베딩 모델(TFLite) 기반 고차원 특징
- 다중 얼굴 지원, 히스토리/저장, 간단 AB 테스트

---

## 14) 참고/연계 문서
- 앱 아이콘 규격 치트시트: `docs/a_APP_ICON_QUICK_SPEC.md`
- 아이콘 상세 가이드: `docs/a_APP_ICON_AND_EXPORT_GUIDE.md`
- UI/스타일 프롬프트: `docs/a_FLAT_UI_BASE_PROMPT.md`

---

## 부록 A) 데이터 스키마 예시(JSON)
```json
{
  "id": "SAMPLE_0001",
  "weightKg": 68,
  "meta": { "gender": "U", "ageRange": "20s" },
  "landmarks": [[0.12, -0.03], [0.08, 0.01] /* ... */],
  "thumbnailPath": "face_samples/images/SAMPLE_0001.jpg"
}
```

## 부록 B) 성공 판단 체크리스트(요약)
- [ ] 오프라인 상태에서도 전체 플로우 동작
- [ ] 1MB 이미지 E2E 처리 800ms 이내(중급 기기)
- [ ] 얼굴 검출 실패 시 사용자 가이드 적절히 노출
- [ ] 결과 화면에서 유사도/가상 체중/썸네일 정상 표기
- [ ] ±5/±10 버튼 시 인접 샘플로 자연스러운 전환(크로스페이드)
- [ ] 개인정보 처리 원칙(온디바이스, 미저장) 충족

