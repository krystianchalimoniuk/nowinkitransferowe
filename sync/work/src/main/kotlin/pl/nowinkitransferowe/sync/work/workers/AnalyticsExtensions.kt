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
        AnalyticsEvent(type = "displaying_general_notification_is_not_allowed"),
    )

internal fun AnalyticsHelper.logEmptyNotification() =
    logEvent(
        AnalyticsEvent(type = "notification_title_and_description_is_empty"),
    )

internal fun AnalyticsHelper.logPostNotificationWithImageFinished() {
    logEvent(
        AnalyticsEvent(type = "post_notification_with_image_finished"),
    )
}
