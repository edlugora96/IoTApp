package iothoth.edlugora.usecases

import android.app.Activity
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import kotlinx.coroutines.flow.Flow

//region User Indo
class GetUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity): iothoth.edlugora.domain.User? = userInfoRepository.getUserInfo(activity)
}

class SetUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity, data: iothoth.edlugora.domain.User?): Boolean =
        userInfoRepository.setUserInfo(activity, data)
}

class UpdateUserInfo(
    private val userInfoRepository: UserInfoRepository
) {
    fun invoke(activity: Activity, data: UpdateUser?): Boolean =
        userInfoRepository.updateUserInfo(activity, data)
}
//endregion

//region Gadgets
class GetGadget(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(id: Int): Flow<iothoth.edlugora.domain.Gadget> = gadgetRepository.getGadget(id)
}

class GetAllGadgets(
    private val gadgetRepository: GadgetRepository
) {
    fun invoke(): Flow<List<iothoth.edlugora.domain.Gadget>> = gadgetRepository.getAllGadgets()
}

class TriggerGadgetAction(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(baseUrl: String, url: String, data: iothoth.edlugora.domain.RequestApi): iothoth.edlugora.domain.ResponseApi =
        gadgetRepository.triggerGadgetAction(baseUrl, url, data)
}

class TestGadgetConnection(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(baseUrl: String, url: String): iothoth.edlugora.domain.ResponseApi =
        gadgetRepository.testGadgetConnection(baseUrl, url)
}

class InsertGadget(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(gadget: iothoth.edlugora.domain.Gadget) : Long = gadgetRepository.insertGadget(gadget)
}

class UpdateGadget(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(gadget: iothoth.edlugora.domain.Gadget) = gadgetRepository.updateGadget(gadget)
}

class DeleteGadget(
    private val gadgetRepository: GadgetRepository
) {
    suspend fun invoke(gadget: iothoth.edlugora.domain.Gadget) = gadgetRepository.deleteGadget(gadget)
}
//endregion