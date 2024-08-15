package pl.nowinkitransferowe.core.model

data class TransferResource(
    val id: String,
    val name: String,
    val footballerImg: String,
    val clubFrom: String,
    val clubFromImg: String,
    val clubTo: String,
    val clubToImg: String,
    val price: String,
    val url: String
)