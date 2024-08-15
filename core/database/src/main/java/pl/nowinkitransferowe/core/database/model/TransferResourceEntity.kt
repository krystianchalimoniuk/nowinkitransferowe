package pl.nowinkitransferowe.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.nowinkitransferowe.core.model.TransferResource

/**
 * Defines an NT news resource.
 */
@Entity(
    tableName = "transfer_resources",
)
data class TransferResourceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo("footballer_img")
    val footballerImg: String,
    @ColumnInfo("club_from")
    val clubFrom: String,
    @ColumnInfo("club_from_img")
    val clubFromImg: String,
    @ColumnInfo("club_to")
    val clubTo: String,
    @ColumnInfo("club_to_img")
    val clubToImg: String,
    val price: String,
    val url: String
)

fun TransferResourceEntity.asExternalModel() = TransferResource(id, name, footballerImg, clubFrom, clubFromImg, clubTo, clubToImg, price, url)

