package pl.nowinkitransferowe.news2pane

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
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule
import pl.nowinkitransferowe.stringResource
import pl.nowinkitransferowe.ui.news2pane.NewsListDetailScreen
import pl.nowinkitransferowe.uitesthiltmanifest.HiltComponentActivity
import javax.inject.Inject
import kotlin.test.assertTrue
import pl.nowinkitransferowe.feature.details.R as FeatureDetailsR

@HiltAndroidTest
class NewsListDetailScreenTest {
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
    lateinit var newsRepository: NewsRepository

    // The strings used for matching in these tests.
    private val placeholderText by composeTestRule.stringResource(FeatureDetailsR.string.feature_details_select_an_news)
    private val listPaneTag = "news:feed"

    private val NewsResource.testTag
        get() = "news:${this.id}"

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

    /** Convenience function for getting all news during tests, */
    private fun getNews(): List<NewsResource> = runBlocking {
        newsRepository.getNewsResources().first()
    }

    @Test
    fun expandedWidth_initialState_showsTwoPanesWithPlaceholder() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Expanded) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            NewsListDetailScreen(
                                windowAdaptiveInfo = adaptiveInfo,
                                onTopicClick = {})
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
                            NewsListDetailScreen(
                                windowAdaptiveInfo = adaptiveInfo,
                                onTopicClick = {})
                        }
                    }
                }
            }

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
        }
    }

    @Test
    fun expandedWidth_newsSelected_updatesDetailPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Expanded) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            NewsListDetailScreen(
                                windowAdaptiveInfo = adaptiveInfo,
                                onTopicClick = {})
                        }
                    }
                }
            }

            val firstNews = getNews().first()
            onNodeWithText(firstNews.title).performClick()

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstNews.testTag).assertIsDisplayed()
        }
    }

    @Test
    fun compactWidth_newsSelected_showsNewsDetailPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Compact) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            NewsListDetailScreen(windowAdaptiveInfo = adaptiveInfo, onTopicClick = {})
                        }
                    }
                }
            }

            val firstNews = getNews().first()
            onNodeWithText(firstNews.title).performClick()

            onNodeWithTag(listPaneTag).assertIsNotDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstNews.testTag).assertIsDisplayed()
        }
    }

    @Test
    fun expandedWidth_backPressFromTopicDetail_leavesInterests() {
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
                            NewsListDetailScreen(windowAdaptiveInfo = adaptiveInfo, onTopicClick = {})
                        }
                    }
                }
            }

            val firstNews = getNews().first()
            onNodeWithText(firstNews.title).performClick()

            Espresso.pressBack()

            assertTrue(unhandledBackPress)
        }
    }

    @Test
    fun compactWidth_backPressFromTopicDetail_showsListPane() {
        composeTestRule.apply {
            setContent {
                with(TestDeviceConfig.Compact) {
                    DeviceConfigurationOverride(override = sizeOverride) {
                        NtTheme {
                            NewsListDetailScreen(windowAdaptiveInfo = adaptiveInfo, onTopicClick = {})
                        }
                    }
                }
            }

            val firstNews = getNews().first()
            onNodeWithText(firstNews.title).performClick()

            Espresso.pressBack()

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstNews.testTag).assertIsNotDisplayed()
        }
    }
}
