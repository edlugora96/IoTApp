package iothoth.edlugora.com.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.viewModel.MainViewModel
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.User
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var navController: NavController
    private var _userInfo: User? = null

    //region ViewModel Declaration
    private val userInfo = UserInfo()
    private val shareUserInfoDataSource =
        UserInfoDataSource(userInfo)

    private val userInfoRepository = UserInfoRepository(shareUserInfoDataSource)
    private val getUserInfo = GetUserInfo(userInfoRepository)

    private val viewModel: MainViewModel by lazy {
        MainViewModel(
            getUserInfo
        )
    }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_main)
        val navGraph = navController.graph
        _userInfo = viewModel.getUser(activity = this).value

        if (_userInfo?.firstStep != null && viewModel.getUser(
                activity = this
            ).value?.firstStep == true
        ) {
            navGraph.startDestination = R.id.profileViewFragment
        } else if (_userInfo?.lastGadgetAdded != null) {
            if(_userInfo!!.lastGadgetAdded > 0 && _userInfo!!.lastGadgetAdded > 2){
                navGraph.startDestination = R.id.gadgetsListFragment
            }
        } else {
            navGraph.startDestination = R.id.gadgetsListFragment
        }

        navController.graph = navGraph

    }
}