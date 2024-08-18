/*
 * Copyright 2024 The Android Open Source Project
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

package pl.nowinkitransferowe.core.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for informing about the performance of operations on the room database
 */
interface DatabaseUpdatingMonitor {
    val newsDataChanged: Flow<Boolean>
    val transferDataChanged: Flow<Boolean>
    suspend fun notifyNewsDataChanged()
    suspend fun notifyTransfersDataChanged()
}

@Singleton
class DefaultDatabaseUpdatingMonitor @Inject constructor(
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : DatabaseUpdatingMonitor {
    private val _newsDataChanged: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val newsDataChanged: Flow<Boolean>
        get() = _newsDataChanged

    private val _transferDataChanged: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val transferDataChanged: Flow<Boolean>
        get() = _transferDataChanged

    override suspend fun notifyNewsDataChanged() {
        withContext(ioDispatcher) {
            _newsDataChanged.value = true
            delay(2000)
            _newsDataChanged.value = false
        }
    }

    override suspend fun notifyTransfersDataChanged() {
        withContext(ioDispatcher) {
            _transferDataChanged.value = true
            delay(2000)
            _transferDataChanged.value = false
        }
    }
}
