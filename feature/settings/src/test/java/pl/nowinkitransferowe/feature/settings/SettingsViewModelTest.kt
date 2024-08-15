package pl.nowinkitransferowe.feature.settings

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlinx.coroutines.flow.collect
import org.junit.Before
import org.junit.Rule
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule


class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(userDataRepository)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        kotlin.test.assertEquals(SettingsUiState.Loading, viewModel.settingsUiState.value)
    }

    @Test
    fun stateIsSuccessAfterUserDataLoaded() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.settingsUiState.collect() }

        userDataRepository.setDarkThemeConfig(DarkThemeConfig.DARK)

        kotlin.test.assertEquals(
            SettingsUiState.Success(
                UserEditableSettings(
                    darkThemeConfig = DarkThemeConfig.DARK,
                    useDynamicColor = false,
                    isNewsNotificationsAllowed = true,
                    isTransfersNotificationsAllowed = true,
                    isGeneralNotificationsAllowed = true
                ),
            ),
            viewModel.settingsUiState.value,
        )

        collectJob.cancel()
    }
}