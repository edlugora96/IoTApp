package iothoth.edlugora.com.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import iothoth.edlugora.com.R
import iothoth.edlugora.com.data.Dao.UsersDao
import iothoth.edlugora.com.data.model.EntitiesCombined
import iothoth.edlugora.com.data.model.Users
import iothoth.edlugora.com.network.GadgetApi
import iothoth.edlugora.com.network.model.RequestApi
import iothoth.edlugora.com.network.model.ResponseApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UserDatabaseViewModel(private val usersDao: UsersDao) : ViewModel() {

    val getUser = usersDao.getUser().asLiveData()
    val getGadget = usersDao.getGadget().asLiveData()

    private fun insert(data: EntitiesCombined): Job {
        return viewModelScope.launch {
            usersDao.insert(
                Users(
                    name = data.user.name,
                    type = "user",
                    firstConf = 1,
                    host = ""
                )
            )
            usersDao.insert(
                Users(
                    name = data.gadget.name,
                    host = data.gadget.host,
                    firstConf = 1,
                    type = "gadget",
                )
            )
        }
    }

    private fun update(data: EntitiesCombined): Job {
        return viewModelScope.launch {
            usersDao.update(
                Users(
                    id = data.user.id,
                    name = data.user.name,
                    type = "user",
                    firstConf = 1,
                    host = ""
                )
            )
            usersDao.update(
                Users(
                    id = data.gadget.id,
                    name = data.gadget.name,
                    host = data.gadget.host,
                    firstConf = 1,
                    type = "gadget",
                )
            )
        }
    }

    suspend fun insertOrUpdateUser(data: EntitiesCombined): Boolean {
        return if (isEntryValid(data.gadget.name, data.user.name, data.gadget.host)) {
            if (data.user.id > 0 && data.gadget.id > 0) {
                update(data).join()
            } else {
                insert(data).join()
            }
            true
        } else {
            false
        }
    }

    private fun isEntryValid(gadgetName: String, userName: String, host: String): Boolean {
        if (gadgetName.isBlank() || userName.isBlank() || host.isBlank()) {
            return false
        }
        return true
    }

     suspend fun sendActionToApi(gadget:Users, action: String) : ResponseApi{
         return try {
             GadgetApi(gadget.host).retrofitService.doAction(
                 RequestApi(
                     data = action
                 )
             )


         } catch (e: Exception) {
             Log.e("Api Error", "$e")
             ResponseApi(
                 data = "",
                 error = "$e"
             )
         }
    }

    suspend fun sendTestConnection(gadget:Users) : ResponseApi{
        return try {
            GadgetApi(gadget.host).retrofitService.testConnection()

        } catch (e: Exception) {
            Log.e("Api Error", "$e")
            ResponseApi(
                data = "",
                error = "$e"
            )
        }
    }
}

class UserDatabaseViewModelFactory(private val usersDao: UsersDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDatabaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDatabaseViewModel(usersDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

