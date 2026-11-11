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

package pl.nowinkitransferowe.sync.synctest

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.sync.di.SyncModule
import pl.nowinkitransferowe.sync.work.status.StubSubscriber
import pl.nowinkitransferowe.sync.work.status.Subscriber

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SyncModule::class],
)
internal interface TestSyncModule {
    @Binds
    fun bindsSyncStatusMonitor(
        syncStatusMonitor: NeverSyncingSyncManager,
    ): SyncManager

    @Binds
    fun bindsSyncSubscriber(
        subscriber: StubSubscriber,
    ): Subscriber
}
