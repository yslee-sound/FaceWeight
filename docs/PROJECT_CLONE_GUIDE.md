# 프로젝트 복제/리네임 가이드 (Windows/cmd)

이 문서는 현재 저장소를 통째로 복사해 새 Android 프로젝트로 분기(복제)하고, 식별자와 이름을 변경하는 전 과정을 요약합니다.

## 체크리스트 요약
- 폴더 복사: 빌드 산출물/캐시/IDE 설정은 제외하고 새 폴더로 복사
- Git 초기화: 기존 .git 제거 후 새 저장소로 초기화 및 원격 연결
- 식별자/이름 변경: rootProject.name, applicationId, namespace, 패키지명, 앱 이름
- 서명/배포 스크립트 갱신: keystore 경로/비밀, 스크립트 하드코딩 경로
- 외부 연동 점검: Firebase/Crashlytics/딥링크/인앱 업데이트 등
- 클린 및 빌드: Sync → Clean → assembleDebug로 동작 확인

> 참고: 아래 명령은 Windows cmd 기준입니다. PowerShell이 아닌 cmd.exe에서 실행하세요.

---

## 1) 폴더 복사 (빌드/캐시 제외)
아래 명령은 원본을 새 경로로 복사하면서, 빌드/캐시/IDE 디렉터리를 제외합니다.

```bat
robocopy "G:\Workspace\AlcoholicTimer" "G:\Workspace\MyNewApp" /E /XD .git .gradle build app\build .idea
```

- `/E`: 하위 디렉터리(빈 폴더 포함) 복사
- `/XD`: 지정한 디렉터리 제외
- 대상(`G:\Workspace\MyNewApp`)이 이미 있다면 비우거나 다른 새 이름을 사용하세요.

## 2) Git 초기화 (새 프로젝트로 관리)
```bat
cd /d "G:\Workspace\MyNewApp"
rmdir /S /Q .git
git init
git add .
git commit -m "Initialize MyNewApp from AlcoholicTimer template"
```
원격 저장소를 연결하려면:
```bat
git branch -M main
git remote add origin <새_원격_URL>
git push -u origin main
```

## 3) 프로젝트 이름/식별자 변경
필수 변경 포인트는 다음과 같습니다.

- 루트 프로젝트 이름: `settings.gradle.kts`
  ```kotlin
  rootProject.name = "MyNewApp"
  ```

- 앱 모듈 Gradle(KTS): `app/build.gradle.kts`
  - `namespace = "com.example.mynewapp"`
  - `defaultConfig { applicationId = "com.example.mynewapp" }`
  - `versionCode`/`versionName`을 새 프로젝트 정책에 맞게 초기화(예: `1`, `"1.0.0"`)

- 패키지명 리팩터(소스 트리):
  - Android Studio에서 `app/src/main/java`의 최상위 패키지 우클릭 → Refactor → Rename → Rename package
  - import/Manifest/테스트 코드까지 일괄 변경 적용
  - 최신 Gradle에서는 `namespace`가 기준입니다. `AndroidManifest.xml`의 `package` 속성이 남아 있다면 정리하여 불일치가 없도록 합니다.

- 앱 표시 이름: `app/src/main/res/values/strings.xml`
  - `app_name` 값을 새 이름으로 변경

- 리소스/아이콘(선택): 앱 아이콘, 색상, 테마 등 브랜드 요소 업데이트

## 4) 서명/배포/스크립트 갱신
- `app/build.gradle.kts`의 `signingConfigs`에서 keystore 경로/alias/비밀번호 확인 및 갱신
- `scripts/*.ps1` 및 루트의 배포 스크립트(예: `build_release.ps1`, `release_pipeline.ps1`)에 하드코딩된 경로/앱 아이디가 있다면 새 경로/값으로 업데이트
- `docs/RELEASE_SIGNING.md`, `docs/PLAY_STORE_RELEASE_WORKFLOW.md` 등 문서의 앱명/패키지/스토어 설정 값 업데이트
- Google Play에 신규로 배포하려면 기존 앱과 **다른** `applicationId`를 사용해야 합니다.

## 5) 외부 서비스/기능 점검
- Firebase/Crashlytics 사용 시 `google-services.json`을 새 프로젝트/패키지로 재발급하여 교체
- 딥링크/앱 링크/인앱 업데이트 설정의 패키지/호스트 확인 (본 저장소는 `docs/IN_APP_UPDATE_IMPLEMENTATION.md` 및 정책 문서를 포함)
- Analytics 이벤트/키 값 등 추적 체계를 새 프로젝트 정책에 맞게 재검토

## 6) 클린 및 빌드/실행
```bat
cd /d "G:\Workspace\MyNewApp"
gradlew.bat clean assembleDebug
```
- Android Studio에서 Sync 후 Run. 패키지명 변경으로 인한 import/리소스 참조 에러가 없는지 확인하세요.

## 7) 흔한 이슈/팁
- `BuildConfig.APPLICATION_ID`와 코드 상 패키지명은 다를 수 있습니다. 딥링크/외부 연동은 보통 `applicationId` 기준이므로 혼동 주의.
- IDE 캐시(`.idea`)와 Gradle 캐시(`.gradle`)는 복사하지 않는 것이 안전합니다. 처음 열 때 Sync로 자동 재생성됩니다.
- 앱 내 표기(앱 정보 화면, 라이선스 고지, Privacy Policy 링크 등)도 새 프로젝트에 맞춰 업데이트하세요.
- 초기 버전은 가능한 단순하게(기본 테마/아이콘) 두고, 빌드 성공 확인 후 점진적으로 브랜드 요소를 변경하면 위험을 줄일 수 있습니다.

---

## 부록: 빠른 되돌리기/검증
- 문제가 생기면 Git 초기 커밋으로 되돌린 뒤(또는 새 복제부터) 단계별로 변경하고 빌드가 깨지는 지점을 좁혀가세요.
- CI가 있다면 새 저장소에 기본 빌드 워크플로우를 먼저 설정하여 이상 유무를 자동으로 확인하세요.

이 문서를 따라 진행하면 기존 저장소를 안전하게 복제해 독립적인 새 Android 앱 프로젝트를 만들 수 있습니다.
