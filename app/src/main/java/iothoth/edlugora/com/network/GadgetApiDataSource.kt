package iothoth.edlugora.com.network

import iothoth.edlugora.com.domain.RequestApi
import iothoth.edlugora.com.domain.ResponseApi
import iothoth.edlugora.com.repositories.RemoteGadgetDataSource

class GadgetApiDataSource(
    private val gadgetRequest: GadgetRequest
): RemoteGadgetDataSource {

    override suspend fun testGadgetConnection(baseUrl:String, url: String): ResponseApi =
        gadgetRequest.api(baseUrl).testGadgetConnection(url)

    override suspend fun triggerGadgetAction(baseUrl:String, url: String, data: RequestApi): ResponseApi =
        gadgetRequest.api(baseUrl).triggerGadgetAction(url, data)
}