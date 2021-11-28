package iothoth.edlugora.networkmanager

import iothoth.edlugora.domain.RequestApi
import iothoth.edlugora.domain.ResponseApi
import iothoth.edlugora.domain.repository.RemoteGadgetDataSource

class GadgetApiDataSource(
    private val gadgetRequest: GadgetRequest
): RemoteGadgetDataSource {

    override suspend fun testGadgetConnection(baseUrl:String, url: String): ResponseApi =
        gadgetRequest.api(baseUrl).testGadgetConnection(url)

    override suspend fun triggerGadgetAction(baseUrl:String, url: String, data: RequestApi): ResponseApi =
        gadgetRequest.api(baseUrl).triggerGadgetAction(url, data)
}