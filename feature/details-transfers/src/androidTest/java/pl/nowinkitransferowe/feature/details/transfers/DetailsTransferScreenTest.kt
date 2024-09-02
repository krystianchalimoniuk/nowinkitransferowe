package pl.nowinkitransferowe.feature.details.transfers

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.userTransferResourcesTestData

class DetailsTransferScreenTest {
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun circularProgressIndicator_whenScreenIsLoading_exists() {
        composeTestRule.setContent {
            DetailsTransferScreen(
                detailsTransferUiState = DetailsTransferUiState.Loading,
                showBackButton = true,
                onBackClick = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_details_transfers_loading),
            )
            .assertExists()
    }

    @Test
    fun feed_whenLoaded_showsDetailsScreen() {
        composeTestRule.setContent {
            DetailsTransferScreen(
                detailsTransferUiState = DetailsTransferUiState.Success(
                    userTransferResource = userTransferResourcesTestData,
                    dataPoints = arrayListOf(
                        DataPoint(date = "paź 22", price = 0.0f, bitmap = null),
                    ),
                ),
                showBackButton = true,
                onBackClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                userTransferResourcesTestData[0].name,
                substring = true,
            )
            .assertExists()
    }


    @Test
    fun whenEntityExistAndHasMoreThanTwoNonFreeTransfer_calculationsAreCorrect() {
        composeTestRule.setContent {
            DetailsTransferScreen(
                detailsTransferUiState = DetailsTransferUiState.Success(
                    userTransferResource = arrayListOf(
                        userTransferResourcesTestData[2],
                        userTransferResourcesTestData[3],
                    ),
                    dataPoints = arrayListOf(
                        DataPoint(date = "paź 22", price = 0.5f, bitmap = null),
                        DataPoint(date = "paź 22", price = 34.5f, bitmap = null)
                    ),
                ),
                showBackButton = true,
                onBackClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                "35.0 mln €",
                substring = true,
            )
            .assertExists()

        composeTestRule
            .onNodeWithText(
                "Wartość transferów",
                substring = true,
            )
            .assertExists()

        composeTestRule
            .onNodeWithText(
                "Najwyższy transfer:\n34.5 mln €",
                substring = true,
            )
            .assertExists()
    }
}
