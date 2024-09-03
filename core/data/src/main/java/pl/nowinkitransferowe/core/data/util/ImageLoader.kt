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

package pl.nowinkitransferowe.core.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.nowinkitransferowe.core.network.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

interface ImageDownloader {
    suspend fun loadImage(url: String): Bitmap?
}

@Singleton
internal class DefaultImageDownLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageDownloader {

    override suspend fun loadImage(url: String): Bitmap? {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data("${BuildConfig.IMAGES_URL}$url".toUri())
            .allowHardware(false)
            .build()

        return when (val result = imageLoader.execute(request)) {
            is ErrorResult -> {
                null
            }
            is SuccessResult -> {
                (result.drawable as BitmapDrawable).bitmap
            }
        }
    }
}
