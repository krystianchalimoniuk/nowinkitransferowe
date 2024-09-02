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
        list.forEachIndexed() { index, s ->
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

    fun hasMoreThanTwoCashTransfers(prices: List<String>): Boolean {
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
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.of("Europe/Warsaw"))
        .format(publishDate.toJavaInstant())
}