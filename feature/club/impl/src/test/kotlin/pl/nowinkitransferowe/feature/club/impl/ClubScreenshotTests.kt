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

package pl.nowinkitransferowe.feature.club.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.screenshottesting.DefaultTestDevices
import pl.nowinkitransferowe.core.screenshottesting.captureForDevice
import pl.nowinkitransferowe.core.screenshottesting.captureMultiDevice
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.data.userTransfersTestData
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ClubScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun clubScreen_finances() {
        composeTestRule.captureMultiDevice("ClubScreenFinances") { Screen(ClubTab.FINANCES) }
    }

    @Test
    fun clubScreen_finances_dark() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "ClubScreenFinances", darkMode = true) {
            Screen(ClubTab.FINANCES)
        }
    }

    @Test
    fun clubScreen_arrivals_allSeasons() {
        composeTestRule.captureForDevice("phone", DefaultTestDevices.PHONE.spec, "ClubScreenArrivalsAllSeasons") { Screen(ClubTab.ARRIVALS, season = null) }
    }

    @Composable
    private fun Screen(tab: ClubTab, season: Season? = westHamSeasonsTestData.first().season) {
        NtTheme {
            NtBackground {
                ClubScreen(
                    uiState = ClubUiState.Success(
                        club = clubsTestData[0].copy(stadium = "London Stadium"),
                        country = countriesTestData[0],
                        league = topicsTestData[0],
                        tab = tab,
                        seasons = westHamSeasonsTestData,
                        availableSeasons = westHamSeasonsTestData.map { it.season },
                        season = season,
                        arrivals = userTransfersTestData.filter { (it.to as? ClubRef.Club)?.id == 83 },
                        departures = emptyList(),
                        rumours = emptyList(),
                        transferCount = 2,
                    ),
                    showBackButton = true,
                    onBackClick = {},
                    onTabSelected = {},
                    onTransferClick = {},
                    onRumourClick = {},
                )
            }
        }
    }
}
