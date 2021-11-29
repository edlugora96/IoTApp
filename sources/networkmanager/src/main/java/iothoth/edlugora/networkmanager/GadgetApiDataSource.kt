package iothoth.edlugora.networkmanager

import android.util.Log
import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.domain.ResponseApi
import iothoth.edlugora.domain.repository.RemoteGadgetDataSource
import retrofit2.awaitResponse
import java.lang.Exception
import java.net.SocketTimeoutException

class GadgetApiDataSource(
    private val gadgetRequest: GadgetRequest
) : RemoteGadgetDataSource {

    override suspend fun testGadgetConnection(baseUrl: String, url: String): ResponseApi {
        try {
            val res = gadgetRequest.api(baseUrl).testGadgetConnection(url)

            return ResponseApi(
                data = res.data,
                error = res.error,
                code = res.code.toString()

            )
        } catch (ex: Exception) {
            return ResponseApi(
                data = null,
                error = "Device offline",
                code = "500"
            )
        }
    }

    override suspend fun triggerGadgetAction(
        baseUrl: String,
        url: String,
        data: RequestApi
    ): ResponseApi {
        try {
            val res = gadgetRequest.api(baseUrl).triggerGadgetAction(url, data)

            return ResponseApi(
                data = res.data,
                error = res.error,
                code = res.code.toString()

            )
        } catch (ex: Exception) {
            return ResponseApi(
                data = null,
                error = "Device offline",
                code = "500"
            )
        }
    }

}