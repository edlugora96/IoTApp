package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions
import iothoth.edlugora.com.viewModel.ControlViewModel.UiReactions.*
import iothoth.edlugora.usecases.GetUserInfo
import iothoth.edlugora.usecases.TestGadgetConnection
import iothoth.edlugora.usecases.TriggerGadgetAction

class ControlViewModel(
    private val triggerGadgetAction: TriggerGadgetAction,
    private val testGadgetConnection: TestGadgetConnection,
    private val getUserInfo: GetUserInfo,
    ) : ViewModel() {
    //region Utils declarations
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events

    sealed class UiReactions {
        object NavToProfile : UiReactions()
        data class ShowSuccessSnackBar(val message: String) : UiReactions()
        data class ShowErrorSnackBar(val message: String) : UiReactions()
        data class ShowWarningSnackBar(val message: String) : UiReactions()
    }
    //endregion

    fun getUser(activity: Activity): LiveData<iothoth.edlugora.domain.User?> = MutableLiveData(getUserInfo.invoke(activity))

    fun checkFirstStep(activity: Activity) {
        if (getUser(activity).value?.firstStep == true) {
            _events.value = Event(NavToProfile)
        }
    }

    //region Control handler
    suspend fun testConnection(baseUrl: String, url: String) {
        testGadgetConnection.invoke(baseUrl, url)

    }

    suspend fun gadgetDoAction(baseUrl: String, url: String, data: iothoth.edlugora.domain.RequestApi) {
        triggerGadgetAction.invoke(baseUrl, url, data)

    }
    //endregion

}