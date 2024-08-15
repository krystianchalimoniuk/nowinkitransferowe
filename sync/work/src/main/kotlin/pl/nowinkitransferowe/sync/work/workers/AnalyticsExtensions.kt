package pl.nowinkitransferowe.sync.work.workers

import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper

internal fun AnalyticsHelper.logSyncStarted() =
    logEvent(
        AnalyticsEvent(type = "network_sync_started"),
    )

internal fun AnalyticsHelper.logSyncFinished(syncedSuccessfully: Boolean) {
    val eventType = if (syncedSuccessfully) "network_sync_successful" else "network_sync_failed"
    logEvent(
        AnalyticsEvent(type = eventType),
    )
}

internal fun AnalyticsHelper.logPostNotificationWithImageStarted() =
    logEvent(
        AnalyticsEvent(type = "post_notification_with_image_started"),
    )

internal fun AnalyticsHelper.logDisplayingGeneralNotificationIsNotAllowed() =
    logEvent(
        AnalyticsEvent(type = "displaying_general_notification_is_not_allowed")
    )

internal fun AnalyticsHelper.logEmptyNotification() =
    logEvent(
        AnalyticsEvent(type = "notification_title_and_description_is_empty")
    )
internal fun AnalyticsHelper.logDownloadImageError(param: AnalyticsEvent.Param) =
    logEvent(
        AnalyticsEvent(type = "coil_download_image_error", extras = listOf(param))
    )


internal fun AnalyticsHelper.logPostNotificationWithImageFinished() {
    logEvent(
        AnalyticsEvent(type = "post_notification_with_image_finished" ),
    )
}

