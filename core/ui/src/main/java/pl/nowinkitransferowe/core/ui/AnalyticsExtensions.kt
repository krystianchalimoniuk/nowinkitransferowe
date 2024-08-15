package pl.nowinkitransferowe.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.analytics.LocalAnalyticsHelper

/**
 * Classes and functions associated with analytics events for the UI.
 */
fun AnalyticsHelper.logScreenView(screenName: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SCREEN_VIEW,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SCREEN_NAME, screenName),
            ),
        ),
    )
}

fun AnalyticsHelper.logNewsResourceOpened(newsResourceId: String) {
    logEvent(
        event = AnalyticsEvent(
            type = "news_resource_opened",
            extras = listOf(
                AnalyticsEvent.Param("opened_news_resource", newsResourceId),
            ),
        ),
    )
}


/**
 * A side-effect which records a screen view event.
 */
@Composable
fun TrackScreenViewEvent(
    screenName: String,
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current,
) = DisposableEffect(Unit) {
    analyticsHelper.logScreenView(screenName)
    onDispose {}
}
