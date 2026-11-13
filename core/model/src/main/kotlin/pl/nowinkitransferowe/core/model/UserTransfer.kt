/*
 * Copyright 2026 The Android Open Source Project
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

package pl.nowinkitransferowe.core.model

import kotlinx.datetime.LocalDate

/**
 * A [Transfer] with the user's data: whether it is saved and whether it was opened.
 */
data class UserTransfer internal constructor(
    val id: Int,
    val player: PlayerRef,
    val from: ClubRef,
    val to: ClubRef,
    val fee: Fee,
    val date: LocalDate,
    val articleId: Int?,
    val isSaved: Boolean,
    val hasBeenViewed: Boolean,
) {
    constructor(transfer: Transfer, userData: UserData) : this(
        id = transfer.id,
        player = transfer.player,
        from = transfer.from,
        to = transfer.to,
        fee = transfer.fee,
        date = transfer.date,
        articleId = transfer.articleId,
        isSaved = transfer.id.toString() in userData.bookmarkedTransferResources,
        hasBeenViewed = transfer.id.toString() in userData.viewedTransferResources,
    )

    val season: Season get() = Season.of(date)
}

fun List<Transfer>.mapToUserTransfers(userData: UserData): List<UserTransfer> = map { UserTransfer(it, userData) }
