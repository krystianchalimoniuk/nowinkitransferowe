package pl.nowinkitransferowe.core.network.model.utill

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.asNewsCategoryType

object NewsCategoryTypeSerializer : KSerializer<NewsCategory> {
    override fun deserialize(decoder: Decoder): NewsCategory =
        decoder.decodeString().asNewsCategoryType()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "category", kind = PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NewsCategory) =
        encoder.encodeString(value.serializedName)
}