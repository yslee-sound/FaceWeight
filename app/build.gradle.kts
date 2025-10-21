import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// release 관련 태스크 실행 여부 (configuration 시점에 1회 계산)
// bundleRelease / assembleRelease / publishRelease / 끝이 Release 인 태스크 포함
val isReleaseTaskRequested: Boolean = gradle.startParameter.taskNames.any { name ->
    val lower = name.lowercase()
    ("release" in lower && ("assemble" in lower || "bundle" in lower || "publish" in lower)) || lower.endsWith("release")
}

android {
    namespace = "com.example.alcoholictimer" // 코드 패키지 구조는 유지 (선택)
    compileSdk = 36

    // 버전 코드 전략: yyyymmdd + 2자리 시퀀스 (NN)
    // 이전 사용: 2025100800 -> 신규: 2025100801
    val releaseVersionCode = 2025101900
    val releaseVersionName = "1.0.8"

    defaultConfig {
        applicationId = "kr.sweetapps.alcoholictimer" // 변경: Play Console에서 com.example.* 금지 대응
        minSdk = 21
        targetSdk = 36
        versionCode = releaseVersionCode
        versionName = releaseVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // Play Console 경고 대응: 네이티브 심볼 업로드용 심볼 테이블 생성 (FULL 은 용량↑)
            debugSymbolLevel = "SYMBOL_TABLE"
        }
    }

    signingConfigs {
        // 환경변수 기반 release 서명 (키 미설정 시 경고만 출력 -> 로컬 debug 빌드 영향 X)
        create("release") {
            val ksPath = System.getenv("KEYSTORE_PATH")
            if (!ksPath.isNullOrBlank()) {
                storeFile = file(ksPath)
            } else {
                println("[WARN] Release keystore not configured - will build unsigned bundle. Set KEYSTORE_PATH before production release.")
            }
            storePassword = System.getenv("KEYSTORE_STORE_PW") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            // 릴리스 번들 최적화: 코드/리소스 축소 (ProGuard/R8)
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 서명 강제: 실제 release 관련 태스크(assembleRelease/bundleRelease 등) 요청 시에만 검사
            val hasKeystore = !System.getenv("KEYSTORE_PATH").isNullOrBlank()
            if (isReleaseTaskRequested && !hasKeystore) {
                throw GradleException("Unsigned release build blocked. Set KEYSTORE_PATH, KEYSTORE_STORE_PW, KEY_ALIAS, KEY_PASSWORD env vars before running a release build.")
            }
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        // debug 설정 변경 없음
    }

    // Java/Kotlin 타깃 유지
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // 필요시 buildConfig true (기본 true) / viewBinding 등 미사용
    }

    lint {
        // 릴리스 치명적 이슈 CI fail fast
        abortOnError = true
        warningsAsErrors = false // 초기 온보딩: 경고는 유지, 필요시 true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.app.update.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    // org.json (Android 내장) 를 JVM 유닛 테스트 환경에서 사용하기 위한 의존성
    testImplementation("org.json:json:20240303")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// signingReport 대안: 서명 환경변수 및 키스토어 존재 여부를 출력하는 헬퍼 태스크
// 구성 캐시 문제로 signingReport 가 실패할 때 빠르게 상태를 확인하는 용도
tasks.register("printReleaseSigningEnv") {
    group = "help"
    description = "Prints release signing env vars and keystore file existence"
    doLast {
        val ksPath = System.getenv("KEYSTORE_PATH")
        val alias = System.getenv("KEY_ALIAS")
        val hasStorePw = !System.getenv("KEYSTORE_STORE_PW").isNullOrEmpty()
        val hasKeyPw = !System.getenv("KEY_PASSWORD").isNullOrEmpty()
        println("KEYSTORE_PATH=${ksPath ?: "<not set>"}")
        if (!ksPath.isNullOrBlank()) {
            val f = file(ksPath)
            println(" - exists=${f.exists()} size=${if (f.exists()) f.length() else 0}")
        }
        println("KEY_ALIAS=${alias ?: "<not set>"}")
        println("KEYSTORE_STORE_PW set=${hasStorePw}")
        println("KEY_PASSWORD set=${hasKeyPw}")
    }
}

// (단순화) designTokenCheck 커스텀 태스크 제거.
// 필요 시 별도 스크립트나 독립 Gradle 플러그인/CI 스텝으로 수행 권장.
