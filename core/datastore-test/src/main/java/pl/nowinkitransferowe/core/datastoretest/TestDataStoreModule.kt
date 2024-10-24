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

package pl.nowinkitransferowe.core.datastoretest

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import org.junit.rules.TemporaryFolder
import pl.nowinkitransferowe.core.common.network.di.ApplicationScope
import pl.nowinkitransferowe.core.datastore.UserPreferencesSerializer
import pl.nowinkitransferowe.core.datastore.di.DataStoreModule
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class],
)
internal object TestDataStoreModule {
    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(
        serializer: UserPreferencesSerializer,
    ): DataStore<UserPreferences> = InMemoryDataStore(serializer.defaultValue)
}
