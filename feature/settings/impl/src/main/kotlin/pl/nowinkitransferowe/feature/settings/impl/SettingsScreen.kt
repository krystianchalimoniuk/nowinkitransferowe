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

package pl.nowinkitransferowe.feature.settings.impl

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import pl.nowinkitransferowe.core.designsystem.component.NtConnectedButtonGroup
import pl.nowinkitransferowe.core.designsystem.component.NtIconButton
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtSectionTitle
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.designsystem.theme.supportsDynamicTheming
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.network.BuildConfig
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.feature.settings.impl.R.string
import pl.nowinkitransferowe.core.designsystem.R as DesignSystemR

@Composable
internal fun SettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Notifications can be turned off in the system settings while the screen is in the background
    var notificationsEnabled by remember { mutableStateOf(areNotificationsEnabled(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsEnabled = areNotificationsEnabled(context)
    }
    SettingsScreen(
        settingsUiState = settingsUiState,
        notificationsEnabled = notificationsEnabled,
        onBackClick = onBackClick,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
        onChangeNewsNotificationsPreference = viewModel::updateNewsNotificationsPreference,
        onChangeTransfersNotificationsPreference = viewModel::updateTransfersNotificationsPreference,
        onChangeGeneralNotificationsPreference = viewModel::updateGeneralNotificationsPreference,
        onOpenSystemNotificationSettings = { context.startActivity(systemNotificationSettingsIntent(context)) },
        onOpenLicenses = { context.startActivity(Intent(context, OssLicensesMenuActivity::class.java)) },
        appVersion = remember { appVersion(context) },
        modifier = modifier,
    )
}

/**
 * The settings as a full screen, grouped like the Android settings: appearance, notifications and
 * information about the app.
 */
@Composable
internal fun SettingsScreen(
    settingsUiState: SettingsUiState,
    notificationsEnabled: Boolean,
    onBackClick: () -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    onChangeNewsNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onChangeTransfersNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onChangeGeneralNotificationsPreference: (isAllowed: Boolean) -> Unit,
    onOpenSystemNotificationSettings: () -> Unit,
    onOpenLicenses: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .testTag("settings"),
        ) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(NtIcons.ArrowBack, contentDescription = stringResource(string.feature_settings_impl_navigate_up))
                }
                NtSectionTitle(stringResource(string.feature_settings_impl_title), Modifier.padding(start = 4.dp))
            }
            when (settingsUiState) {
                SettingsUiState.Loading -> NtLoadingWheel(
                    contentDesc = stringResource(string.feature_settings_impl_loading),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp),
                )

                is SettingsUiState.Success -> {
                    val settings = settingsUiState.settings
                    AppearanceGroup(settings, supportDynamicColor, onChangeDarkThemeConfig, onChangeDynamicColorPreference)
                    NotificationsGroup(
                        settings = settings,
                        notificationsEnabled = notificationsEnabled,
                        onChangeNewsNotificationsPreference = onChangeNewsNotificationsPreference,
                        onChangeTransfersNotificationsPreference = onChangeTransfersNotificationsPreference,
                        onChangeGeneralNotificationsPreference = onChangeGeneralNotificationsPreference,
                        onOpenSystemNotificationSettings = onOpenSystemNotificationSettings,
                    )
                }
            }
            InformationGroup(appVersion, onOpenLicenses)
            SocialMediaRow()
        }
    }
    TrackScreenViewEvent(screenName = "Settings")
}

@Composable
private fun AppearanceGroup(
    settings: UserEditableSettings,
    supportDynamicColor: Boolean,
    onChangeDarkThemeConfig: (DarkThemeConfig) -> Unit,
    onChangeDynamicColorPreference: (Boolean) -> Unit,
) {
    SettingsGroup(stringResource(string.feature_settings_impl_appearance)) {
        val themes = DarkThemeConfig.entries
        SettingsItem(
            icon = NtIcons.DarkMode,
            title = stringResource(string.feature_settings_impl_theme),
            below = {
                NtConnectedButtonGroup(
                    options = themes.map { stringResource(it.label) },
                    selectedIndex = themes.indexOf(settings.darkThemeConfig),
                    onSelect = { onChangeDarkThemeConfig(themes[it]) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
        if (supportDynamicColor) {
            SwitchItem(
                icon = NtIcons.Palette,
                title = stringResource(string.feature_settings_impl_dynamic_color),
                summary = stringResource(string.feature_settings_impl_dynamic_color_summary),
                checked = settings.useDynamicColor,
                onCheckedChange = onChangeDynamicColorPreference,
            )
        }
    }
}

@Composable
private fun NotificationsGroup(
    settings: UserEditableSettings,
    notificationsEnabled: Boolean,
    onChangeNewsNotificationsPreference: (Boolean) -> Unit,
    onChangeTransfersNotificationsPreference: (Boolean) -> Unit,
    onChangeGeneralNotificationsPreference: (Boolean) -> Unit,
    onOpenSystemNotificationSettings: () -> Unit,
) {
    SettingsGroup(stringResource(string.feature_settings_impl_notification_preference)) {
        if (!notificationsEnabled) {
            SettingsItem(
                icon = NtIcons.NotificationsOff,
                title = stringResource(string.feature_settings_impl_notifications_off),
                summary = stringResource(string.feature_settings_impl_notifications_off_summary),
                onClick = onOpenSystemNotificationSettings,
            ) {
                Icon(NtIcons.OpenInNew, contentDescription = null)
            }
        } else {
            SwitchItem(
                icon = NtIcons.Rumours,
                title = stringResource(string.feature_settings_impl_notification_channel_general),
                summary = stringResource(string.feature_settings_impl_notification_channel_general_summary),
                checked = settings.isGeneralNotificationsAllowed,
                onCheckedChange = onChangeGeneralNotificationsPreference,
            )
            SwitchItem(
                icon = NtIcons.Article,
                title = stringResource(string.feature_settings_impl_notification_channel_news),
                summary = stringResource(string.feature_settings_impl_notification_channel_news_summary),
                checked = settings.isNewsNotificationsAllowed,
                onCheckedChange = onChangeNewsNotificationsPreference,
            )
            SwitchItem(
                icon = NtIcons.Transfers,
                title = stringResource(string.feature_settings_impl_notification_channel_transfers),
                summary = stringResource(string.feature_settings_impl_notification_channel_transfers_summary),
                checked = settings.isTransfersNotificationsAllowed,
                onCheckedChange = onChangeTransfersNotificationsPreference,
            )
        }
    }
}

@Composable
private fun InformationGroup(appVersion: String, onOpenLicenses: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    SettingsGroup(stringResource(string.feature_settings_impl_information)) {
        SettingsItem(
            icon = NtIcons.Language,
            title = stringResource(string.feature_settings_impl_website),
            onClick = { uriHandler.openUri(WWW_URL) },
        ) { Icon(NtIcons.OpenInNew, contentDescription = null) }
        SettingsItem(
            icon = NtIcons.PrivacyTip,
            title = stringResource(string.feature_settings_impl_privacy_policy),
            onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
        ) { Icon(NtIcons.OpenInNew, contentDescription = null) }
        SettingsItem(
            icon = NtIcons.Info,
            title = stringResource(string.feature_settings_impl_version, appVersion),
            summary = stringResource(string.feature_settings_impl_licenses),
            onClick = onOpenLicenses,
        ) { Icon(NtIcons.ChevronRight, contentDescription = null) }
    }
}

@Composable
private fun SocialMediaRow() {
    val uriHandler = LocalUriHandler.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            Triple(X_URL, DesignSystemR.drawable.core_designsystem_ic_x, string.feature_settings_impl_x_content_description),
            Triple(FACEBOOK_URL, DesignSystemR.drawable.core_designsystem_ic_facebook, string.feature_settings_impl_facebook_content_description),
            Triple(INSTAGRAM_URL, DesignSystemR.drawable.core_designsystem_ic_instagram, string.feature_settings_impl_instagram_content_description),
        ).forEach { (url, icon, description) ->
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                NtIconButton(
                    onClick = { uriHandler.openUri(url) },
                    drawable = icon,
                    modifier = Modifier.size(24.dp),
                    contentDescription = stringResource(description),
                )
            }
        }
    }
}

/** A titled group of items with the shared rounded corners of the Android 16 settings. */
@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 16.dp, bottom = 8.dp),
    )
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content,
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    below: (@Composable () -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                summary?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing()
        }
        below?.invoke()
    }
}

@Composable
private fun SwitchItem(
    icon: ImageVector,
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsItem(
        icon = icon,
        title = title,
        summary = summary,
        modifier = Modifier
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .testTag(title),
    ) {
        Switch(checked = checked, onCheckedChange = null)
    }
}

private val DarkThemeConfig.label: Int
    get() = when (this) {
        DarkThemeConfig.FOLLOW_SYSTEM -> string.feature_settings_impl_dark_mode_config_system_default
        DarkThemeConfig.LIGHT -> string.feature_settings_impl_dark_mode_config_light
        DarkThemeConfig.DARK -> string.feature_settings_impl_dark_mode_config_dark
    }

internal fun areNotificationsEnabled(context: Context): Boolean {
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
}

private fun systemNotificationSettingsIntent(context: Context): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
} else {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
}

private fun appVersion(context: Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
}.getOrNull().orEmpty()

@Preview
@Composable
private fun SettingsScreenPreview() {
    NtTheme {
        SettingsScreen(
            settingsUiState = SettingsUiState.Success(
                UserEditableSettings(
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    useDynamicColor = false,
                    isNewsNotificationsAllowed = true,
                    isTransfersNotificationsAllowed = false,
                    isGeneralNotificationsAllowed = true,
                ),
            ),
            notificationsEnabled = true,
            onBackClick = {},
            onChangeDynamicColorPreference = {},
            onChangeDarkThemeConfig = {},
            onChangeNewsNotificationsPreference = {},
            onChangeTransfersNotificationsPreference = {},
            onChangeGeneralNotificationsPreference = {},
            onOpenSystemNotificationSettings = {},
            onOpenLicenses = {},
            appVersion = "4.0.0",
            supportDynamicColor = true,
        )
    }
}

/** The channel of the app's notifications, created by the notifier in core:notifications. */
private const val NOTIFICATION_CHANNEL_ID = "update_channel"
private const val PRIVACY_POLICY_URL = "${BuildConfig.BASE_URL}polityka-prywatnosci-aplikacja"
private const val INSTAGRAM_URL = "https://www.instagram.com/nowinki_transferowe/"
private const val FACEBOOK_URL = "https://www.facebook.com/nowinkitransferowe"
private const val X_URL = "https://x.com/Nowinkitransfer"
private const val WWW_URL = BuildConfig.BASE_URL
