# 패키지/폴더 구조 리팩터링 실행 계획(Refactoring Plan)

마지막 업데이트: 2025-10-04 (레거시 스텁 정리 완료)

목표
- 불일치한 패키징(components에 Activity 혼재 등)을 정리해 가독성/확장성/탐색성을 개선
- 기능(Feature) 기준 패키징으로 일관성 확립(권장) + 안전한 단계적 이동

성공 기준(체크리스트)
- [x] 모든 Activity가 기능 패키지(feature.*)에 위치하고 Manifest가 정확히 가리킨다
- [x] 재사용 컴포넌트는 core/ui/components로, 기능 전용 컴포넌트는 해당 feature로 이동
- [x] BaseActivity 등 코어는 core.*로 정리되고 전역 의존 방향은 feature -> core
- [x] 빌드/런/린트가 문제 없이 통과하고 내비게이션이 정상 동작
- [x] (추가) 레거시 루트 스텁/placeholder 파일과 구 디렉터리(components, ui, utils)의 물리 삭제 완료

현재 요약(문제 포인트)
- 루트: 여러 Activity + BaseActivity + LevelDefinitions 혼재 (→ 제거됨)
- components/ui/utils: 혼재 구조 (→ core/* 및 feature/* 로 이관 후 제거)

타깃 구조(Feature-by-package 권장)
```
com.example.alcoholictimer
├─ core
│  ├─ ui            (BaseActivity, StandardScreen, theme, 재사용 컴포넌트)
│  ├─ model         (SobrietyRecord)
│  ├─ data          (RecordsDataLoader)
│  └─ util          (PercentUtils, FormatUtils, DateOverlapUtils, Constants)
└─ feature
   ├─ start         (StartActivity)
   ├─ run           (RunActivity, QuitActivity)
   ├─ records       (RecordsActivity, AllRecordsActivity, 레코드용 컴포넌트/화면)
   ├─ detail        (DetailActivity, DetailStatCard)
   ├─ level         (LevelActivity, LevelDefinitions)
   ├─ settings      (SettingsActivity)
   ├─ profile       (NicknameEditActivity)
   └─ addrecord     (AddRecordActivity, addrecord 전용 컴포넌트)
```

실행 결과 요약(2025-10-04 최종)
- Manifest 정합성 확보 (모든 Activity: feature.*)
- core/* 및 feature/* 구조 정착
- 레거시 루트 Activity/유틸/컴포넌트 스텁 물리 삭제 및 sourceSets exclude (안전망) 적용
- Lint/구조 점검 완료

검증
- `gradlew.bat :app:assembleDebug` 성공
- `gradlew.bat :app:lintDebug` 성공
- 주요 사용자 흐름 수기 테스트 완료

완료 기준(최종 체크)
- [x] 패키지 트리 상단에 core, feature만 보임
- [x] Manifest는 FQCN만 사용하며 모든 Activity 경로가 유효
- [x] Lint 중대 경고 없음
- [x] 앱 주요 흐름 정상 동작
- [x] 레거시 스텁 물리 삭제

---
## 2025-10-04 후속 정리 (완료 기록)

### 조치 내역
- Gradle 커스텀 태스크 `:app:removeLegacyStubs` 및 sourceSets `exclude` 로 안전 삭제 → 이후 스텁/디렉터리 제거
- 삭제 대상: BaseActivity.kt, LevelActivity.kt, LevelDefinitions.kt, NicknameEditActivity.kt, QuitActivity.kt, RunActivity.kt, SettingsActivity.kt, StartActivity.kt, components/, ui/, utils/
- 빌드 & Lint 재확인 후 이상 없음

### 재발 방지
- 새 공통 코드 반드시 core/* 하위에만 생성
- Activity 신규 추가 시 feature/<name> 전용 패키지 사용
- (선택) CI 단계에서 legacy 경로 감지 스크립트 추가 가능 (예: git grep "com.example.alcoholictimer.*Activity" | grep -E "src/main/java/com/example/alcoholictimer/[^c|f]")

### 참고
- 추후 multi-module 분리 시 core 모듈 추출 용이 상태
- 문서 히스토리 유지 위해 본 섹션은 삭제하지 않고 상태만 “완료”로 업데이트

---
## FAQ (요약)
Q. 왜 예전에 두 벌로 보였나?  
A. 이전 위치 안내용 스텁 파일이 package 그대로 남아 Android 뷰에서 중복 노출.

Q. 지금은?  
A. 물리 삭제 및 exclude 적용으로 중복 사라짐.

Q. 똑같은 문제가 다시 생기면?  
A. 루트 패키지에 새로운 Activity/유틸이 생겼는지 git diff / Android 뷰 확인. 발견 시 즉시 feature/core 로 이동.

---
(문서 끝)
