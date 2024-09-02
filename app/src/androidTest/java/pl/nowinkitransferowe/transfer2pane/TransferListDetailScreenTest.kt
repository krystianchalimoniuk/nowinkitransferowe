package pl.nowinkitransferowe.transfer2pane

import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.window.core.layout.WindowSizeClass
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule
import pl.nowinkitransferowe.stringResource
import pl.nowinkitransferowe.ui.transfers2pane.TransfersListDetailScreen
import pl.nowinkitransferowe.uitesthiltmanifest.HiltComponentActivity
import javax.inject.Inject
import kotlin.test.assertTrue

@HiltAndroidTest
class TransferListDetailScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @get:Rule(order = 1)
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @get:Rule(order = 4)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @Inject
    lateinit var transferRepository: TransferRepository

    // The strings used for matching in these tests.
    private val placeholderText by composeTestRule.stringResource(pl.nowinkitransferowe.feature.details.transfers.R.string.feature_details_transfers_select_a_transfer)
    private val listPaneTag = "transfers:feed"

    private val TransferResource.testTag
        get() = "transfer:${this.id}"

    // Overrides for device sizes.
    private enum class TestDeviceConfig(widthDp: Float, heightDp: Float) {
        Compact(412f, 915f),
        Expanded(1200f, 840f),
        ;

        val sizeOverride = DeviceConfigurationOverride.ForcedSize(DpSize(widthDp.dp, heightDp.dp))
        val adaptiveInfo = WindowAdaptiveInfo(
            windowSizeClass = WindowSizeClass.compute(widthDp, heightDp),
            windowPosture = Posture(),
        )
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    /** Convenience function for getting all transfer during tests, */
    private fun getTransfers(): List<TransferResource> = runBlocking {
        transferRepository.getTransferResources().first()
    }

    @Test
    fun expandedWidth_initialState_showsTwoPanesWithPlaceholder() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Expanded) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            TransfersListDetailScreen(
                                windowAdaptiveInfo = adaptiveInfo,
                            )
                        }
                    }
                }
            }

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsDisplayed()
        }
    }

    @Test
    fun compactWidth_initialState_showsListPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Compact) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            TransfersListDetailScreen(
                                windowAdaptiveInfo = adaptiveInfo,
                            )
                        }
                    }
                }
            }

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
        }
    }

    @Test
    fun expandedWidth_transferSelected_updatesDetailPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Expanded) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            TransfersListDetailScreen(
                                windowAdaptiveInfo = adaptiveInfo,
                            )
                        }
                    }
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstTransfers.testTag).assertIsDisplayed()
        }
    }

    @Test
    fun compactWidth_transferSelected_showsTransferDetailPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Compact) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            TransfersListDetailScreen(windowAdaptiveInfo = adaptiveInfo)
                        }
                    }
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            onNodeWithTag(listPaneTag).assertIsNotDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstTransfers.testTag).assertIsDisplayed()
        }
    }

    @Test
    fun expandedWidth_backPressFromTransferDetail_leavesInterests() {
        var unhandledBackPress = false
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Expanded) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            // Back press should not be handled by the two pane layout, and thus
                            // "fall through" to this BackHandler.
                            BackHandler {
                                unhandledBackPress = true
                            }
                            TransfersListDetailScreen(windowAdaptiveInfo = adaptiveInfo)
                        }
                    }
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            Espresso.pressBack()

            assertTrue(unhandledBackPress)
        }
    }

    @Test
    fun compactWidth_backPressFromTransferDetail_showsListPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Compact) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            TransfersListDetailScreen(windowAdaptiveInfo = adaptiveInfo)
                        }
                    }
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            Espresso.pressBack()

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstTransfers.testTag).assertIsNotDisplayed()
        }
    }
}