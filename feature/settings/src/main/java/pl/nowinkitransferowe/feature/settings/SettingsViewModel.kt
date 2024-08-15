package pl.nowinkitransferowe.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> =
        userDataRepository.userData
            .map { userData ->
                SettingsUiState.Success(
                    settings = UserEditableSettings(
                        useDynamicColor = userData.useDynamicColor,
                        darkThemeConfig = userData.darkThemeConfig,
                        isNewsNotificationsAllowed = userData.isNewsNotificationsAllowed,
                        isTransfersNotificationsAllowed = userData.isTransfersNotificationsAllowed,
                        isGeneralNotificationsAllowed = userData.isGeneralNotificationAllowed
                    ),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = SettingsUiState.Loading,
            )

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userDataRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }

    fun updateDynamicColorPreference(useDynamicColor: Boolean) {
        viewModelScope.launch {
            userDataRepository.setDynamicColorPreference(useDynamicColor)
        }
    }

    fun updateNewsNotificationsPreference(isAllowed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsNotificationsAllowed(isAllowed)
        }
    }
    fun updateTransfersNotificationsPreference(isAllowed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransfersNotificationsAllowed(isAllowed)
        }
    }
    fun updateGeneralNotificationsPreference(isAllowed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setGeneralNotificationsAllowed(isAllowed)
        }
    }
}

/**
 * Represents the settings which the user can edit within the app.
 */
data class UserEditableSettings(
    val useDynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig,
    val isNewsNotificationsAllowed: Boolean,
    val isTransfersNotificationsAllowed: Boolean,
    val isGeneralNotificationsAllowed: Boolean,
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}
