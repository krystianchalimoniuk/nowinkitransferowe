/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.nowinkitransferowe.core.data.repository

import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper

internal fun AnalyticsHelper.logNewsResourceBookmarkToggled(newsResourceId: String, isBookmarked: Boolean) {
    val eventType = if (isBookmarked) "news_resource_saved" else "news_resource_unsaved"
    val paramKey = if (isBookmarked) "saved_news_resource_id" else "unsaved_news_resource_id"
    logEvent(
        AnalyticsEvent(
            type = eventType,
            extras = listOf(
                AnalyticsEvent.Param(key = paramKey, value = newsResourceId),
            ),
        ),
    )
}

internal fun AnalyticsHelper.logDarkThemeConfigChanged(darkThemeConfigName: String) =
    logEvent(
        AnalyticsEvent(
            type = "dark_theme_config_changed",
            extras = listOf(
                AnalyticsEvent.Param(key = "dark_theme_config", value = darkThemeConfigName),
            ),
        ),
    )

internal fun AnalyticsHelper.logDynamicColorPreferenceChanged(useDynamicColor: Boolean) =
    logEvent(
        AnalyticsEvent(
            type = "dynamic_color_preference_changed",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = "dynamic_color_preference",
                    value = useDynamicColor.toString(),
                ),
            ),
        ),
    )

internal fun AnalyticsHelper.logNewsNotificationsPreferenceChanged(isAllowed: Boolean) =
    logEvent(
        AnalyticsEvent(
            type = "news_notifications_preference_changed",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = "news_notifications_preference",
                    value = isAllowed.toString(),
                ),
            ),
        ),
    )

internal fun AnalyticsHelper.logTransfersNotificationsPreferenceChanged(isAllowed: Boolean) =
    logEvent(
        AnalyticsEvent(
            type = "transfers_notifications_preference_changed",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = "transfers_notifications_preference",
                    value = isAllowed.toString(),
                ),
            ),
        ),
    )

internal fun AnalyticsHelper.logGeneralNotificationsPreferenceChanged(isAllowed: Boolean) =
    logEvent(
        AnalyticsEvent(
            type = "general_notifications_preference_changed",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = "general_notifications_preference",
                    value = isAllowed.toString(),
                ),
            ),
        ),
    )
