package pl.nowinkitransferowe.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.nowinkitransferowe.core.database.NtDatabase
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.NewsCategory

class NewsResourceDaoTest {

    private lateinit var newsResourceDao: NewsResourceDao
    private lateinit var db: NtDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            NtDatabase::class.java,
        ).build()
        newsResourceDao = db.newsResourceDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun newsResourceDao_fetches_items_by_descending_publish_date() = runTest {
        val newsResourceEntities = listOf(
            testTransfersResource(
                id = "0",
                millisSinceEpoch = 0,
            ),
            testTransfersResource(
                id = "1",
                millisSinceEpoch = 3,
            ),
            testTransfersResource(
                id = "2",
                millisSinceEpoch = 1,
            ),
            testTransfersResource(
                id = "3",
                millisSinceEpoch = 2,
            ),
        )
        newsResourceDao.upsertNewsResources(
            newsResourceEntities,
        )

        val savedNewsResourceEntities = newsResourceDao.getNewsResources()
            .first()

        kotlin.test.assertEquals(
            listOf(3L, 2L, 1L, 0L),
            savedNewsResourceEntities.map {
                it.asExternalModel().publishDate.toEpochMilliseconds()
            },
        )
    }

    @Test
    fun newsResourceDao_filters_items_by_news_ids_by_descending_publish_date() = runTest {
        val newsResourceEntities = listOf(
            testTransfersResource(
                id = "0",
                millisSinceEpoch = 0,
            ),
            testTransfersResource(
                id = "1",
                millisSinceEpoch = 3,
            ),
            testTransfersResource(
                id = "2",
                millisSinceEpoch = 1,
            ),
            testTransfersResource(
                id = "3",
                millisSinceEpoch = 2,
            ),
        )
        newsResourceDao.upsertNewsResources(
            newsResourceEntities,
        )

        val savedNewsResourceEntities = newsResourceDao.getNewsResources(
            useFilterNewsIds = true,
            filterNewsIds = setOf("3", "0"),
        )
            .first()

        kotlin.test.assertEquals(
            listOf("3", "0"),
            savedNewsResourceEntities.map {
                it.id
            },
        )
    }



    @Test
    fun newsResourceDao_deletes_items_by_ids() =
        runTest {
            val newsResourceEntities = listOf(
                testTransfersResource(
                    id = "0",
                    millisSinceEpoch = 0,
                ),
                testTransfersResource(
                    id = "1",
                    millisSinceEpoch = 3,
                ),
                testTransfersResource(
                    id = "2",
                    millisSinceEpoch = 1,
                ),
                testTransfersResource(
                    id = "3",
                    millisSinceEpoch = 2,
                ),
            )
            newsResourceDao.upsertNewsResources(newsResourceEntities)

            val (toDelete, toKeep) = newsResourceEntities.partition { it.id.toInt() % 2 == 0 }

            newsResourceDao.deleteNewsResources(
                toDelete.map(NewsResourceEntity::id),
            )

            kotlin.test.assertEquals(
                toKeep.map(NewsResourceEntity::id)
                    .toSet(),
                newsResourceDao.getNewsResources().first()
                    .map { it.id }
                    .toSet(),
            )
        }
}
private fun testTransfersResource(
    id: String = "0",
    millisSinceEpoch: Long = 0,
) = NewsResourceEntity(
    id = id,
    title = "",
    description = "",
    link = "",
    imageUrl = "",
    publishDate = Instant.fromEpochMilliseconds(millisSinceEpoch),
    category = NewsCategory.TRANSFERS,
    topics = "topic, topic",
    authPic = "",
    authTwitter = "",
    isImportant = true,
    time = "",
    src = "",
    author = "",
    photoSrc = ""
)