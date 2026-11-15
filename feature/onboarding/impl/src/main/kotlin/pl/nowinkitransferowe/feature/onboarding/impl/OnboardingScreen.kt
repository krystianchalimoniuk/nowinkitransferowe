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

package pl.nowinkitransferowe.feature.onboarding.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtButton
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtSectionTitle
import pl.nowinkitransferowe.core.designsystem.component.NtTextButton
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent

/** "Wybierz swoje kluby", shown once before the app; see [OnboardingViewModel]. */
@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingScreen(
        uiState = uiState,
        onLeagueSelected = viewModel::selectLeague,
        onClubToggled = viewModel::togglePicked,
        onDone = { viewModel.finish() },
        onSkip = { viewModel.finish(skip = true) },
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingScreen(
    uiState: OnboardingUiState,
    onLeagueSelected: (String) -> Unit,
    onClubToggled: (Int) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 840.dp).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NtSectionTitle(stringResource(R.string.feature_onboarding_impl_title))
                Text(
                    stringResource(R.string.feature_onboarding_impl_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when (uiState) {
                OnboardingUiState.Loading -> Column(
                    Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    NtLoadingWheel(contentDesc = stringResource(R.string.feature_onboarding_impl_loading))
                    Text(
                        stringResource(R.string.feature_onboarding_impl_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                is OnboardingUiState.Success -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.leagues, key = { it.slug }) { league ->
                            val count = uiState.clubsPickedIn(league.slug)
                            FilterChip(
                                selected = league.slug == uiState.league,
                                onClick = { onLeagueSelected(league.slug) },
                                label = { Text(if (count > 0) "${league.name} · $count" else league.name) },
                                modifier = Modifier.testTag("onboarding:league:${league.slug}"),
                            )
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 104.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f).testTag("onboarding:clubs"),
                    ) {
                        items(uiState.clubs, key = { it.id }) { club ->
                            ClubTile(club, picked = club.id in uiState.picked, onToggle = { onClubToggled(club.id) })
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NtTextButton(onClick = onSkip, modifier = Modifier.testTag("onboarding:skip")) {
                    Text(stringResource(R.string.feature_onboarding_impl_skip))
                }
                Spacer(Modifier.weight(1f))
                val picked = (uiState as? OnboardingUiState.Success)?.picked?.size ?: 0
                NtButton(onClick = onDone, enabled = picked > 0, modifier = Modifier.testTag("onboarding:done")) {
                    Text(
                        if (picked > 0) {
                            stringResource(R.string.feature_onboarding_impl_follow, picked)
                        } else {
                            stringResource(R.string.feature_onboarding_impl_pick)
                        },
                    )
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Onboarding")
}

private fun OnboardingUiState.Success.clubsPickedIn(league: String) = clubs.count { it.id in picked && it.topic == league }

/** A club to pick: its crest and name, outlined in the primary colour with a check when picked. */
@Composable
private fun ClubTile(club: Club, picked: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (picked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.cardContainer,
        border = if (picked) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .toggleable(value = picked, role = Role.Checkbox, onValueChange = { onToggle() })
            .testTag("onboarding:club:${club.id}"),
    ) {
        Box {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NtCrest(imageUrl = club.crestUrl, name = club.name, size = 56.dp)
                Text(
                    club.name,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (picked) {
                Icon(
                    NtIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp),
                )
            }
        }
    }
}
