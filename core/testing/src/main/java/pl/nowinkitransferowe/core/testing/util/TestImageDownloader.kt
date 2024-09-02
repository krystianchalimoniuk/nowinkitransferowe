package pl.nowinkitransferowe.core.testing.util

import android.graphics.Bitmap
import pl.nowinkitransferowe.core.data.util.ImageDownloader

class TestImageDownloader : ImageDownloader {
    override suspend fun loadImage(url: String): Bitmap? {
        return null
    }
}