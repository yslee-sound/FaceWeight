package com.example.alcoholictimer.core.util

/**
 * Play In-App Update API는 대상 버전의 versionName을 제공하지 않으므로
 * 우리 릴리스 정책(yyyymmddNN -> 1.x.y)에 따라 사용자 노출용 버전명을 매핑한다.
 * - 반드시 각 릴리스 시 versionCode -> versionName 매핑을 갱신하세요.
 * - 알 수 없는 코드일 경우 null을 반환하여 호출 측에서 폴백(코드 문자열)을 사용합니다.
 */
object UpdateVersionMapper {
    private val map: Map<Int, String> = mapOf(
        // 최신부터 과거 순으로 적재
        2025101001 to "1.0.7",
        2025100801 to "1.0.6"
    )

    fun toVersionName(versionCode: Int): String? = map[versionCode]
}

