package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.*
import iothoth.edlugora.com.R
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.com.viewModel.ProfileViewModel.UiReactions
import iothoth.edlugora.com.viewModel.ProfileViewModel.UiReactions.*
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.domain.User
import iothoth.edlugora.domain.toUpdateUser
import iothoth.edlugora.domain.toUser
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val setUserInfo: SetUserInfo,
    private val insertGadget: InsertGadget,
    private val updateGadget: UpdateGadget,
    private val updateUserInfo: UpdateUserInfo,
    private val getUserInfo: GetUserInfo,
    private val getGadget: GetGadget
) : ViewModel() {
    //region Utils declarations
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events

    sealed class UiReactions {
        object NavToControl : UiReactions()
        data class ShowToast(val message: Int) : UiReactions()
        data class IdInsertedGadget(val id: Long) : UiReactions()
    }
    //endregion

    //region Get user and gadgets information
    fun gadget(id: Int): LiveData<iothoth.edlugora.domain.Gadget> = getGadget.invoke(id).asLiveData()

    fun getUser(activity: Activity): LiveData<iothoth.edlugora.domain.User?> = MutableLiveData(getUserInfo.invoke(activity))

    //endregion

    //region Insert or update gadgets and user
    private fun setUserInfo(activity: Activity, data: iothoth.edlugora.domain.User): Job {
        return viewModelScope.launch { setUserInfo.invoke(activity, data) }
    }

    private suspend fun insertGadget(gadget: iothoth.edlugora.domain.Gadget): Job {
        return viewModelScope.launch {
            _events.value = Event(
                IdInsertedGadget(
                    insertGadget.invoke(
                        gadget
                    )
                )
            )
        }
    }

    private suspend fun updateGadget(gadget: iothoth.edlugora.domain.Gadget): Job {
        return viewModelScope.launch { updateGadget.invoke(gadget) }

    }

    private fun updateUserInfo(activity: Activity, user: User): Job {
        return viewModelScope.launch { updateUserInfo.invoke(activity, user.toUpdateUser()) }
    }

    fun updateUserInfo(activity: Activity, user: UpdateUser): Job {
        return viewModelScope.launch { updateUserInfo.invoke(activity, user) }
    }



    suspend fun sendForm(activity: Activity, user: iothoth.edlugora.domain.User, gadget: iothoth.edlugora.domain.Gadget) {
        _loading.value = true
        try {
            if (!isEntryValid(gadget.name, user.name, gadget.ipAddress)) {
                _events.value = Event(ShowToast(R.string.error_message))
                _loading.value = false
                return
            }

            if (user.name.isNotEmpty() && gadget.id != 0) {
                updateUserInfo(activity, user).join()
                updateUserInfo(activity, user).join()
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