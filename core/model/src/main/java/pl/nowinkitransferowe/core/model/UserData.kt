package pl.nowinkitransferowe.core.model

data class UserData(
    val bookmarkedNewsResources: Set<String>,
    val viewedNewsResources: Set<String>,
    val bookmarkedTransferResources: Set<String>,
    val viewedTransferResources: Set<String>,
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val isNewsNotificationsAllowed: Boolean,
    val isTransfersNotificationsAllowed: Boolean,
    val isGeneralNotificationAllowed: Boolean
)
