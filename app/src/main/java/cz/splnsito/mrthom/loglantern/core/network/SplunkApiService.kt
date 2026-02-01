package cz.splnsito.mrthom.loglantern.core.network

import cz.splnsito.mrthom.loglantern.feature.auth.model.SplunkTokenJsonResponse
import cz.splnsito.mrthom.loglantern.feature.search.model.SplunkJobStatusResponse
import cz.splnsito.mrthom.loglantern.feature.search.model.SplunkSearchJobResponse
import cz.splnsito.mrthom.loglantern.feature.search.model.SplunkSearchResultResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SplunkApiService {
    // ============ Authentication ============

    @FormUrlEncoded
    @POST("services/authorization/tokens")
    suspend fun createToken(
        @Header("Authorization") authHeader: String,
        @Field("name") username: String,
        @Field("audience") audience: String,
        @Field("type") type: String = "static",
        @Field("output_mode") outputMode: String = "json"
    ): SplunkTokenJsonResponse

    @GET("services/authorization/tokens")
    suspend fun getAuthTokens(
        @Query("username") username: String,
        @Query("output_mode") outputMode: String = "json",
        @Header("Authorization") authHeader: String
    ): SplunkTokenJsonResponse

    // ============ Search Jobs ============

    @FormUrlEncoded
    @POST("services/search/jobs")
    suspend fun createSearchJob(
        @Header("Authorization") token: String,
        @Field("search") searchQuery: String,
        @Field("output_mode") outputMode: String = "json"
    ): SplunkSearchJobResponse

    @GET("services/search/jobs/{sid}")
    suspend fun getJobStatus(
        @Header("Authorization") token: String,
        @Path("sid") sid: String,
        @Query("output_mode") outputMode: String = "json"
    ): SplunkJobStatusResponse

    @GET("services/search/jobs/{sid}/results")
    suspend fun getResults(
        @Header("Authorization") token: String,
        @Path("sid") sid: String,
        @Query("output_mode") outputMode: String = "json",
        @Query("count") count: Int = 0
    ): SplunkSearchResultResponse
}
