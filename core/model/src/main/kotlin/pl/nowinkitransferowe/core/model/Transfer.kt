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
 * A transfer of [player] from [from] to [to] on [date].
 */
data class Transfer(
    val id: Int,
    val player: PlayerRef,
    val from: ClubRef,
    val to: ClubRef,
    val fee: Fee,
    val date: LocalDate,
    val articleId: Int?,
    /** The last day of a loan; null for other transfers. */
    val loanUntil: LocalDate? = null,
) {
    val season: Season get() = Season.of(date)

    /** A loan that has not ended on [today]. */
    fun isLoanOn(today: LocalDate): Boolean = isLoanOn(fee, loanUntil, today)

    /** Where the transfer leaves the player on [today]: after a loan ends he is back at [from], as on the website. */
    fun clubOn(today: LocalDate): ClubRef = clubOn(from, to, fee, loanUntil, today)

    /** A contract extension (the same club on both sides); left out of spending, income and season lists. */
    val isExtension: Boolean
        get() = from is ClubRef.Club && to is ClubRef.Club && from.id == to.id
}

internal fun isLoanOn(fee: Fee, loanUntil: LocalDate?, today: LocalDate): Boolean = fee.kind == FeeKind.LOAN && (loanUntil == null || today <= loanUntil)

internal fun clubOn(from: ClubRef, to: ClubRef, fee: Fee, loanUntil: LocalDate?, today: LocalDate): ClubRef = if (fee.kind == FeeKind.LOAN && loanUntil != null && today > loanUntil && from is ClubRef.Club) from else to

/**
 * A player as a transfer or rumour refers to him; his current name comes from the players table
 * when it is there.
 */
data class PlayerRef(
    val id: Int,
    val name: String,
    val photoUrl: String? = null,
    /** Country code of the first nationality, for the flag; null for a stub. */
    val nationality: String? = null,
)
