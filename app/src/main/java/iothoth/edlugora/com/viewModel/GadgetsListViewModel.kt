package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.com.viewModel.GadgetsListViewModel.UiReactions
import iothoth.edlugora.com.viewModel.GadgetsListViewModel.UiReactions.*
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.User
import iothoth.edlugora.domain.emptyGadget
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GadgetsListViewModel(
    private val setUserInfo: SetUserInfo,
    private val insertGadget: InsertGadget,
    private val updateGadget: UpdateGadget,
    private val updateUserInfo: UpdateUserInfo,
    private val getUserInfo: GetUserInfo,
    private val getAllGadgets: GetAllGadgets,
    private val deleteGadget: DeleteGadget,
) : ViewModel() {
    //region Utils declarations
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events
    private val _gadget = MutableLiveData<Gadget>()
    private val _gadgets = MutableLiveData<List<Gadget>>()

    sealed class UiReactions {
        object NavToProfile : UiReactions()
        data class ShowSuccessSnackBar(val message: String) : UiReactions()
        data class ShowErrorSnackBar(val message: String) : UiReactions()
        data class ShowWarningSnackBar(val message: String) : UiReactions()
        data class GetUserR(val data: User) : UiReactions()
        data class GetGadgetR(val data: Gadget) : UiReactions()
    }
    //endregion

    //region Get gadget and user information
    /*fun gadget(id: Int): LiveData<Gadget> {
        return if (id > 0) {
            viewModelScope.launch {
                getGadget.invoke(id).collect {
                    _gadget.value = it
                }
            }
            _gadget
        } else {
            MutableLiveData(_gadget.emptyGadget())
        }

    }*/

    /*val allGadget: LiveData<List<Gadget>> = run {
        viewModelScope.launch {
            getAllGadgets.invoke().collect {
                if (it != null && it.isNotEmpty()) {
                    _gadgets.value = it
                }
            }
        }
        _gadgets
    }*/

    val allGadget = getAllGadgets.invoke().asLiveData()

    fun getUser(activity: Activity): LiveData<User?> = MutableLiveData(getUserInfo.invoke(activity))
    //endregion

    fun checkFirstStep(activity: Activity) {
        if (getUser(activity).value?.firstStep == true) {
            _events.value = Event(NavToProfile)
        }
    }
}
