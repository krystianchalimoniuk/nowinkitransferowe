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
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.InboxStyle
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.model.NewsChannel
import pl.nowinkitransferowe.core.model.NewsGroup
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.model.NewsSubject
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.crestUrl
import javax.inject.Inject
import javax.inject.Singleton
import pl.nowinkitransferowe.core.common.R as CommonR

private const val TARGET_ACTIVITY_NAME = "pl.nowinkitransferowe.MainActivity"
private const val ACTION_RECEIVER_NAME = "pl.nowinkitransferowe.sync.work.notifications.NewsActionReceiver"
private const val GENERAL_NOTIFICATION_SUMMARY_ID = 5
private const val NEWS_NOTIFICATION_SUMMARY_ID = 7
private const val HELD_NEWS_NOTIFICATION_ID = 8
private const val MAX_LINES = 6
private const val BRAND_COLOR = 0xFFB3121B.toInt()
private const val STAGE_DONE_COLOR = 0xFFB3121B.toInt()
private const val STAGE_TODO_COLOR = 0xFFF0C9C5.toInt()

/** The channel of the editors' push notifications; the id of the 3.x channel, so its setting stays. */
private const val EDITORS_CHANNEL_ID = "update_channel"
private const val FOLLOWED_CHANNEL_ID = "followed_channel"
private const val LEAGUES_CHANNEL_ID = "leagues_channel"
private const val GENERAL_NOTIFICATION_GROUP = "GENERAL_NOTIFICATIONS"
private const val NEWS_NOTIFICATION_GROUP = "NEWS_NOTIFICATIONS"

private const val DEEP_LINK_SCHEME_AND_HOST = "https://nowinkitransferowe.pl"
const val DEEP_LINK_NEWS_RESOURCE_ID_KEY = "newsId"
const val DEEP_LINK_TRANSFER_RESOURCE_ID_KEY = "transferId"
const val DEEP_LINK_RUMOUR_ID_KEY = "rumourId"
const val DEEP_LINK_CLUB_ID_KEY = "clubId"
const val DEEP_LINK_PLAYER_ID_KEY = "playerId"
const val DEEP_LINK_NEWS_URI_PATTERN = "$DEEP_LINK_SCHEME_AND_HOST/news/{$DEEP_LINK_NEWS_RESOURCE_ID_KEY}"
const val DEEP_LINK_TRANSFER_URI_PATTERN = "$DEEP_LINK_SCHEME_AND_HOST/transfer/{$DEEP_LINK_TRANSFER_RESOURCE_ID_KEY}"
const val DEEP_LINK_RUMOUR_URI_PATTERN = "$DEEP_LINK_SCHEME_AND_HOST/rumour/{$DEEP_LINK_RUMOUR_ID_KEY}"
const val DEEP_LINK_CLUB_URI_PATTERN = "$DEEP_LINK_SCHEME_AND_HOST/club/{$DEEP_LINK_CLUB_ID_KEY}"
const val DEEP_LINK_PLAYER_URI_PATTERN = "$DEEP_LINK_SCHEME_AND_HOST/player/{$DEEP_LINK_PLAYER_ID_KEY}"
const val DEEP_LINK_LEAGUES_URI = "$DEEP_LINK_SCHEME_AND_HOST/leagues"

/** The in-app link to the card of the rumour [id]. */
fun rumourAppLink(id: Int) = "$DEEP_LINK_SCHEME_AND_HOST/rumour/$id"

/** The in-app link to the card of the transfer [id]. */
fun transferAppLink(id: Int) = "$DEEP_LINK_SCHEME_AND_HOST/transfer/$id"

/** The in-app link to the article [id]. */
fun articleAppLink(id: Int) = "$DEEP_LINK_SCHEME_AND_HOST/news/$id"

/** The notification actions handled by the receiver: save the record, or mute its subject today. */
const val ACTION_SAVE_NEWS = "pl.nowinkitransferowe.action.SAVE_NEWS"
const val ACTION_MUTE_NEWS_SUBJECT = "pl.nowinkitransferowe.action.MUTE_NEWS_SUBJECT"
const val EXTRA_SUBJECT_KEY = "pl.nowinkitransferowe.extra.SUBJECT_KEY"

/** The extras of the "Zapisz" action, read by the receiver that saves the record. */
const val EXTRA_SAVE_KIND = "pl.nowinkitransferowe.extra.SAVE_KIND"
const val EXTRA_SAVE_ID = "pl.nowinkitransferowe.extra.SAVE_ID"
const val EXTRA_NOTIFICATION_ID = "pl.nowinkitransferowe.extra.NOTIFICATION_ID"

/**
 * Implementation of [Notifier] that displays notifications in the system tray.
 *
 * Each kind has its own channel, so it can also be turned off in the system settings: the
 * editors' notifications, followed clubs and players, and followed leagues.
 */
@Singleton
internal class SystemTrayNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : Notifier {

    override fun postNews(groups: List<NewsGroup>, images: Map<String, Bitmap>) = with(context) {
        if (!canPost()) return
        ensureNotificationChannelsExist()
        val manager = NotificationManagerCompat.from(this)
        val posted = groups.map { group ->
            val id: Int
            val notification = if (group.items.size == 1) {
                val item = group.items.single()
                id = item.key.hashCode()
                singleNews(item, group, images, id)
            } else {
                id = group.subject.key.hashCode()
                groupedNews(group, images)
            }
            manager.notify(id, notification)
            group
        }
        if (posted.size > 1) {
            val title = getString(R.string.core_notifications_news_summary)
            manager.notify(
                NEWS_NOTIFICATION_SUMMARY_ID,
                NotificationCompat.Builder(this, FOLLOWED_CHANNEL_ID)
                    .setSmallIcon(CommonR.drawable.core_common_ic_nt_notification)
                    .setColor(BRAND_COLOR)
                    .setContentTitle(title)
                    .setStyle(posted.fold(InboxStyle()) { style, group -> style.addLine(group.subject.name) }.setSummaryText(title))
                    .setGroup(NEWS_NOTIFICATION_GROUP)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .build(),
            )
        }
    }

    override fun postHeldNews(groups: List<NewsGroup>) = with(context) {
        if (!canPost() || groups.isEmpty()) return
        ensureNotificationChannelsExist()
        val count = groups.sumOf { it.items.size }
        val title = resources.getQuantityString(R.plurals.core_notifications_held_news_title, count, count)
        val lines = groups.flatMap { group -> group.items.map { item -> "${group.subject.name} · ${itemLine(item)}" } }
        val notification = NotificationCompat.Builder(this, FOLLOWED_CHANNEL_ID)
            .setSmallIcon(CommonR.drawable.core_common_ic_nt_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle(title)
            .setContentText(groups.joinToString { it.subject.name })
            .setStyle(lines.take(MAX_LINES).fold(InboxStyle()) { style, line -> style.addLine(line) }.setBigContentTitle(title))
            .setContentIntent(openIntent(null, HELD_NEWS_NOTIFICATION_ID))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(HELD_NEWS_NOTIFICATION_ID, notification)
    }

    override fun postGeneralNotification(
        generalNotificationResource: GeneralNotificationResource,
        bitmap: Bitmap?,
    ) = with(context) {
        if (!canPost()) return
        ensureNotificationChannelsExist()
        val summaryNotification = NotificationCompat.Builder(this, EDITORS_CHANNEL_ID)
            .setSmallIcon(CommonR.drawable.core_common_ic_nt_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle(getString(R.string.core_notifications_general_notification_group_summary))
            .setStyle(
                InboxStyle().addLine(generalNotificationResource.title)
                    .setSummaryText(getString(R.string.core_notifications_general_notification_group_summary)),
            )
            .setGroup(GENERAL_NOTIFICATION_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notification = NotificationCompat.Builder(this, EDITORS_CHANNEL_ID)
            .setSmallIcon(CommonR.drawable.core_common_ic_nt_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle(generalNotificationResource.title)
            .setContentText(generalNotificationResource.description)
            .setStyle(
                if (bitmap != null) {
                    BigPictureStyle().bigPicture(bitmap)
                } else {
                    BigTextStyle().bigText(generalNotificationResource.description)
                },
            )
            .setContentIntent(generalNotificationPendingIntent(generalNotificationResource))
            .setGroup(GENERAL_NOTIFICATION_GROUP)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(generalNotificationResource.id, notification)
        notificationManager.notify(GENERAL_NOTIFICATION_SUMMARY_ID, summaryNotification)
    }

    private fun Context.canPost() = checkSelfPermission(this, permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED

    /** One item as a rich notification: a transfer with its picture, a rumour with its stages. */
    private fun Context.singleNews(item: NewsItem, group: NewsGroup, images: Map<String, Bitmap>, id: Int): Notification {
        if (item is NewsItem.RumourNews && VERSION.SDK_INT >= 36) {
            return rumourWithStages(item, group, images, id)
        }
        val builder = newsBuilder(group.channel)
            .setSubText(group.subject.name)
            .setContentTitle(itemTitle(item))
            .setContentText(itemText(item))
            .setContentIntent(openIntent(item.deepLink(), id))
            .addAction(0, getString(R.string.core_notifications_action_save), saveIntent(item, id))
        when (item) {
            is NewsItem.TransferNews -> {
                val transfer = item.transfer
                transfer.player.photoUrl?.let(images::get)?.let { builder.setLargeIcon(circleCrop(it)) }
                val picture = transferGraphic(transfer, images, transfer.fee.label)
                builder.setStyle(BigPictureStyle().bigPicture(picture).bigLargeIcon(null as Bitmap?))
                (transfer.to as? ClubRef.Club)?.let { club ->
                    builder.addAction(0, club.name, openIntent(clubDeepLink(club.id), id + 1))
                }
                builder.addAction(0, getString(R.string.core_notifications_action_share), shareIntent("${itemTitle(item)}\n${itemText(item)}\n$DEEP_LINK_SCHEME_AND_HOST", id + 2))
            }

            is NewsItem.RumourNews -> {
                val rumour = item.rumour
                rumour.player.photoUrl?.let(images::get)?.let { builder.setLargeIcon(circleCrop(it)) }
                // Before Android 16 the stage is in the text: a plain progress bar would hide it
                builder.setStyle(
                    BigTextStyle().bigText(
                        getString(R.string.core_notifications_rumour_stage_of, itemText(item), rumour.stage.level, RumourStage.entries.size),
                    ),
                )
            }

            is NewsItem.ArticleNews -> {
                val article = item.article
                val image = (article.thumbnailUrl ?: article.imageUrl)?.let(images::get)
                builder.setStyle(
                    if (image != null) {
                        BigPictureStyle().bigPicture(image).setSummaryText(itemText(item))
                    } else {
                        BigTextStyle().bigText(itemText(item))
                    },
                )
            }
        }
        return builder.build()
    }

    /**
     * A rumour on Android 16 and later: the stages as a segmented bar with the player's photo
     * at the stage it reached.
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(36)
    private fun Context.rumourWithStages(item: NewsItem.RumourNews, group: NewsGroup, images: Map<String, Bitmap>, id: Int): Notification {
        val rumour = item.rumour
        val photo = rumour.player.photoUrl?.let(images::get)?.let(::circleCrop)
        val stages = RumourStage.entries
        val style = Notification.ProgressStyle()
            .setStyledByProgress(false)
            .setProgressSegments(
                stages.map { stage ->
                    Notification.ProgressStyle.Segment(1).setColor(if (stage.level <= rumour.stage.level) STAGE_DONE_COLOR else STAGE_TODO_COLOR)
                },
            )
            .setProgressPoints(
                (1 until stages.size).map { level ->
                    Notification.ProgressStyle.Point(level).setColor(if (level <= rumour.stage.level) STAGE_DONE_COLOR else STAGE_TODO_COLOR)
                },
            )
            .setProgress(rumour.stage.level)
        photo?.let { style.setProgressTrackerIcon(Icon.createWithBitmap(it)) }
        // The path of the rumour: the club the player leaves at the start, the new one at the end
        rumour.from?.crestUrl?.let(images::get)?.let { style.setProgressStartIcon(Icon.createWithBitmap(crestIcon(it))) }
        rumour.to.crestUrl?.let(images::get)?.let { style.setProgressEndIcon(Icon.createWithBitmap(crestIcon(it))) }
        return Notification.Builder(this, group.channel.channelId())
            .setSmallIcon(CommonR.drawable.core_common_ic_nt_notification)
            .setColor(BRAND_COLOR)
            .setSubText(group.subject.name)
            .setContentTitle(itemTitle(item))
            .setContentText(itemText(item))
            .apply { photo?.let { setLargeIcon(it) } }
            .setStyle(style)
            .setContentIntent(openIntent(item.deepLink(), id))
            .addAction(
                Notification.Action.Builder(null as Icon?, getString(R.string.core_notifications_action_save), saveIntent(item, id)).build(),
            )
            .setGroup(NEWS_NOTIFICATION_GROUP)
            .setAutoCancel(true)
            .build()
    }

    /** Several items about one subject: one notification that opens the club, player or leagues. */
    private fun Context.groupedNews(group: NewsGroup, images: Map<String, Bitmap>): Notification {
        val count = group.items.size
        val text = resources.getQuantityString(R.plurals.core_notifications_news_count, count, count)
        val style = group.items.take(MAX_LINES).fold(InboxStyle()) { inbox, item -> inbox.addLine(styledLine(item)) }
            .setBigContentTitle(group.subject.name)
            .setSummaryText(text)
        val builder = newsBuilder(group.channel)
            .setContentTitle(group.subject.name)
            .setContentText(group.items.joinToString(" · ") { itemTitle(it) })
            .setStyle(style)
            .setNumber(count)
            .setContentIntent(openIntent(group.subject.deepLink(), group.subject.key.hashCode()))
            .addAction(0, getString(group.subject.openLabel()), openIntent(group.subject.deepLink(), group.subject.key.hashCode() + 1))
            .addAction(0, getString(R.string.core_notifications_action_mute_today), muteIntent(group.subject, group.subject.key.hashCode()))
        when (val subject = group.subject) {
            is NewsSubject.Club -> subject.crestUrl?.let(images::get)?.let { builder.setLargeIcon(crestIcon(it)) }
            is NewsSubject.Player -> subject.photoUrl?.let(images::get)?.let { builder.setLargeIcon(circleCrop(it)) }
            is NewsSubject.League -> Unit
        }
        return builder.build()
    }

    private fun Context.newsBuilder(channel: NewsChannel) = NotificationCompat.Builder(this, channel.channelId())
        .setSmallIcon(CommonR.drawable.core_common_ic_nt_notification)
        .setColor(BRAND_COLOR)
        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
        .setGroup(NEWS_NOTIFICATION_GROUP)
        .setAutoCancel(true)

    private fun Context.itemTitle(item: NewsItem): String = when (item) {
        is NewsItem.TransferNews -> getString(R.string.core_notifications_transfer_title, item.transfer.player.name)
        is NewsItem.RumourNews -> getString(R.string.core_notifications_rumour_title, item.rumour.player.name, item.rumour.to.name)
        is NewsItem.ArticleNews -> item.article.title
    }

    private fun Context.itemText(item: NewsItem): String = when (item) {
        is NewsItem.TransferNews -> getString(
            R.string.core_notifications_transfer_text,
            item.transfer.from.name,
            item.transfer.to.name,
            item.transfer.fee.label,
        )

        is NewsItem.RumourNews -> {
            val rumour = item.rumour
            val previous = rumour.previousStage?.takeIf { it.level < rumour.stage.level }
            if (previous != null) {
                getString(R.string.core_notifications_rumour_stage_up, stageName(previous), stageName(rumour.stage))
            } else {
                getString(R.string.core_notifications_rumour_new, stageName(rumour.stage))
            }
        }

        is NewsItem.ArticleNews -> HtmlCompat.fromHtml(item.article.lead, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    }

    /** A line of a grouped notification: the kind, then what happened. */
    private fun Context.itemLine(item: NewsItem): String = "${kindName(item)} · ${lineText(item)}"

    /** [itemLine] with the kind in bold. */
    private fun Context.styledLine(item: NewsItem): CharSequence = SpannableStringBuilder()
        .append(kindName(item), StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        .append("  ")
        .append(lineText(item))

    private fun Context.kindName(item: NewsItem): String = getString(
        when (item) {
            is NewsItem.TransferNews -> R.string.core_notifications_kind_transfer
            is NewsItem.RumourNews -> R.string.core_notifications_kind_rumour
            is NewsItem.ArticleNews -> R.string.core_notifications_kind_article
        },
    )

    private fun Context.lineText(item: NewsItem): String = when (item) {
        is NewsItem.TransferNews -> getString(R.string.core_notifications_line_transfer, item.transfer.player.name, item.transfer.fee.label)
        is NewsItem.RumourNews -> getString(R.string.core_notifications_line_rumour, item.rumour.player.name, stageName(item.rumour.stage))
        is NewsItem.ArticleNews -> item.article.title
    }

    private fun Context.stageName(stage: RumourStage): String = getString(
        when (stage) {
            RumourStage.SPECULATION -> R.string.core_notifications_stage_speculation
            RumourStage.INTEREST -> R.string.core_notifications_stage_interest
            RumourStage.TALKS -> R.string.core_notifications_stage_talks
            RumourStage.CLOSE -> R.string.core_notifications_stage_close
            RumourStage.AGREEMENT -> R.string.core_notifications_stage_agreement
        },
    )
}

private fun NewsChannel.channelId() = when (this) {
    NewsChannel.FOLLOWED -> FOLLOWED_CHANNEL_ID
    NewsChannel.LEAGUES -> LEAGUES_CHANNEL_ID
}

private fun NewsSubject.openLabel() = when (this) {
    is NewsSubject.Club -> R.string.core_notifications_action_open_club
    is NewsSubject.Player -> R.string.core_notifications_action_open_player
    is NewsSubject.League -> R.string.core_notifications_action_open_leagues
}

private fun NewsSubject.deepLink(): Uri = when (this) {
    is NewsSubject.Club -> clubDeepLink(id)
    is NewsSubject.Player -> "$DEEP_LINK_SCHEME_AND_HOST/player/$id".toUri()
    is NewsSubject.League -> DEEP_LINK_LEAGUES_URI.toUri()
}

private fun clubDeepLink(id: Int) = "$DEEP_LINK_SCHEME_AND_HOST/club/$id".toUri()

private fun NewsItem.deepLink(): Uri = when (this) {
    is NewsItem.TransferNews -> "$DEEP_LINK_SCHEME_AND_HOST/transfer/${transfer.id}".toUri()
    is NewsItem.RumourNews -> "$DEEP_LINK_SCHEME_AND_HOST/rumour/${rumour.id}".toUri()
    is NewsItem.ArticleNews -> "$DEEP_LINK_SCHEME_AND_HOST/news/${article.id}".toUri()
}

/** Opens [uri] in the app, or just the app when it is null. */
private fun Context.openIntent(uri: Uri?, requestCode: Int): PendingIntent = PendingIntent.getActivity(
    this,
    requestCode,
    Intent().apply {
        action = if (uri != null) Intent.ACTION_VIEW else Intent.ACTION_MAIN
        data = uri
        component = ComponentName(packageName, TARGET_ACTIVITY_NAME)
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

/** Shares [text] with the system share sheet. */
private fun Context.shareIntent(text: String, requestCode: Int): PendingIntent = PendingIntent.getActivity(
    this,
    requestCode,
    Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

/** Mutes the news about [subject] for the rest of the day and closes its notification. */
private fun Context.muteIntent(subject: NewsSubject, notificationId: Int): PendingIntent = PendingIntent.getBroadcast(
    this,
    notificationId,
    Intent(ACTION_MUTE_NEWS_SUBJECT).apply {
        component = ComponentName(packageName, ACTION_RECEIVER_NAME)
        putExtra(EXTRA_SUBJECT_KEY, subject.key)
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

/** Saves the item to "Zapisane" and closes the notification [notificationId]. */
private fun Context.saveIntent(item: NewsItem, notificationId: Int): PendingIntent {
    val (kind, id) = when (item) {
        is NewsItem.TransferNews -> NewsItem.TRANSFER_KEY to item.transfer.id
        is NewsItem.RumourNews -> NewsItem.RUMOUR_KEY to item.rumour.id
        is NewsItem.ArticleNews -> NewsItem.ARTICLE_KEY to item.article.id
    }
    return PendingIntent.getBroadcast(
        this,
        notificationId,
        Intent(ACTION_SAVE_NEWS).apply {
            component = ComponentName(packageName, ACTION_RECEIVER_NAME)
            putExtra(EXTRA_SAVE_KIND, kind)
            putExtra(EXTRA_SAVE_ID, id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

/**
 * Creates the channels: the editors' one, followed clubs and players, and followed leagues.
 */
private fun Context.ensureNotificationChannelsExist() {
    if (VERSION.SDK_INT < VERSION_CODES.O) return
    val manager = NotificationManagerCompat.from(this)
    listOf(
        Triple(EDITORS_CHANNEL_ID, R.string.core_notifications_channel_editors, R.string.core_notifications_channel_editors_description),
        Triple(FOLLOWED_CHANNEL_ID, R.string.core_notifications_channel_followed, R.string.core_notifications_channel_followed_description),
        Triple(LEAGUES_CHANNEL_ID, R.string.core_notifications_channel_leagues, R.string.core_notifications_channel_leagues_description),
    ).forEach { (id, name, description) ->
        manager.createNotificationChannel(
            NotificationChannel(id, getString(name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                this.description = getString(description)
            },
        )
    }
}

/**
 * Opens the rumour card or article in the app when the notification has an app link, otherwise its
 * url, in the browser for addresses the app does not handle.
 */
private fun Context.generalNotificationPendingIntent(generalNotificationResource: GeneralNotificationResource): PendingIntent? = generalNotificationResource.appLink
    ?.let { openIntent(it.toUri(), generalNotificationResource.id) }
    ?: PendingIntent.getActivity(
        this,
        generalNotificationResource.id,
        Intent().apply {
            action = Intent.ACTION_VIEW
            data = generalNotificationResource.url.toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
