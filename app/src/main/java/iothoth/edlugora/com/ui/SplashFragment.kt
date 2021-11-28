package iothoth.edlugora.com.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentSplashBinding
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel
import iothoth.edlugora.com.viewModel.UserDatabaseViewModelFactory
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private var gadgetId: Int = 0

    //region ViewModel Declaration
    private val database: GadgetsRoomDatabase by lazy {
        (activity?.application as IoThothApplication).database
    }
    private val localGadgetDataSource: LocalGadgetDataSource by lazy {
        GadgetRoomDataSource(database)
    }
    private val gadgetRequest = GadgetRequest()
    private val remoteGadgetDataSource = GadgetApiDataSource(gadgetRequest)
    private val gadgetRepository by lazy {
        GadgetRepository(localGadgetDataSource, remoteGadgetDataSource)
    }
    private val userInfo = UserInfo()
    private val shareUserInfoDataSource =
        UserInfoDataSource(userInfo)

    private val userInfoRepository = UserInfoRepository(shareUserInfoDataSource)
    private val getUserInfo = GetUserInfo(userInfoRepository)
    private val setUserInfo = SetUserInfo(userInfoRepository)

    private val triggerGadgetAction by lazy {
        TriggerGadgetAction(gadgetRepository)
    }
    private val testGadgetConnection by lazy {
        TestGadgetConnection(gadgetRepository)
    }
    private val insertGadget by lazy {
        InsertGadget(gadgetRepository)
    }
    private val updateGadget by lazy {
        UpdateGadget(gadgetRepository)
    }
    private val updateUserInfo by lazy {
        UpdateUserInfo(userInfoRepository)
    }
    private val getAllGadgets by lazy {
        GetAllGadgets(gadgetRepository)
    }
    private val getGadget by lazy {
        GetGadget(gadgetRepository)
    }

    private val viewModel: UserDatabaseViewModel by activityViewModels {
        UserDatabaseViewModelFactory(
            triggerGadgetAction,
            testGadgetConnection,
            setUserInfo,
            insertGadget,
            updateGadget,
            updateUserInfo,
            getAllGadgets,
            getUserInfo,
            getGadget
        )
    }
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //viewModel.checkFirstStep(requireActivity())
        //viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
    }

    /*private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is NavToProfile -> goToProfileView()
                else -> goToControlView()
            }
        }

    }

    private fun goToProfileView() {
        val action = SplashFragmentDirections.actionSplashFragmentToProfileViewFragment(
            viewModel.getGadgets.value?.first()?.id ?: 0
        )
        findNavController().navigate(action)
    }

    private fun goToControlView() {
        val action = SplashFragmentDirections.actionSplashFragmentToControlViewFragment(
            viewModel.getGadgets.value?.first()?.id ?: 0,
            viewModel.getGadgets.value?.first()?.type ?: "control"
        )
        findNavController().navigate(R.id.action_splashFragment_to_controlViewFragment)
    }*/
    //endregion
}
