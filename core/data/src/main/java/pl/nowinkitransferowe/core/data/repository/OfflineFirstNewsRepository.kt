package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.changeListSync
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.util.DatabaseUpdatingMonitor
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.notifications.Notifier
import javax.inject.Inject

private const val SYNC_BATCH_SIZE = 1000

internal class OfflineFirstNewsRepository @Inject constructor(
    private val ntPreferencesDataSource: NtPreferencesDataSource,
    private val newsResourceDao: NewsResourceDao,
    private val network: NtNetworkDataSource,
    private val notifier: Notifier,
    private val databaseUpdatingMonitor: DatabaseUpdatingMonitor,
) : NewsRepository {

    override fun getNewsResources(
        query: NewsResourceQuery,
    ): Flow<List<NewsResource>> = newsResourceDao.getNewsResources(
        useFilterNewsIds = query.filterNewsIds != null,
        filterNewsIds = query.filterNewsIds ?: emptySet(),
    )
        .map { it.map(NewsResourceEntity::asExternalModel) }

    override fun getNewsResourcesPages(limit: Int, offset: Int): Flow<List<NewsResource>> =
        newsResourceDao.getNewsResourcesPages(limit, offset)
            .map { it.map(NewsResourceEntity::asExternalModel) }


    override fun getNewsResource(id: String): Flow<NewsResource> =
        newsResourceDao.getNewsResource(id).map { it.asExternalModel() }

    override fun getNewsCount(): Flow<Int> = newsResourceDao.getCount()


    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        var isFirstSync = false
        return synchronizer.changeListSync(
            versionReader = ChangeListVersions::newsResourceVersion,
            changeListFetcher = { currentVersion ->
                isFirstSync = currentVersion <= 0
                network.getNewsResourceChangeList(after = currentVersion)
            },
            versionUpdater = { latestVersion ->
                copy(newsResourceVersion = latestVersion)
            },
            modelDeleter = { toDelete ->
                newsResourceDao.deleteNewsResources(toDelete)
                if (toDelete.isNotEmpty()) databaseUpdatingMonitor.notifyNewsDataChanged()
            },
            modelUpdater = { changedIds ->
                val isNewsNotificationAllowed =
                    ntPreferencesDataSource.userData.first().isNewsNotificationsAllowed

                val existingNewsResourceIdsThatHaveChanged = when {
                    isFirstSync -> emptySet()
                    else -> newsResourceDao.getNewsResourceIds(
                        useFilterNewsIds = true,
                        filterNewsIds = changedIds.toSet(),
                    )
                        .first()
                        .toSet()
                }



                if (isFirstSync) {
                    // When we first retrieve news, mark everything viewed, so that we aren't
                    // overwhelmed with all historical news.
                    ntPreferencesDataSource.setNewsResourcesViewed(changedIds, true)
                }

                if (changedIds.isNotEmpty()) databaseUpdatingMonitor.notifyNewsDataChanged()
                // Obtain the news resources which have changed from the network and upsert them locally
                changedIds.chunked(SYNC_BATCH_SIZE).forEach { chunkedIds ->
                    val networkNewsResources =
                        network.getNewsResources(ids = chunkedIds.joinToString(","))

                    // Order of invocation matters to satisfy id and foreign key constraints!

                    newsResourceDao.upsertNewsResources(
                        newsResourceEntities = networkNewsResources.map(
                            NetworkNewsResource::asEntity,
                        ),
                    )
                }

                if (!isFirstSync) {
                    val addedNewsResources = newsResourceDao.getNewsResources(
                        useFilterNewsIds = true,
                        filterNewsIds = changedIds.toSet() - existingNewsResourceIdsThatHaveChanged,
                    )
                        .first()
                        .map(NewsResourceEntity::asExternalModel)

                    if (addedNewsResources.isNotEmpty() && isNewsNotificationAllowed) {
                        notifier.postNewsNotifications(
                            newsResources = addedNewsResources,
                        )
                    }
                }
            },
        )
    }
}
