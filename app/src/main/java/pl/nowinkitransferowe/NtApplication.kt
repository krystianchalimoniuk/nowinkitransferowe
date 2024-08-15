package pl.nowinkitransferowe

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import pl.nowinkitransferowe.nowinkitransferowe.util.ProfileVerifierLogger
import pl.nowinkitransferowe.sync.work.initializers.Sync
import javax.inject.Inject

/**
 * [Application] class for NT
 */
@HiltAndroidApp
class NtApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    lateinit var profileVerifierLogger: ProfileVerifierLogger

    override fun onCreate() {
        super.onCreate()
        // Initialize Sync; the system responsible for keeping data in the app up to date.
        Sync.initialize(context = this)
        profileVerifierLogger()
    }

    override fun newImageLoader(): ImageLoader = imageLoader.get()
}
