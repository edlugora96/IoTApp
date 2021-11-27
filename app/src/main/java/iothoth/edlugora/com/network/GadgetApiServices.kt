package iothoth.edlugora.com.network

import iothoth.edlugora.com.domain.RequestApi
import iothoth.edlugora.com.domain.ResponseApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface GadgetService {
    @GET
    suspend fun testGadgetConnection(@Url url: String) : ResponseApi
    @POST
    suspend fun triggerGadgetAction(@Url url: String, @Body body : RequestApi) : ResponseApi
}
