package dev.toothlonely.notesapp.core.data.network.gigachat

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface GigaChatApi {
    @Headers("Accept: application/json")
    @GET("balance")
    suspend fun getBalance(
        @Header("Authorization") authorization: String,
    ): Response<ResponseBody>

    @Headers("Accept: application/json")
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: RequestBody,
    ): Response<ResponseBody>
}

internal interface GigaChatAuthApi {
    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("api/v2/oauth")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Header("RqUID") requestId: String,
        @Field("scope") scope: String,
    ): Response<ResponseBody>
}
