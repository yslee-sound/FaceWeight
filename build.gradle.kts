// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Gradle 구성 캐시 정리 (AGP/Gradle 업데이트 후 캐시 충돌 시 사용)
tasks.register("purgeConfigCache") {
    group = "help"
    description = "Deletes .gradle/configuration-cache in the root project"
    doLast {
        val dir = file(".gradle/configuration-cache")
        if (dir.exists()) {
            if (dir.deleteRecursively()) {
                println("[purgeConfigCache] Deleted: ${dir}")
            } else {
                println("[purgeConfigCache] Failed to delete: ${dir}. Try closing IDE/daemons and re-run.")
            }
        } else {
            println("[purgeConfigCache] Nothing to delete. ${dir} not found.")
        }
    }
}
