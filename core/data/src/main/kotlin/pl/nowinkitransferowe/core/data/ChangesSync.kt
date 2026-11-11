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

package pl.nowinkitransferowe.core.data

import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.network.MAX_IDS_PER_REQUEST
import pl.nowinkitransferowe.core.network.model.NetworkChange
import pl.nowinkitransferowe.core.network.model.NetworkChanges

/**
 * Syncs one API v3 record type with its change list.
 *
 * 1. Reads every page of changes after the local version ([changesFetcher] is called until
 *    `hasMore` is false). When an id changed twice while paging, only its newest change counts.
 * 2. Deletes the records the server deleted ([modelDeleter]).
 * 3. Saves the changed records in batches of [batchSize] ids, newest ids first ([modelUpdater]).
 * 4. Saves the new version only after the last batch, so an interrupted sync starts again from the
 *    same place and nothing is lost.
 *
 * Returns whether the sync succeeded. The blocks are never run concurrently, and the
 * [Synchronizer] implementation must guarantee this.
 */
suspend fun Synchronizer.syncChanges(
    versionReader: (ChangeListVersions) -> Int,
    changesFetcher: suspend (after: Int) -> NetworkChanges,
    versionUpdater: ChangeListVersions.(Int) -> ChangeListVersions,
    modelDeleter: suspend (List<Int>) -> Unit,
    modelUpdater: suspend (List<Int>) -> Unit,
    batchSize: Int = MAX_IDS_PER_REQUEST,
): Boolean = suspendRunCatching {
    val currentVersion = versionReader(getChangeListVersions())

    val newestChanges = mutableMapOf<Int, NetworkChange>()
    var after = currentVersion
    var latestVersion = currentVersion
    do {
        val page = changesFetcher(after)
        page.changes.forEach { change ->
            val known = newestChanges[change.id]
            if (known == null || change.version > known.version) newestChanges[change.id] = change
        }
        val pageVersion = page.changes.maxOfOrNull(NetworkChange::version) ?: after
        latestVersion = maxOf(latestVersion, pageVersion, if (page.hasMore) 0 else page.latestVersion)
        val hasNextPage = page.hasMore && pageVersion > after
        after = pageVersion
    } while (hasNextPage)

    val (deleted, changed) = newestChanges.values.partition(NetworkChange::deleted)
    if (deleted.isNotEmpty()) modelDeleter(deleted.map(NetworkChange::id))
    changed.map(NetworkChange::id)
        .sortedDescending()
        .chunked(batchSize)
        .forEach { modelUpdater(it) }

    if (latestVersion != currentVersion) {
        updateChangeListVersions { versionUpdater(latestVersion) }
    }
}.isSuccess
