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
    val url: String,
)

fun TransferResourceEntity.asExternalModel() = TransferResource(id, name, footballerImg, clubFrom, clubFromImg, clubTo, clubToImg, price, url)
