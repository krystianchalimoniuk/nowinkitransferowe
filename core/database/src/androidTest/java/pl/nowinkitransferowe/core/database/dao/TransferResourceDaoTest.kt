package pl.nowinkitransferowe.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.nowinkitransferowe.core.database.NtDatabase
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel

class TransferResourceDaoTest {

    private lateinit var transferResourceDao: TransferResourceDao
    private lateinit var db: NtDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            NtDatabase::class.java,
        ).build()
        transferResourceDao = db.transferResourceDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun transferResourceDao_fetches_items_by_descending_id() = runTest {
        val newsResourceEntities = listOf(
            testTransferResource(
                id = "0",
                millisSinceEpoch = 0,
            ),
            testTransferResource(
                id = "1",
                millisSinceEpoch = 3,
            ),
            testTransferResource(
                id = "2",
                millisSinceEpoch = 1,
            ),
            testTransferResource(
                id = "3",
                millisSinceEpoch = 2,
            ),
        )
        transferResourceDao.upsertTransferResources(
            newsResourceEntities,
        )

        val savedTransfersResourceEntities = transferResourceDao.getTransferResources()
            .first()

        kotlin.test.assertEquals(
            listOf(3, 2, 1, 0),
            savedTransfersResourceEntities.map {
                it.asExternalModel().id.toInt()
            },
        )
    }

    @Test
    fun transferResourceDao_filters_items_by_news_ids_by_descending_id() = runTest {
        val transferResourceEntities = listOf(
            testTransferResource(
                id = "0",
                millisSinceEpoch = 0,
            ),
            testTransferResource(
                id = "1",
                millisSinceEpoch = 3,
            ),
            testTransferResource(
                id = "2",
                millisSinceEpoch = 1,
            ),
            testTransferResource(
                id = "3",
                millisSinceEpoch = 2,
            ),
        )
        transferResourceDao.upsertTransferResources(
            transferResourceEntities,
        )

        val savedTransfersResourceEntities = transferResourceDao.getTransferResources(
            useFilterTransferIds = true,
            filterTransferIds = setOf("3", "0"),
        )
            .first()

        kotlin.test.assertEquals(
            listOf("3", "0"),
            savedTransfersResourceEntities.map {
                it.id
            },
        )
    }


    @Test
    fun newsResourceDao_deletes_items_by_ids() =
        runTest {
            val transfersResourceEntities = listOf(
                testTransferResource(
                    id = "0",
                    millisSinceEpoch = 0,
                ),
                testTransferResource(
                    id = "1",
                    millisSinceEpoch = 3,
                ),
                testTransferResource(
                    id = "2",
                    millisSinceEpoch = 1,
                ),
                testTransferResource(
                    id = "3",
                    millisSinceEpoch = 2,
                ),
            )
            transferResourceDao.upsertTransferResources(transfersResourceEntities)

            val (toDelete, toKeep) = transfersResourceEntities.partition { it.id.toInt() % 2 == 0 }

            transferResourceDao.deleteTransferResources(
                toDelete.map(TransferResourceEntity::id),
            )

            kotlin.test.assertEquals(
                toKeep.map(TransferResourceEntity::id)
                    .toSet(),
                transferResourceDao.getTransferResources().first()
                    .map { it.id }
                    .toSet(),
            )
        }
}

private fun testTransferResource(
    id: String = "0",
    millisSinceEpoch: Long = 0,
) = TransferResourceEntity(
    id = id,
    name = "",
    clubFrom = "",
    clubFromImg = "",
    clubTo = "",
    clubToImg = "",
    footballerImg = "",
    price = "",
    url = ""
)