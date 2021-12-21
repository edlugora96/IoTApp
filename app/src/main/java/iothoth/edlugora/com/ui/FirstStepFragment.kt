package iothoth.edlugora.com.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentDetectNetworkBinding
import iothoth.edlugora.com.databinding.FragmentFirstStepBinding
import iothoth.edlugora.com.utils.showLongToast
import iothoth.edlugora.com.viewModel.FirstStepViewModel
import iothoth.edlugora.com.viewModel.ProfileViewModel
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource
import kotlinx.coroutines.launch

class FirstStepFragment : Fragment() {
    private lateinit var binding: FragmentFirstStepBinding
    private val _state: MutableLiveData<Int> = MutableLiveData(0)
    var state: LiveData<Int> = _state

    //region ViewModel Declaration
    private val database: GadgetsRoomDatabase by lazy {
        (activity?.application as IoThothApplication).database
    }
    private val userInfo = UserInfo()
    private val shareUserInfoDataSource =
        UserInfoDataSource(userInfo)
    private val userInfoRepository = UserInfoRepository(shareUserInfoDataSource)
        private val updateUserInfo by lazy {
        UpdateUserInfo(userInfoRepository)
    }
    private val viewModel: FirstStepViewModel by lazy {
        FirstStepViewModel(
            updateUserInfo
        )
    }
    //endregion


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_first_step, container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun goToInsertGadget() {
        val action = FirstStepFragmentDirections.actionFirstStepFragmentToInsertGadgetFragment()
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.also {
            //it.viewModel = viewModel
            it.codeBehind = this@FirstStepFragment
            it.lifecycleOwner = viewLifecycleOwner
        }
        binding.firstContinue.setOnClickListener {
            _state.value = 1
        }
        binding.secondContinue.setOnClickListener {
            if (binding.userNameInput.text.isNullOrEmpty()){
                requireContext().showLongToast(getString(R.string.error_message))
            }else{
                lifecycleScope.launch {
                    viewModel.updateUserInfo(requireActivity(), UpdateUser(
                        name = binding.userNameInput.text.toString(),
                        firstStep = false,
                        startScreen = "control"
                    )).join()
                    goToInsertGadget()
                }
            }
        }
    }

}