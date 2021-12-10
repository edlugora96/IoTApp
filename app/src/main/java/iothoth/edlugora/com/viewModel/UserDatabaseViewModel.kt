package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.*
import iothoth.edlugora.com.R
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel.UiReactions.*
import iothoth.edlugora.domain.*
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.*

class UserDatabaseViewModel(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
    private val setUserInfo: SetUserInfo,
    private val insertGadget: InsertGadget,
    private val updateGadget: UpdateGadget,
    private val updateUserInfo: UpdateUserInfo,
    private val getAllGadgets: GetAllGadgets,
    private val getUserInfo: GetUserInfo,
    private val getGadget: GetGadget
) : ViewModel() {
    //region Utils declarations
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events

    sealed class UiReactions {
        object NavToProfile : UiReactions()
        object NavToControl : UiReactions()
        data class ShowSuccessSnackBar(val message: String) : UiReactions()
        data class ShowErrorSnackBar(val message: String) : UiReactions()
        data class ShowWarningSnackBar(val message: String) : UiReactions()
        data class ShowToast(val message: Int) : UiReactions()
        data class IdInsertedGadget(val id: Long) : UiReactions()
    }
    //endregion

    //region Get user and gadgets information
    val getGadgets: LiveData<List<iothoth.edlugora.domain.Gadget>> = getAllGadgets.invoke().asLiveData()
    fun gadget(id: Int): LiveData<iothoth.edlugora.domain.Gadget> = getGadget.invoke(id).asLiveData()

    fun getUser(activity: Activity): LiveData<iothoth.edlugora.domain.User?> = MutableLiveData(getUserInfo.invoke(activity))

    fun checkFirstStep(activity: Activity) {
        if (getUser(activity).value?.firstStep == true) {
            _events.value = Event(NavToProfile)
        } else {
            _events.value = Event(NavToControl)
        }
    }
    //endregion

    //region Control handler
    suspend fun testConnection(baseUrl: String, url: String) {
        testGadgetConnection.invoke(baseUrl, url)

    }

    suspend fun gadgetDoAction(baseUrl: String, url: String, data: iothoth.edlugora.domain.RequestApi) {
        triggerGadgetAction.invoke(baseUrl, url, data)

    }
    //endregion

    //region Insert or update gadgets and user
    private fun setUserInfo(activity: Activity, data: User): Job {
        return viewModelScope.launch { setUserInfo.invoke(activity, data) }
    }

    private suspend fun insertGadget(gadget: Gadget): Job {
        return viewModelScope.launch {
            _events.value = Event(IdInsertedGadget(insertGadget.invoke(gadget)))
        }
    }

    private suspend fun updateGadget(gadget: Gadget): Job {
        return viewModelScope.launch { updateGadget.invoke(gadget) }

    }

    private fun updateUserInfo(activity: Activity, user: UpdateUser): Job {
        return viewModelScope.launch { updateUserInfo.invoke(activity, user) }
    }

    suspend fun sendForm(activity: Activity, user: User, gadget: Gadget) {
        _loading.value = true
        try {
            if (!isEntryValid(gadget.name, user.name, gadget.ipAddress)) {
                _events.value = Event(ShowToast(R.string.error_message))
                _loading.value = false
                return
            }

            if (user.name.isNotEmpty() && gadget.id != 0) {
                updateUserInfo(activity, user.toUpdateUser()).join()
                updateUserInfo(activity, user.toUpdateUser()).join()
                updateGadget(gadget).join()
                _events.value = Event(NavToControl)
                _loading.value = false
                return
            }

            setUserInfo(activity, user).join()
            insertGadget(gadget).join()
            _events.value = Event(NavToControl)
            _loading.value = false
        } catch (ex: Exception) {
            _events.value = Event(ShowToast(R.string.error_message))
            _loading.value = false
        }

    }
    //endregion

    //region Others methods
    private fun isEntryValid(gadgetName: String, userName: String, ipAddress: String): Boolean {
        if (gadgetName.isBlank() || userName.isBlank() || ipAddress.isBlank()) {
            return false
        }
        return true
    }
    //endregion
}

//region Factory
class UserDatabaseViewModelFactory(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
    private val setUserInfo: SetUserInfo,
    private val insertGadget: InsertGadget,
    private val updateGadget: UpdateGadget,
    private val updateUserInfo: UpdateUserInfo,
    private val getAllGadgets: GetAllGadgets,
    private val getUserInfo: GetUserInfo,
    private val getGadget: GetGadget
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDatabaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDatabaseViewModel(
                triggerGadgetAction,
                testGadgetConnection,
                setUserInfo,
                insertGadget,
                updateGadget,
                updateUserInfo,
                getAllGadgets,
                getUserInfo,
                getGadget
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//endregion