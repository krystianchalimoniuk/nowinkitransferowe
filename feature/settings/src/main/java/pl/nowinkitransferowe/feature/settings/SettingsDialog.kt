package pl.nowinkitransferowe.feature.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import pl.nowinkitransferowe.core.designsystem.component.NtIconButton
import pl.nowinkitransferowe.core.designsystem.component.NtTextButton
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.designsystem.theme.supportsDynamicTheming
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.network.BuildConfig
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.feature.settings.R.string


@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    SettingsDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
        onChangeNewsNotificationsPreference = viewModel::updateNewsNotificationsPreference,
        onChangeTransfersNotificationsPreference = viewModel::updateTransfersNotificationsPreference,
        onChangeGeneralNotificationsPreference = viewModel::updateGeneralNotificationsPreference
    )
}


@Composable
fun SettingsDialog(
    settingsUiState: SettingsUiState,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
    onDismiss: () -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    onChangeNewsNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onChangeTransfersNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onChangeGeneralNotificationsPreference: (isAllowed: Boolean) -> Unit,
) {
    val configuration = LocalConfiguration.current

    /**
     * usePlatformDefaultWidth = false is use as a temporary fix to allow
     * height recalculation during recomposition. This, however, causes
     * Dialog's to occupy full width in Compact mode. Therefore max width
     * is configured below. This should be removed when there's fix to
     * https://issuetracker.google.com/issues/221643630
     */
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.widthIn(max = configuration.screenWidthDp.dp - 40.dp),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(string.feature_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (settingsUiState) {
                    SettingsUiState.Loading -> {
                        Text(
                            text = stringResource(string.feature_settings_loading),
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }

                    is SettingsUiState.Success -> {
                        NotificationPanel(
                            settings = settingsUiState.settings,
                            onChangeNewsNotificationsPreference = onChangeNewsNotificationsPreference,
                            onChangeTransfersNotificationsPreference = onChangeTransfersNotificationsPreference,
                            onChangeGeneralNotificationsPreference = onChangeGeneralNotificationsPreference
                        )
                        SettingsPanel(
                            settings = settingsUiState.settings,
                            supportDynamicColor = supportDynamicColor,
                            onChangeDynamicColorPreference = onChangeDynamicColorPreference,
                            onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
                SocialMediaPanel()
                HorizontalDivider(Modifier.padding(top = 8.dp))
                LinksPanel()
            }
            TrackScreenViewEvent(screenName = "Settings")
        },
        confirmButton = {
            Text(
                text = stringResource(string.feature_settings_dismiss_dialog_button_text),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onDismiss() },
            )
        },
    )
}


@Composable
private fun ColumnScope.NotificationPanel(
    settings: UserEditableSettings,
    onChangeNewsNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onChangeTransfersNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onChangeGeneralNotificationsPreference: (isAllowed: Boolean) -> Unit,

    ) {
    val context = LocalContext.current
    val isNotificationsAllowed = remember { mutableStateOf(checkIfNotificationsAllowed(context)) }
    val isNotificationChannelEnabled =
        remember { mutableStateOf(checkNotificationChannelEnabled(context)) }
    AnimatedVisibility(visible = isNotificationsAllowed.value && isNotificationChannelEnabled.value) {
        Column {
            SettingsDialogSectionTitle(text = stringResource(string.feature_settings_notification_preference))
            Column {
                SettingsDialogNotificationSwitchRow(
                    text = stringResource(id = string.feature_settings_notification_channel_news),
                    checked = settings.isNewsNotificationsAllowed,
                    onCheckedChange = onChangeNewsNotificationsPreference
                )
                SettingsDialogNotificationSwitchRow(
                    text = stringResource(id = string.feature_settings_notification_channel_transfers),                    checked = settings.isTransfersNotificationsAllowed,
                    onCheckedChange = onChangeTransfersNotificationsPreference
                )
                SettingsDialogNotificationSwitchRow(
                    text = stringResource(id = string.feature_settings_notification_channel_general),                    checked = settings.isGeneralNotificationsAllowed,
                    onCheckedChange = onChangeGeneralNotificationsPreference
                )
            }
        }
    }

}

fun checkIfNotificationsAllowed(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}

fun checkNotificationChannelEnabled(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel("update_channel")
        channel?.importance != NotificationManager.IMPORTANCE_NONE
    } else {
        true
    }
}

// [ColumnScope] is used for using the [ColumnScope.AnimatedVisibility] extension overload composable.
@Composable
private fun ColumnScope.SettingsPanel(
    settings: UserEditableSettings,
    supportDynamicColor: Boolean,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {

    AnimatedVisibility(supportDynamicColor) {
        Column {
            SettingsDialogSectionTitle(text = stringResource(string.feature_settings_dynamic_color_preference))
            Column(Modifier.selectableGroup()) {
                SettingsDialogThemeChooserRow(
                    text = stringResource(string.feature_settings_dynamic_color_yes),
                    selected = settings.useDynamicColor,
                    onClick = { onChangeDynamicColorPreference(true) },
                )
                SettingsDialogThemeChooserRow(
                    text = stringResource(string.feature_settings_dynamic_color_no),
                    selected = !settings.useDynamicColor,
                    onClick = { onChangeDynamicColorPreference(false) },
                )
            }
        }
    }
    SettingsDialogSectionTitle(text = stringResource(string.feature_settings_dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_system_default),
            selected = settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_light),
            selected = settings.darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_dark),
            selected = settings.darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun SettingsDialogNotificationSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
//            .toggleable(
//                checked = checked,
//                role = Role.Switch,
//                onCheckedChange = onCheckedChange,
//            )
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            Modifier.scale(0.8f).testTag(text),
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SocialMediaPanel() {
    SettingsDialogSectionTitle(text = stringResource(string.feature_settings_social_media_panel))
    Spacer(modifier = Modifier.height(16.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(
            space = 32.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val uriHandler = LocalUriHandler.current
        NtIconButton(
            onClick = { uriHandler.openUri(INSTAGRAM_URL) },
            drawable = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_instagram,
            modifier = Modifier
                .width(24.dp)
                .height(24.dp),
            contentDescription = stringResource(id = string.feature_settings_instagram_content_description)
        )
        NtIconButton(
            onClick = { uriHandler.openUri(X_URL) },
            drawable = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_x,
            modifier = Modifier
                .width(24.dp)
                .height(24.dp),
            contentDescription = stringResource(id = string.feature_settings_x_content_description)
        )
        NtIconButton(
            onClick = { uriHandler.openUri(FACEBOOK_URL) },
            drawable = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_facebook,
            modifier = Modifier
                .width(24.dp)
                .height(24.dp),
            contentDescription = stringResource(id = string.feature_settings_facebook_content_description)
        )
        NtIconButton(
            onClick = { uriHandler.openUri(WWW_URL) },
            drawable = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_www,
            modifier = Modifier
                .width(24.dp)
                .height(24.dp),
            contentDescription = stringResource(id = string.feature_settings_www_content_description)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinksPanel() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        val uriHandler = LocalUriHandler.current
        NtTextButton(
            onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
        ) {
            Text(text = stringResource(string.feature_settings_privacy_policy))
        }
        val context = LocalContext.current
        NtTextButton(
            onClick = {
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            },
        ) {
            Text(text = stringResource(string.feature_settings_licenses))
        }
    }
}

@Preview
@Composable
private fun PreviewSettingsDialog() {
    NtTheme {
        SettingsDialog(
            onDismiss = {},
            settingsUiState = SettingsUiState.Success(
                UserEditableSettings(
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    useDynamicColor = false,
                    isNewsNotificationsAllowed = true,
                    isTransfersNotificationsAllowed = true,
                    isGeneralNotificationsAllowed = true
                ),
            ),
            onChangeDynamicColorPreference = {},
            onChangeDarkThemeConfig = {},
            onChangeNewsNotificationsPreference = {},
            onChangeTransfersNotificationsPreference = {},
            onChangeGeneralNotificationsPreference = {}
        )
    }
}

@Preview
@Composable
private fun PreviewSettingsDialogLoading() {
    NtTheme {
        SettingsDialog(
            onDismiss = {},
            settingsUiState = SettingsUiState.Loading,
            onChangeDynamicColorPreference = {},
            onChangeDarkThemeConfig = {},
            onChangeNewsNotificationsPreference = {},
            onChangeTransfersNotificationsPreference = {},
            onChangeGeneralNotificationsPreference = {}
        )
    }
}

private const val PRIVACY_POLICY_URL = "${BuildConfig.BASE_URL}polityka-prywatnosci-aplikacja"
private const val INSTAGRAM_URL = "https://www.instagram.com/nowinki_transferowe/"
private const val FACEBOOK_URL = "https://www.facebook.com/nowinkitransferowe"
private const val X_URL = "https://x.com/Nowinkitransfer"
private const val WWW_URL = BuildConfig.BASE_URL