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

package pl.nowinkitransferowe.core.notifications

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.crestUrl

private const val WIDTH = 1024
private const val HEIGHT = 440
private const val CREST = 168f
private const val CREST_INSET = 22f
private const val MIN_FEE_SIZE = 36f
private const val BACKGROUND = 0xFF3A0A0F.toInt()
private const val CREST_BACKGROUND = 0xFFFFFFFF.toInt()
private const val INITIALS = 0xFF5A3F3C.toInt()
private const val LABEL = 0xFFFFDAD6.toInt()
private const val ACCENT = 0xFFFFB3AC.toInt()

/**
 * The picture of a transfer in its notification, like its card in the app: the crests of the
 * two clubs on the brand red, with the fee and an arrow between them and their names below.
 * Missing crests show the club's initials.
 */
internal fun transferGraphic(transfer: Transfer, images: Map<String, Bitmap>, feeLabel: String): Bitmap {
    val bitmap = createBitmap(WIDTH, HEIGHT)
    val canvas = Canvas(bitmap)
    canvas.drawColor(BACKGROUND)
    // The crests, their names and the fee in the middle of the picture
    val crestTop = (HEIGHT - CREST - 70f) / 2
    val leftCenter = WIDTH * 0.2f
    val rightCenter = WIDTH * 0.8f
    drawCrest(canvas, transfer.from, images, leftCenter, crestTop)
    drawCrest(canvas, transfer.to, images, rightCenter, crestTop)

    val nameY = crestTop + CREST + 60f
    val namePaint = textPaint(LABEL, 34f, Typeface.DEFAULT)
    drawCentered(canvas, transfer.from.name, namePaint, leftCenter, nameY, WIDTH * 0.36f)
    drawCentered(canvas, transfer.to.name, namePaint, rightCenter, nameY, WIDTH * 0.36f)

    val middle = WIDTH / 2f
    val crestMiddle = crestTop + CREST / 2
    // The fee between the crests, as big as it fits
    val feeWidth = rightCenter - leftCenter - CREST - 40f
    val feePaint = textPaint(0xFFFFFFFF.toInt(), if (transfer.fee.kind == FeeKind.AMOUNT) 80f else 52f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
    while (feePaint.measureText(feeLabel) > feeWidth && feePaint.textSize > MIN_FEE_SIZE) feePaint.textSize -= 2f
    drawCentered(canvas, feeLabel, feePaint, middle, crestMiddle + 4f, feeWidth)
    drawArrow(canvas, middle, crestMiddle + 58f)
    return bitmap
}

private fun drawCrest(canvas: Canvas, club: ClubRef, images: Map<String, Bitmap>, centerX: Float, top: Float) {
    val circle = RectF(centerX - CREST / 2, top, centerX + CREST / 2, top + CREST)
    canvas.drawOval(circle, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CREST_BACKGROUND })
    val crest = club.crestUrl?.let(images::get)
    if (crest != null) {
        val inner = RectF(circle).apply { inset(CREST_INSET, CREST_INSET) }
        val fitted = fit(crest, inner)
        canvas.withClip(Path().apply { addOval(circle, Path.Direction.CW) }) {
            drawBitmap(crest, null, fitted, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
    } else {
        val paint = textPaint(INITIALS, 52f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        drawCentered(canvas, initials(club.name), paint, centerX, circle.centerY() + 18f, CREST)
    }
}

/** Scales [bitmap] into [box], keeping its proportions. */
private fun fit(bitmap: Bitmap, box: RectF): RectF {
    val scale = minOf(box.width() / bitmap.width, box.height() / bitmap.height)
    val width = bitmap.width * scale
    val height = bitmap.height * scale
    return RectF(box.centerX() - width / 2, box.centerY() - height / 2, box.centerX() + width / 2, box.centerY() + height / 2)
}

private fun drawArrow(canvas: Canvas, centerX: Float, y: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ACCENT
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    val half = 70f
    canvas.drawLine(centerX - half, y, centerX + half, y, paint)
    canvas.drawLine(centerX + half, y, centerX + half - 24f, y - 20f, paint)
    canvas.drawLine(centerX + half, y, centerX + half - 24f, y + 20f, paint)
}

private fun textPaint(color: Int, size: Float, typeface: Typeface) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    textSize = size
    this.typeface = typeface
    textAlign = Paint.Align.CENTER
}

private fun drawCentered(canvas: Canvas, text: String, paint: TextPaint, centerX: Float, baseline: Float, maxWidth: Float) {
    val shown = TextUtils.ellipsize(text, paint, maxWidth, TextUtils.TruncateAt.END).toString()
    canvas.drawText(shown, centerX, baseline, paint)
}

/** "Real Madryt" -> "RM", "Gavi" -> "GAV", like the crest placeholders in the app. */
internal fun initials(name: String): String {
    val words = name.split(' ', '-').filter { it.isNotBlank() && it.first().isLetterOrDigit() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(3).uppercase()
        else -> words.take(2).joinToString("") { it.first().uppercase() }
    }
}

/** A square copy of [bitmap] cut to a circle, for the large icon of a notification. */
internal fun circleCrop(bitmap: Bitmap, background: Int? = null): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val output = createBitmap(size, size)
    val canvas = Canvas(output)
    val circle = RectF(0f, 0f, size.toFloat(), size.toFloat())
    canvas.withClip(Path().apply { addOval(circle, Path.Direction.CW) }) {
        background?.let { drawColor(it) }
        // Photos are cut from the top, where the face is
        val source = Rect((bitmap.width - size) / 2, 0, (bitmap.width - size) / 2 + size, size)
        drawBitmap(bitmap, source, circle, Paint(Paint.FILTER_BITMAP_FLAG))
    }
    return output
}

/** A crest fitted into a white circle, for the large icon of a notification about a club. */
internal fun crestIcon(crest: Bitmap): Bitmap {
    val size = 192
    val output = createBitmap(size, size)
    val canvas = Canvas(output)
    val circle = RectF(0f, 0f, size.toFloat(), size.toFloat())
    canvas.drawOval(circle, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CREST_BACKGROUND })
    canvas.drawBitmap(crest, null, fit(crest, RectF(circle).apply { inset(26f, 26f) }), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    return output
}
