package cz.splnsito.mrthom.loglantern.core.network

import cz.splnsito.mrthom.loglantern.feature.auth.model.SplunkTokenJsonResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SplunkApiService {
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
}
