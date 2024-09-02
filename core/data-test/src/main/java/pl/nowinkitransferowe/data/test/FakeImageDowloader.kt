package pl.nowinkitransferowe.data.test

import android.graphics.Bitmap
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import javax.inject.Inject

class FakeImageDownloader @Inject constructor() : ImageDownloader {
    override suspend fun loadImage(url: String): Bitmap? {
        return null
    }
}