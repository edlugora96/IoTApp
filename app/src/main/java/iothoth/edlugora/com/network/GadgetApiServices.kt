package iothoth.edlugora.com.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iothoth.edlugora.com.network.model.RequestApi
import iothoth.edlugora.com.network.model.ResponseApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private fun retrofit(baseUrl:String) = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(baseUrl)
    .build()

interface GadgetService {
    @GET("/")
    suspend fun testConnection() : ResponseApi
    @POST("/action")
    suspend fun doAction(@Body body : RequestApi) : ResponseApi
}

class GadgetApi(url: String) {
    val retrofitService : GadgetService by lazy {
        retrofit(url).create(GadgetService::class.java)
    }
}