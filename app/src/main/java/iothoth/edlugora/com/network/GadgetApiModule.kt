package iothoth.edlugora.com.network

import dagger.hilt.InstallIn
import iothoth.edlugora.com.domain.RequestApi
import iothoth.edlugora.com.domain.ResponseApi
import iothoth.edlugora.com.repositories.RemoteGadgetDataSource
import retrofit2.http.Body
import retrofit2.http.Url

/*

@InstallIn
class GadgetApiModule {
    suspend fun testGadgetConnectionProvider(gadgetRequest: GadgetRequest): RemoteGadgetDataSource =
        GadgetApiDataSource(gadgetRequest)

    suspend fun triggerGadgetActionProvider(gadgetRequest: GadgetRequest
    ): RemoteGadgetDataSource = GadgetApiDataSource(gadgetRequest)
}
*/
