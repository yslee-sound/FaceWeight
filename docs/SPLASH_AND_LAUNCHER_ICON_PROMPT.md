# 스플래시 + 런처 아이콘 — 단일 표준 프롬프트(프로젝트 적용본)

> 문서 버전
> - 버전: v1.1.0
> - 최근 업데이트: 2025-10-20
> - 변경 요약: Pre-Android 12(API 30-)에서 installSplashScreen(compat) 전역 호출 금지 정책을 명시하고, 런타임 규칙/트러블슈팅/체크리스트를 해당 정책과 일치하도록 업데이트
>
> 변경 이력(Changelog)
> - v1.1.0 (2025-10-20)
>   - API 30-에서 installSplashScreen 미사용을 명문화(비런처 화면 compat 인플레이트로 인한 InflateException 재발 방지)
>   - 공통 런타임 규칙, 내부 네비게이션, 트러블슈팅, 체크리스트 갱신
> - v1.0.0 (초판)
>   - 스플래시/런처 아이콘 표준 초안 정립(31+ Theme.SplashScreen, 30- layer-list, 288dp 전용 벡터 등)
>
> 버전 규칙
> - Semantic Versioning 준수: MAJOR(호환성 깨짐)/MINOR(기능·정책 추가)/PATCH(오타·경미한 정정)
> - 문서 내용이 변경될 때 상단 버전/날짜/요약, 하단 변경 이력을 함께 업데이트합니다.

<!-- Canonical source for all apps sharing the same base. Reuse across modules/projects via the template variables below. -->

이 문서는 스플래시 화면과 런처 아이콘을 일관되게 구현/유지하기 위한 단일 기준입니다. 그대로 복사·실행 가능한 “AI 에이전트용 프롬프트”와, 본 저장소에 이미 적용된 실제 경로/정책을 함께 제공합니다.

---

## 0) 목표
- Android 12+(API 31+)과 11-(API 30-)에서 스플래시 크기·배경·애니메이션을 일관화
- 런처 아이콘은 플랫폼 권장 세이프존(전경 인셋 18dp)을 따르고, 스플래시 아이콘 크기와 분리 관리
- 반복 이슈(원형 테두리, 기기간 크기 차이, 잔상)를 구조적으로 차단
- 동일 베이스를 공유하는 여러 앱/모듈에 “템플릿 변수”만 바꿔 재사용 가능

핵심 요약
- 31+: Theme.SplashScreen을 부모로 사용, `windowSplashScreenIconBackgroundColor` 미설정(원형 컨테이너 방지), AnimatedIcon은 Vector/PNG/WEBP만
- 30-: 스플래시는 layer-list로 흰 배경 + 중앙 288dp 전용 벡터를 직접 그림, 메인 테마는 흰 배경만
- 런처 전경 인셋: 18dp(표준), 스플래시 전용 리소스와 분리
- 최소 표시시간 800ms, 31+ 퇴장 애니메이션 220ms
- 중요: API 30-에서는 installSplashScreen(compat) 호출을 전역에서 사용하지 않음. 스플래시는 테마 layer-list로만 표시(비런처 화면에서 compat 뷰 인플레이트로 인한 크래시 방지)

---

## 1) 재사용 프롬프트(그대로 복사해서 실행)
아래 프롬프트를 AI 에이전트에 붙여넣으면, 지정한 모듈(들)에 스플래시/아이콘 구성을 자동 적용·수정합니다.

```
내 안드로이드 모노레포/다중 앱에 스플래시/런처 아이콘 표준을 적용해줘. 아래 정책을 모든 대상 모듈에 일관되게 반영하고, 빌드까지 검증해.

[템플릿 변수]
- APP_MODULES: 대상 application 모듈 배열 (기본: ['app'])
- THEME_BASE: 메인 테마 이름 (기본: Theme.NoSmokeTimer)
- THEME_SPLASH: 스플래시 테마 이름 (기본: Theme.NoSmokeTimer.Splash)
- DRAWABLE_SPLASH_LAYER: 스플래시 layer-list 드로어블 이름 (기본: splash_screen)
- DRAWABLE_SPLASH_ICON: 스플래시 중앙 아이콘 컨테이너 이름 (기본: splash_app_icon)
- DRAWABLE_SPLASH_LARGE: 288dp 전용 전경 벡터 이름 (기본: splash_foreground_288)
- LAUNCHER_FOREGROUND_FILES: 런처 전경 벡터 파일 글롭 (기본: drawable-anydpi-*/ic_launcher_foreground.xml)
- ICON_COLORS: { fgColorRef: @color/icon_launcher_fg, bgColorRef: @color/icon_launcher_bg }

[정책]
1) Android 12+(API 31+) 스플래시
   - values-v31/themes.xml의 스플래시 테마 부모는 Theme.SplashScreen을 사용.
   - windowSplashScreenAnimatedIcon=@drawable/${DRAWABLE_SPLASH_LARGE} (Vector/PNG/WEBP만; InsetDrawable 금지)
   - windowSplashScreenBackground=@android:color/white
   - postSplashScreenTheme=@style/${THEME_BASE}
   - android:windowSplashScreenIconBackgroundColor 는 "설정하지 않는다"(중요)

2) Android 11-(API 30-) 스플래시
   - 메인 테마(android:windowBackground)는 항상 @android:color/white (잔상 방지)
   - 스플래시 테마에서만 android:windowBackground=@drawable/${DRAWABLE_SPLASH_LAYER} 설정
   - ${DRAWABLE_SPLASH_ICON} 은 ${DRAWABLE_SPLASH_LARGE}(288dp 벡터)를 직접 그림 (Vector/PNG/WEBP만)

3) 런처 아이콘(Adaptive)
   - ${LAUNCHER_FOREGROUND_FILES}의 전경 벡터가 18dp 세이프존(인셋) 기준을 만족하도록 확인
     (필요시 InsetDrawable 래퍼를 사용하되, Compose painterResource 대상으론 사용하지 않음)
   - 모노크롬/배경은 유지, 색상은 ${ICON_COLORS.fgColorRef}/${ICON_COLORS.bgColorRef}

4) 공통 런타임 규칙
   - API 31+에서만 BaseActivity.installSplashScreen() 호출: setKeepOnScreenCondition 으로 최소 800ms 보장
   - API 30-에선 installSplashScreen(compat) 호출 금지: 테마 windowBackground 스플래시만 사용
   - API 31+ 퇴장 애니메이션: 220ms, fade-out + scale 1.05 적용

5) 내부 네비게이션에서 스플래시 생략(옵션)
   - API 30-: 기본적으로 installSplashScreen을 사용하지 않으므로 내부 이동 시 스플래시 재등장 없음
   - (선택) 런처로만 적용: 내부 이동 시 putExtra("skip_splash", true)를 사용하고, 필요 시 런처 화면에서 첫 프레임 처리로 잔상 제거

6) 파일/경로(모듈당)
   - res: values*/themes.xml, drawable/${DRAWABLE_SPLASH_LAYER}.xml, drawable/${DRAWABLE_SPLASH_ICON}.xml, drawable/${DRAWABLE_SPLASH_LARGE}.xml, ${LAUNCHER_FOREGROUND_FILES}
   - 코드: BaseActivity(or Application) 위치

[에이전트 절차(이드empotent)]
A. 모듈 탐색: settings.gradle 및 각 모듈 build.gradle(.kts)에서 'com.android.application' 플러그인 적용 모듈을 APP_MODULES로 결정(사용자 지정 값을 우선) 
B. 각 모듈에 대해:
   1) values-v31/themes.xml 에서 스플래시 테마 부모를 Theme.SplashScreen으로 일치; windowSplashScreenIconBackgroundColor 제거
   2) values, v23, v29 의 스플래시 테마에서 windowBackground=@drawable/${DRAWABLE_SPLASH_LAYER} 설정; 메인 테마 windowBackground=@android:color/white 유지
   3) drawable/${DRAWABLE_SPLASH_ICON}.xml 이 ${DRAWABLE_SPLASH_LARGE}를 직접 그리도록 구성(미존재 시 생성)
   4) ${DRAWABLE_SPLASH_LARGE}.xml (288dp) 생성/갱신; path는 런처 아이콘과 동일
   5) ${LAUNCHER_FOREGROUND_FILES}의 전경이 18dp 세이프존을 만족하는지 확인(벡터 경계/패스 또는 Inset 래퍼로 보정)
   6) BaseActivity 에 (31+) installSplashScreen + 최소 800ms 유지 + 220ms 퇴장 애니메이션/(30-) 미호출 적용
C. 빌드: gradlew :<module>:assembleDebug -x lint 를 모듈별 실행
D. 검증: Pixel 4a(API 30)·Pixel 7 Pro(API 36)에서 스플래시 크기 동등성/런처 과대 표시 없음 확인

[트러블슈팅]
- API 30-에서 AndroidX SplashScreen compat가 비런처 화면에서 인플레이트되면 테마 속성 누락으로 InflateException이 발생할 수 있음 → installSplashScreen은 31+에서만 호출하거나, 런처 액티비티로 범위를 한정
- Compose Image(painterResource)는 InsetDrawable을 지원하지 않음 → Vector/PNG/WEBP 사용
- 31+: 스플래시 캐시 → 앱 삭제 후 재설치
- 런처 아이콘 캐시 → 런처 앱 캐시 삭제 또는 기기 재부팅

---

## 2) 본 저장소의 현재 적용 상태(최신)
- 31+ 스플래시 테마: Theme.SplashScreen 부모 사용, 원형 배경 속성 미설정, AnimatedIcon은 288dp Vector(`@drawable/splash_foreground_288`)
  - 파일: `app/src/main/res/values-v31/themes.xml`
- 30- 스플래시 테마: layer-list 배경(@drawable/splash_screen)으로 중앙 아이콘 표시, 메인 테마는 흰 배경
  - 파일: `values/`, `values-v23/`, `values-v29/`의 `Theme.NoSmokeTimer.Splash`
- Pre-12 중앙 아이콘: 288dp 전용 벡터 사용
  - 파일: `drawable/splash_app_icon.xml` → 내부에 `@drawable/splash_foreground_288`
  - 파일: `drawable/splash_foreground_288.xml` (크기: 288dp)
- 런처 아이콘 전경: 18dp 세이프존 기준으로 설계된 전경 벡터(`drawable-anydpi-v21/ic_launcher_foreground.xml`, `drawable-anydpi-v26/ic_launcher_foreground.xml`)
- 공통 런타임 규칙: (31+) 최소 800ms 보장 + 220ms 퇴장 애니메이션, (30-) BaseActivity에서는 installSplashScreen 미사용
  - 파일: `core/ui/BaseActivity.kt`
- Start 화면 워터마크(배경 장식): 공용 스크린의 backgroundDecoration 슬롯을 사용, Vector를 직접 그려 Compose 충돌 회피
  - 파일: `core/ui/StandardScreen.kt`, `feature/start/StartActivity.kt`

---

## 3) 반복 이슈의 근본 원인과 회피법
- InsetDrawable 사용 금지(Compose): painterResource 대상으론 Vector/PNG/WEBP만 허용 → 워터마크/장식은 Vector 사용
- 원형 테두리(31+): Theme.SplashScreen 사용 시 `?windowSplashScreenIconBackgroundColor`가 개입될 수 있으므로 해당 속성 미설정
- 기기간 스플래시 크기 차이: 31+는 표준(배경 없음 288dp), 30-는 앱 리소스 좌우 → Pre-12는 288dp 전용 벡터를 직접 그림
- 런처 아이콘 과대 표시: 전경 세이프존 18dp 유지(벡터 경계/패스 또는 Inset 래퍼로 보정)
- 잔상/겹침: 메인 테마 windowBackground 에 스플래시 레이어 사용 시 전환 후 잔상 → 메인 테마는 흰색만, 스플래시는 스플래시 테마에만 사용
- Pre-31 compat 스플래시 전역 호출: 비런처 화면에서도 compat 뷰가 인플레이트되어 속성 미정의로 크래시 유발 가능 → (31+)에만 호출하거나 런처 화면으로 범위 제한

---

## 4) 점검 체크리스트(에이전트용)
- [ ] values*/themes.xml: 정책 반영(31+ 부모/배경, 30- layer-list, 메인 테마 흰 배경)
- [ ] drawable/${DRAWABLE_SPLASH_ICON}.xml → ${DRAWABLE_SPLASH_LARGE} 참조
- [ ] ${DRAWABLE_SPLASH_LARGE}.xml → width/height=288dp, path 최신
- [ ] ${LAUNCHER_FOREGROUND_FILES} → 18dp 세이프존 충족
- [ ] BaseActivity → (31+) installSplashScreen + keepOnScreenCondition(>=800ms) + 퇴장 애니메이션 / (30-) installSplashScreen 미호출
- [ ] assembleDebug 모듈별 성공
- [ ] 기기 캡처: 30/36 크기 동등, 런처 과대 표시 없음

---

## 5) 빠른 실행 커맨드(Windows cmd)
```cmd
G:\Workspace\NoSmokeTimer\gradlew.bat --no-daemon --no-configuration-cache --console plain :app:assembleDebug -x lint
```
- 다중 모듈 예시: `:appA:assembleDebug :appB:assembleDebug`

---

## 6) 참고 리소스(본 저장소)
- 스플래시
  - `app/src/main/res/drawable/splash_screen.xml`
  - `app/src/main/res/drawable/splash_app_icon.xml`
  - `app/src/main/res/drawable/splash_foreground_288.xml`
- 런처
  - `app/src/main/res/drawable-anydpi-v21/ic_launcher_foreground.xml`
  - `app/src/main/res/drawable-anydpi-v26/ic_launcher_foreground.xml`
- 테마
  - `app/src/main/res/values/themes.xml`
  - `app/src/main/res/values-v23/themes.xml`
  - `app/src/main/res/values-v29/themes.xml`
  - `app/src/main/res/values-v31/themes.xml`
- 코드
  - `app/src/main/java/com/sweetapps/nosmoketimer/core/ui/BaseActivity.kt`
  - `app/src/main/java/com/sweetapps/nosmoketimer/core/ui/StandardScreen.kt`
  - `app/src/main/java/com/sweetapps/nosmoketimer/feature/start/StartActivity.kt`

---

## 7) 운영 팁
- Android 12+는 스플래시 캐시가 간헐적으로 남을 수 있음 → 앱 삭제 후 재설치로 최신 리소스 반영 확인
- 홈 런처 아이콘 캐시는 런처 앱 캐시를 비우거나 재부팅으로 동기화
- 색상 변경은 `@color/icon_launcher_fg/bg`만 바꾸면 전역 적용
