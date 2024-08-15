package pl.nowinkitransferowe.core.data.model

import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource

fun NetworkTransferResource.asEntity() = TransferResourceEntity(
    id = id.toString(),
    name = name,
    footballerImg = footballerImg,
    clubTo = clubTo,
    clubToImg = clubToImg,
    clubFrom = clubFrom,
    clubFromImg = clubFromImg,
    price = price,
    url = link
)