package pl.nowinkitransferowe.core.network.retrofit

import androidx.tracing.trace
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import pl.nowinkitransferowe.core.network.BuildConfig
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.demo.mapToChangeList
import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit API declaration for NT Network API
 */
private interface RetrofitNtNetworkApi {
    @GET(value = "transfers")
    suspend fun getTransfers(
        @Query("id") ids: String?,
    ): List<NetworkTransferResource>

    @GET(value = "news")
    suspend fun getNews(
        @Query("id") ids: String?,
    ): List<NetworkNewsResource>

    @GET(value = "transfers-network-change-list")
    suspend fun getTransfersChangeList(
        @Query("after") after: Int?,
    ): List<NetworkChangeList>

    @GET(value = "news-network-change-list")
    suspend fun getNewsResourcesChangeList(
        @Query("after") after: Int?,
    ): List<NetworkChangeList>
}

private const val NT_BASE_URL = BuildConfig.BACKEND_URL


/**
 * Wrapper for data provided from the [NT_BASE_URL]
 */
@Serializable
private data class NetworkResponse<T>(
    val data: T,
)

/**
 * [Retrofit] backed [NtNetworkDataSource]
 */
@Singleton
class RetrofitNtNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : NtNetworkDataSource {

    private val networkApi = trace("RetrofitNtNetwork") {
        Retrofit.Builder()
            .baseUrl(NT_BASE_URL)
            // We use callFactory lambda here with dagger.Lazy<Call.Factory>
            // to prevent initializing OkHttp on the main thread.
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(RetrofitNtNetworkApi::class.java)
    }

    override suspend fun getTransferResources(ids: String?): List<NetworkTransferResource> =
        networkApi.getTransfers(ids = ids)

    override suspend fun getNewsResources(ids: String?): List<NetworkNewsResource> =
        networkApi.getNews(ids = ids)

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        networkApi.getNewsResourcesChangeList(after = after)

    override suspend fun getTransferResourceChangeList(after: Int?): List<NetworkChangeList> =
        networkApi.getTransfersChangeList(after = after)


}
