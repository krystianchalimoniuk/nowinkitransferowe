/*
 * Copyright 2023 The Android Open Source Project
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

package pl.nowinkitransferowe.sync.status

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import pl.nowinkitransferowe.sync.work.initializers.GENERAL_TOPIC
import pl.nowinkitransferowe.sync.work.initializers.SYNC_TOPIC
import pl.nowinkitransferowe.sync.work.status.Subscriber
import javax.inject.Inject

/**
 * Implementation of [Subscriber] that subscribes to the FCM [SYNC_TOPIC] and [GENERAL_TOPIC]
 */
internal class FirebaseSubscriber @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
) : Subscriber {
    override suspend fun subscribeToSync() {
        firebaseMessaging
            .subscribeToTopic(SYNC_TOPIC)
            .await()
    }

    override suspend fun subscribeToGeneral() {
        firebaseMessaging
            .subscribeToTopic(GENERAL_TOPIC)
            .await()
    }
}
