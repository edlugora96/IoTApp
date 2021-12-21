package iothoth.edlugora.com.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.usecases.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FirstStepViewModel(
    private val updateUserInfo: UpdateUserInfo,
) : ViewModel() {
    fun updateUserInfo(activity: Activity, user: UpdateUser): Job {
        return viewModelScope.launch { updateUserInfo.invoke(activity, user) }
    }
}