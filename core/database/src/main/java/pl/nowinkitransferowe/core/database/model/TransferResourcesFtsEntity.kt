package pl.nowinkitransferowe.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "transferResourcesFts")
@Fts4
data class TransferResourceFtsEntity(

    @ColumnInfo(name = "transferResourceId")
    val transferResourceId: String,

    @ColumnInfo(name = "name")
    val name: String,
)

fun TransferResourceEntity.asFtsEntity() = TransferResourceFtsEntity(
    transferResourceId = id,
    name = name,
)