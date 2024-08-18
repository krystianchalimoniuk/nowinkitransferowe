/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
