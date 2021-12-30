package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.*
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class InsertGadgetViewModel(
    private val insertGadget: InsertGadget,
    private val updateGadget: UpdateGadget,
    private val updateUserInfo: UpdateUserInfo,
    private val isGadgetAdded: IsGadgetAdded
) : ViewModel() {

    //region Utils declarations
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _events = MutableLiveData<Event<UiReactions>>()
    val events: LiveData<Event<UiReactions>> get() = _events
    private val _gadget = MutableLiveData<Gadget>()

    sealed class UiReactions {
        object NavListGadgets : UiReactions()
        data class ShowToast(val message: Int) : UiReactions()
        data class IdInsertedGadget(val id: Long) : UiReactions()
    }
    //endregion


    suspend fun insertGadget(gadget: Gadget): Job {
        return viewModelScope.launch {
            val idAddedR: Long = 0
            gadget.unitId?.let {
                isGadgetAdded.invoke(it).collect { addId->
                    val isNotAdded = addId == 0

                    if (isNotAdded) {
                        _events.value = Event(
                            UiReactions.IdInsertedGadget(
                                insertGadget.invoke(
                                    gadget
                                )
                            )
                        )
                    } else {
                        _events.value = Event(
                            UiReactions.IdInsertedGadget(idAddedR)
                        )
                    }


                }
            }

        }
    }

    private suspend fun updateGadget(gadget: Gadget): Job {
        return viewModelScope.launch { updateGadget.invoke(gadget) }

    }

    fun updateUserInfo(activity: Activity, user: UpdateUser): Job {
        return viewModelScope.launch { updateUserInfo.invoke(activity, user) }
    }

}

//region Factory
class InsertGadgetViewModelFactory(
    private val insertGadget: InsertGadget,
    private val updateGadget: UpdateGadget,
    private val updateUserInfo: UpdateUserInfo,
    private val isGadgetAdded: IsGadgetAdded
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsertGadgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsertGadgetViewModel(
                insertGadget,
                updateGadget,
                updateUserInfo,
                isGadgetAdded

            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//endregion