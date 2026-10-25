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

package pl.nowinkitransferowe.feature.details.transfers

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object Util {
    fun shortcutDate(date: String): String {
        val list = date.split(" ")
        val stringBuilder = StringBuilder()
        list.forEachIndexed { index, s ->
            if (index == 1) {
                stringBuilder.append(s)
                stringBuilder.append(" ")
            } else if (index == 2) {
                stringBuilder.append(s.takeLast(2))
            }
        }
        return stringBuilder.toString()
    }

    fun priceToFloat(price: String): Float {
        if (price == "za darmo" || price == "nie ujawniono" || price == "wypożyczenie") {
            return 0.0f
        } else {
            val value = price.split(" ")[0].replace(",", ".").toFloatOrNull()
            if (value != null) {
                return value
            }
        }
        return 0f
    }

    fun hasMoreThanOrEqualTwoCashTransfers(prices: List<String>): Boolean {
        var cashTransfersSum = 0
        prices.forEach {
            val priceInFloat = priceToFloat(it)
            if (priceInFloat != 0.0f) {
                cashTransfersSum += 1
            }
        }
        return cashTransfersSum >= 2
    }

    fun calculateTransferSum(prices: List<String>): String {
        var sum = 0.0
        prices.forEach { price ->
            if (price == "za darmo" || price == "nie ujawniono" || price == "wypożyczenie") {
                sum += 0.0
            } else {
                val value = price.split(" ")[0].replace(",", ".").toDoubleOrNull()
                if (value != null) {
                    sum += value
                }
            }
        }
        return " $sum mln €"
    }

    fun calculateMaxTransferValue(prices: List<String>): String {
        var maxValue = 0f
        prices.forEach {
            val priceInFloat = priceToFloat(it)
            if (priceInFloat > maxValue) {
                maxValue = priceInFloat
            }
        }
        return "$maxValue mln €"
    }

    fun dateFormatted(
        publishDate: Instant,
        style: FormatStyle = FormatStyle.MEDIUM,
    ): String = DateTimeFormatter
        .ofLocalizedDate(style)
        .withLocale(Locale.forLanguageTag("pl"))
        .withZone(ZoneId.of("Europe/Warsaw"))
        .format(publishDate.toJavaInstant())
}
