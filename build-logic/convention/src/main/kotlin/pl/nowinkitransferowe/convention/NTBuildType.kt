package pl.nowinkitransferowe.convention


/**
 * This is shared between :app and :benchmarks module to provide configurations type safety.
 */

enum class NTBuildType(val applicationIdSuffix: String? = null) {
    DEBUG(".debug"),
    RELEASE,
}
