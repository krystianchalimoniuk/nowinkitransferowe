package pl.nowinkitransferowe.core.testing.notifications

import android.graphics.Bitmap
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.notifications.Notifier

/**
 * Aggregates news resources that have been notified for addition
 */
class TestNotifier : Notifier {

    private val mutableAddedNewsResources = mutableListOf<List<NewsResource>>()
    private val mutableAddedTransferResources = mutableListOf<List<TransferResource>>()
    private val mutableAddedGeneralNotificationResource =
        mutableListOf<GeneralNotificationResource>()
    val addedNewsResources: List<List<NewsResource>> = mutableAddedNewsResources
    val addedTransferResources: List<List<TransferResource>> = mutableAddedTransferResources
    val addedGeneralNotificationResources: List<GeneralNotificationResource> =
        mutableAddedGeneralNotificationResource

    override fun postNewsNotifications(newsResources: List<NewsResource>) {
        mutableAddedNewsResources.add(newsResources)
    }

    override fun postTransferNotifications(transferResource: List<TransferResource>) {
        mutableAddedTransferResources.add(transferResource)
    }

    override fun postGeneralNotification(
        generalNotificationResource: GeneralNotificationResource,
        bitmap: Bitmap?,
    ) {
        mutableAddedGeneralNotificationResource.add(generalNotificationResource)
    }
}
