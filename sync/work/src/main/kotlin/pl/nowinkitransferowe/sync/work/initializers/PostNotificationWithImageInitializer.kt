package pl.nowinkitransferowe.sync.work.initializers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import pl.nowinkitransferowe.sync.work.workers.PostNotificationWithImageWorker

object PostNotificationWithImageInitializer {
    fun initialize(
        context: Context,
        id: Int,
        title: String,
        description: String,
        url: String,
        imageUrl: String,
    ) {
        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(
                DOWNLOAD_IMAGE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                PostNotificationWithImageWorker.startUpPostNotificationWithImageWorker(id, title, description, url, imageUrl),
            )
        }
    }
}

// This name should not be changed otherwise the app may have concurrent sync requests running
internal const val DOWNLOAD_IMAGE_WORK_NAME = "PostNotificationWithImageWorkName"
