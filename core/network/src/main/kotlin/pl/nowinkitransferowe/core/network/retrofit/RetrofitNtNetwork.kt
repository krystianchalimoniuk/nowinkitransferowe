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

package pl.nowinkitransferowe.core.network.retrofit

import androidx.tracing.trace
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import pl.nowinkitransferowe.core.network.BuildConfig
import pl.nowinkitransferowe.core.network.MAX_IDS_PER_REQUEST
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkArticle
import pl.nowinkitransferowe.core.network.model.NetworkChanges
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkClubSeasons
import pl.nowinkitransferowe.core.network.model.NetworkCountry
import pl.nowinkitransferowe.core.network.model.NetworkLeagues
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import pl.nowinkitransferowe.core.network.model.NetworkRumour
import pl.nowinkitransferowe.core.network.model.NetworkRumourStats
import pl.nowinkitransferowe.core.network.model.NetworkTopic
import pl.nowinkitransferowe.core.network.model.NetworkTransfer
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit API declaration for API v3 (read only, see docs/api-v3.md in the website repository)
 */
private interface RetrofitNtNetworkApiV3 {
    @GET(value = "changes/{type}")
    suspend fun getChanges(
        @Path("type") type: String,
        @Query("after") after: Int,
        @Query("limit") limit: Int,
    ): NetworkChanges

    @GET(value = "articles")
    suspend fun getArticles(@Query("ids") ids: String): List<NetworkArticle>

    @GET(value = "transfers")
    suspend fun getTransfers(@Query("ids") ids: String): List<NetworkTransfer>

    @GET(value = "clubs")
    suspend fun getClubs(@Query("ids") ids: String): List<NetworkClub>

    @GET(value = "players")
    suspend fun getPlayers(@Query("ids") ids: String): List<NetworkPlayer>

    @GET(value = "rumours")
    suspend fun getRumours(@Query("ids") ids: String): List<NetworkRumour>

    @GET(value = "rumours/stats")
    suspend fun getRumourStats(): NetworkRumourStats

    @GET(value = "clubs/{id}/seasons")
    suspend fun getClubSeasons(@Path("id") clubId: Int): NetworkClubSeasons

    @GET(value = "leagues")
    suspend fun getLeagues(@Query("season") season: String?): NetworkLeagues

    @GET(value = "topics")
    suspend fun getTopics(): List<NetworkTopic>

    @GET(value = "countries")
    suspend fun getCountries(): List<NetworkCountry>
}

private const val NT_API_URL = BuildConfig.API_URL

/**
 * [Retrofit] backed [NtNetworkDataSource]
 */
@Singleton
class RetrofitNtNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : NtNetworkDataSource {

    private val apiV3 = trace("RetrofitNtNetworkV3") {
        Retrofit.Builder()
            .baseUrl(NT_API_URL)
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(RetrofitNtNetworkApiV3::class.java)
    }

    override suspend fun getChanges(type: NetworkRecordType, after: Int, limit: Int): NetworkChanges = apiV3.getChanges(type = type.path, after = after, limit = limit)

    override suspend fun getArticles(ids: List<Int>): List<NetworkArticle> = byIds(ids, apiV3::getArticles)

    override suspend fun getTransfers(ids: List<Int>): List<NetworkTransfer> = byIds(ids, apiV3::getTransfers)

    override suspend fun getClubs(ids: List<Int>): List<NetworkClub> = byIds(ids, apiV3::getClubs)

    override suspend fun getPlayers(ids: List<Int>): List<NetworkPlayer> = byIds(ids, apiV3::getPlayers)

    override suspend fun getRumours(ids: List<Int>): List<NetworkRumour> = byIds(ids, apiV3::getRumours)

    override suspend fun getRumourStats(): NetworkRumourStats = apiV3.getRumourStats()

    override suspend fun getClubSeasons(clubId: Int): NetworkClubSeasons = apiV3.getClubSeasons(clubId = clubId)

    override suspend fun getLeagues(season: String?): NetworkLeagues = apiV3.getLeagues(season = season)

    override suspend fun getTopics(): List<NetworkTopic> = apiV3.getTopics()

    override suspend fun getCountries(): List<NetworkCountry> = apiV3.getCountries()
}

/**
 * Calls a records endpoint with [ids] joined by commas; no ids means no request.
 */
private suspend fun <T> byIds(ids: List<Int>, request: suspend (String) -> List<T>): List<T> {
    require(ids.size <= MAX_IDS_PER_REQUEST) { "API v3 accepts at most $MAX_IDS_PER_REQUEST ids, got ${ids.size}" }
    return if (ids.isEmpty()) emptyList() else request(ids.joinToString(","))
}
