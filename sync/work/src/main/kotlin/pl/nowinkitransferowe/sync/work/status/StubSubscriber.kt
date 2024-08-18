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

package pl.nowinkitransferowe.sync.work.status

import android.util.Log
import javax.inject.Inject

private const val TAG = "StubSyncSubscriber"

/**
 * Stub implementation of [Subscriber]
 */
class StubSubscriber @Inject constructor() : Subscriber {
    override suspend fun subscribeToSync() {
        Log.d(TAG, "Subscribing to sync")
    }

    override suspend fun subscribeToGeneral() {
        Log.d(TAG, "Subscribing to general")
    }
}
