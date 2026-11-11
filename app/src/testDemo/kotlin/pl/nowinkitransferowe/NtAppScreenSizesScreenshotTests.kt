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

package pl.nowinkitransferowe

import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.github.takahirom.roborazzi.captureRoboImage
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserNewsResourceRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferResourceRepository
import pl.nowinkitransferowe.core.data.util.NetworkMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.screenshottesting.DefaultRoborazziOptions
import pl.nowinkitransferowe.ui.NtApp
import pl.nowinkitransferowe.ui.rememberNtAppState
import pl.nowinkitransferowe.uitesthiltmanifest.HiltComponentActivity
import java.util.TimeZone
import javax.inject.Inject

/**
 * Tests that the navigation UI is rendered correctly on different screen sizes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Configure Robolectric to use a very large screen size that can fit all of the test sizes.
// This allows enough room to render the content under test without clipping or scaling.
@Config(application = HiltTestApplication::class, qualifiers = "w1000dp-h1000dp-480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
@HiltAndroidTest
class NtAppScreenSizesScreenshotTests {

    /**
     * Manages the components' state and is used to perform injection on your test
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Create a temporary folder used to create a Data Store file. This guarantees that
     * the file is removed in between each test, preventing a crash.
     */
    @BindValue
    @get:Rule(order = 1)
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    /**
     * Use a test activity to set the content on.
     */
    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    lateinit var userTransferResourceRepository: UserTransferResourceRepository

    @Inject
    lateinit var userNewsResourceRepository: UserNewsResourceRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Before
    fun setTimeZone() {
        // Make time zone deterministic in tests
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun testNtAppScreenshotWithSize(width: Dp, height: Dp, screenshotName: String) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
            ) {
                DeviceConfigurationOverride(
                    override = DeviceConfigurationOverride.ForcedSize(DpSize(width, height)),
                ) {
                    NtTheme {
                        val fakeAppState = rememberNtAppState(
                            networkMonitor = networkMonitor,
                            userNewsResourceRepository = userNewsResourceRepository,
                            userTransferResourceRepository = userTransferResourceRepository,
                            timeZoneMonitor = timeZoneMonitor,
                        )
                        NtApp(
                            fakeAppState,
                            windowAdaptiveInfo = WindowAdaptiveInfo(
                                windowSizeClass = WindowSizeClass.compute(
                                    width.value,
                                    height.value,
                                ),
                                windowPosture = Posture(),
                            ),
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(
                "src/testDemo/screenshots/$screenshotName.png",
                roborazziOptions = DefaultRoborazziOptions,
            )
    }

    @Test
    fun compactWidth_compactHeight_showsNavigationBar() {
        testNtAppScreenshotWithSize(
            400.dp,
            400.dp,
            "compactWidth_compactHeight_showsNavigationBar",
        )
    }

    @Test
    fun mediumWidth_compactHeight_showsNavigationBar() {
        testNtAppScreenshotWithSize(
            610.dp,
            400.dp,
            "mediumWidth_compactHeight_showsNavigationBar",
        )
    }

    @Test
    fun expandedWidth_compactHeight_showsNavigationBar() {
        testNtAppScreenshotWithSize(
            900.dp,
            400.dp,
            "expandedWidth_compactHeight_showsNavigationBar",
        )
    }

    @Test
    fun compactWidth_mediumHeight_showsNavigationBar() {
        testNtAppScreenshotWithSize(
            400.dp,
            500.dp,
            "compactWidth_mediumHeight_showsNavigationBar",
        )
    }

    @Test
    fun mediumWidth_mediumHeight_showsNavigationRail() {
        testNtAppScreenshotWithSize(
            610.dp,
            500.dp,
            "mediumWidth_mediumHeight_showsNavigationRail",
        )
    }

    @Test
    fun expandedWidth_mediumHeight_showsNavigationRail() {
        testNtAppScreenshotWithSize(
            900.dp,
            500.dp,
            "expandedWidth_mediumHeight_showsNavigationRail",
        )
    }

    @Test
    fun compactWidth_expandedHeight_showsNavigationBar() {
        testNtAppScreenshotWithSize(
            400.dp,
            1000.dp,
            "compactWidth_expandedHeight_showsNavigationBar",
        )
    }

    @Test
    fun mediumWidth_expandedHeight_showsNavigationRail() {
        testNtAppScreenshotWithSize(
            610.dp,
            1000.dp,
            "mediumWidth_expandedHeight_showsNavigationRail",
        )
    }

    @Test
    fun expandedWidth_expandedHeight_showsNavigationRail() {
        testNtAppScreenshotWithSize(
            900.dp,
            1000.dp,
            "expandedWidth_expandedHeight_showsNavigationRail",
        )
    }
}
