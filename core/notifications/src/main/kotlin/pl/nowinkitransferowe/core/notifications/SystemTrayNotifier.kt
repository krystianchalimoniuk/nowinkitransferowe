/*
 * Copyright 2023 The Android Open Source Project
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

package pl.nowinkitransferowe.core.notifications

import android.Manifest.permission
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.core.app.NotificationCompat.InboxStyle
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.TransferResource
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_NUM_NOTIFICATIONS = 5
private const val TARGET_ACTIVITY_NAME = "pl.nowinkitransferowe.MainActivity"
private const val NEWS_NOTIFICATION_REQUEST_CODE = 0
private const val TRANSFER_NOTIFICATION_REQUEST_CODE = 2
private const val NEWS_NOTIFICATION_SUMMARY_ID = 1
private const val TRANSFER_NOTIFICATION_SUMMARY_ID = 3
private const val GENERAL_NOTIFICATION_SUMMARY_ID = 5

private const val GENERAL_NOTIFICATIONS_REQUEST_CODE = 4
private const val NOTIFICATION_CHANNEL_ID = "update_channel"
private const val NEWS_NOTIFICATION_GROUP = "NEWS_NOTIFICATIONS"
private const val TRANSFER_NOTIFICATION_GROUP = "TRANSFER_NOTIFICATIONS"
private const val GENERAL_NOTIFICATION_GROUP = "GENERAL_NOTIFICATIONS"
private const val DEEP_LINK_SCHEME_AND_HOST = "http://nowinkitransferowe.pl"
private const val NEWS_PATH = "news"
private const val TRANSFER_PATH = "transfer"

/**
 * Implementation of [Notifier] that displays notifications in the system tray.
 */
@Singleton
internal class SystemTrayNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : Notifier {

    override fun postNewsNotifications(
        newsResources: List<NewsResource>,
    ) = with(context) {
        if (checkSelfPermission(this, permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            return
        }

        val truncatedNewsResources = newsResources.take(MAX_NUM_NOTIFICATIONS)

        val newsNotifications = truncatedNewsResources.map { newsResource ->
            createNotification {
                setSmallIcon(
                    pl.nowinkitransferowe.core.common.R.drawable.core_common_ic_nt_notification,
                )
                    .setContentTitle(newsResource.title)
                    .setContentText(
                        HtmlCompat.fromHtml(
                            newsResource.description,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                            .toString()
                    )
                    .setContentIntent(newsPendingIntent(newsResource))
                    .setGroup(NEWS_NOTIFICATION_GROUP)
                    .setAutoCancel(true)
            }
        }
        val summaryNotification = createNotification {
            val title = getString(R.string.core_notifications_news_notification_group_summary)
            setContentTitle(title)
                .setContentText(title)
                .setSmallIcon(
                    pl.nowinkitransferowe.core.common.R.drawable.core_common_ic_nt_notification,
                )
                // Build summary info into InboxStyle template.
                .setStyle(newsNotificationStyle(truncatedNewsResources, title))
                .setGroup(NEWS_NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build()
        }

        // Send the notifications
        val notificationManager = NotificationManagerCompat.from(this)
        newsNotifications.forEachIndexed { index, notification ->
            notificationManager.notify(
                truncatedNewsResources[index].id.hashCode(),
                notification,
            )
        }
        notificationManager.notify(NEWS_NOTIFICATION_SUMMARY_ID, summaryNotification)
    }

    override fun postTransferNotifications(transferResource: List<TransferResource>) =
        with(context) {
            if (checkSelfPermission(this, permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
                return
            }

            val truncatedTransferResources = transferResource.take(MAX_NUM_NOTIFICATIONS)

            val newsNotifications = truncatedTransferResources.map { transferResource ->
                createNotification {
                    setSmallIcon(
                        pl.nowinkitransferowe.core.common.R.drawable.core_common_ic_nt_notification,
                    )
                        .setContentTitle("\uD83D\uDC64 ${transferResource.name}")
                        .setContentText("\uD83D\uDD01 ${transferResource.clubFrom} - ${transferResource.clubTo}\n\uD83D\uDCB6 ${transferResource.price}")
                        .setContentIntent(transferPendingIntent(transferResource))
                        .setGroup(TRANSFER_NOTIFICATION_GROUP)
                        .setAutoCancel(true)
                }
            }
            val summaryNotification = createNotification {
                val title = getString(
                    R.string.core_notifications_transfer_notification_group_summary,
                )
                setContentTitle(title)
                    .setContentText(title)
                    .setSmallIcon(
                        pl.nowinkitransferowe.core.common.R.drawable.core_common_ic_nt_notification,
                    )
                    // Build summary info into InboxStyle template.
                    .setStyle(transferNotificationStyle(truncatedTransferResources, title))
                    .setGroup(TRANSFER_NOTIFICATION_GROUP)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .build()
            }

            // Send the notifications
            val notificationManager = NotificationManagerCompat.from(this)
            newsNotifications.forEachIndexed { index, notification ->
                notificationManager.notify(
                    truncatedTransferResources[index].id.hashCode(),
                    notification,
                )
            }
            notificationManager.notify(TRANSFER_NOTIFICATION_SUMMARY_ID, summaryNotification)
        }

    override fun postGeneralNotification(
        generalNotificationResource: GeneralNotificationResource,
        bitmap: Bitmap?,
    ) =
        with(context) {
            if (checkSelfPermission(
                    this,
                    permission.POST_NOTIFICATIONS
                ) != PERMISSION_GRANTED
            ) {
                return
            }
            val summaryNotification = createNotification {
                val title = getString(
                    R.string.core_notifications_general_notification_group_summary,
                )
                setContentTitle(title)
                    .setContentText(title)
                    .setSmallIcon(
                        pl.nowinkitransferowe.core.common.R.drawable.core_common_ic_nt_notification,
                    )
                    // Build summary info into InboxStyle template.
                    .setStyle(
                        generalNotificationSummaryStyle(
                            generalNotificationResource.title,
                            title
                        )
                    )
                    .setGroup(GENERAL_NOTIFICATION_GROUP)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .build()
            }
            val style = generalNotificationStyle(bitmap)
            val notification = createNotification {
                setSmallIcon(
                    pl.nowinkitransferowe.core.common.R.drawable.core_common_ic_nt_notification,
                )
                    .setContentTitle(generalNotificationResource.title)
                    .setContentText(generalNotificationResource.description)
                    .setStyle(style)
                    .setContentIntent(
                        generalNotificationPendingIntent(
                            generalNotificationResource
                        )
                    )
                    .setGroup(GENERAL_NOTIFICATION_GROUP)
                    .setAutoCancel(true)
            }
            // Send the notifications
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(
                generalNotificationResource.id,
                notification,
            )
            notificationManager.notify(GENERAL_NOTIFICATION_SUMMARY_ID, summaryNotification)
        }


    /**
     * Creates an inbox style summary notification for news updates
     */
    private fun newsNotificationStyle(
        newsResources: List<NewsResource>,
        title: String,
    ): InboxStyle = newsResources
        .fold(InboxStyle()) { inboxStyle, newsResource -> inboxStyle.addLine(newsResource.title) }
        .setBigContentTitle(title)
        .setSummaryText(title)

    /**
     * Creates an inbox style summary notification for transfers updates
     */
    private fun transferNotificationStyle(
        transferResources: List<TransferResource>,
        title: String,
    ): InboxStyle = transferResources
        .fold(InboxStyle()) { inboxStyle, transferResource -> inboxStyle.addLine("\uD83D\uDC64 ${transferResource.name}") }
        .setBigContentTitle(title)
        .setSummaryText(title)

    /**
     * Creates an inbox style summary notification for transfers updates
     */
    private fun generalNotificationStyle(
        bitmap: Bitmap?,
    ): BigPictureStyle {
        return BigPictureStyle().bigPicture(bitmap)
    }

    /**
     * Creates an inbox style summary notification for transfers updates
     */
    private fun generalNotificationSummaryStyle(
        notificationTitle: String,
        title: String,
    ) = InboxStyle().addLine(notificationTitle).setBigContentTitle(title)
        .setSummaryText(title)

}


/**
 * Creates a notification for configured for news updates
 */
private fun Context.createNotification(
    block: NotificationCompat.Builder.() -> Unit,
): Notification {
    ensureNotificationChannelExists()
    return NotificationCompat.Builder(
        this,
        NOTIFICATION_CHANNEL_ID,
    )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .apply(block)
        .build()
}

/**
 * Ensures that a notification channel is present if applicable
 */
private fun Context.ensureNotificationChannelExists() {
    if (VERSION.SDK_INT < VERSION_CODES.O) return

    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        getString(R.string.core_notifications_news_notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = getString(R.string.core_notifications_news_notification_channel_description)
    }
    // Register the channel with the system
    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}

private fun Context.newsPendingIntent(
    newsResource: NewsResource,
): PendingIntent? = PendingIntent.getActivity(
    this,
    NEWS_NOTIFICATION_REQUEST_CODE,
    Intent().apply {
        action = Intent.ACTION_VIEW
        data = newsResource.newsDeepLinkUri()
        component = ComponentName(
            packageName,
            TARGET_ACTIVITY_NAME,
        )
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

private fun Context.transferPendingIntent(
    transferResource: TransferResource,
): PendingIntent? = PendingIntent.getActivity(
    this,
    TRANSFER_NOTIFICATION_REQUEST_CODE,
    Intent().apply {
        action = Intent.ACTION_VIEW
        data = transferResource.transferDeepLinkUri()
        component = ComponentName(
            packageName,
            TARGET_ACTIVITY_NAME,
        )
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

private fun Context.generalNotificationPendingIntent(generalNotificationResource: GeneralNotificationResource): PendingIntent? =
    PendingIntent.getActivity(
        this, GENERAL_NOTIFICATIONS_REQUEST_CODE,
        Intent().apply {
            action = Intent.ACTION_VIEW
            data = generalNotificationResource.url.toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

private fun NewsResource.newsDeepLinkUri() = "$DEEP_LINK_SCHEME_AND_HOST/$NEWS_PATH/$id".toUri()
private fun TransferResource.transferDeepLinkUri() =
    "$DEEP_LINK_SCHEME_AND_HOST/$TRANSFER_PATH/$id".toUri()
