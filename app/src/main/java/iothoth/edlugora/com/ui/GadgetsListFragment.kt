package iothoth.edlugora.com.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.adapters.GadgetAdapter
import iothoth.edlugora.com.databinding.FragmentGadgetsListBinding
import iothoth.edlugora.com.utils.changeColorStatusBar
import iothoth.edlugora.com.utils.showLongSnackBar
import iothoth.edlugora.com.viewModel.GadgetsListViewModel
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.emptyGadget
import iothoth.edlugora.domain.User
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.usecases.*
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource
import kotlinx.coroutines.launch

class GadgetsListFragment : Fragment() {

    private lateinit var binding: FragmentGadgetsListBinding

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user


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

    private val deleteGadget by lazy {
        DeleteGadget(gadgetRepository)
    }

    private val viewModel: GadgetsListViewModel by lazy {
        GadgetsListViewModel(
            setUserInfo,
            insertGadget,
            updateGadget,
            updateUserInfo,
            getUserInfo,
            getAllGadgets,
            deleteGadget
        )
    }
    //endregion


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext().changeColorStatusBar(
            requireActivity(),
            R.color.white,
            R.color.gray_background
        )
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gadgets_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GadgetAdapter {
            val action =
                GadgetsListFragmentDirections.actionGadgetsListFragmentToControlViewFragment(
                    it.id!!,
                    it.type.toString()
                )
            findNavController().navigate(action)
        }
        binding.recycleGadget.adapter = adapter
        viewModel.allGadget.observe(viewLifecycleOwner) { gadget ->
            gadget.let {
                if (it!=null){
                    adapter.submitList(it)
                }
            }
        }
        bind()
        viewModel.checkFirstStep(requireActivity())
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
        fillUserAndGadget()
    }

    //region Methods
    private fun bind() {
        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this@GadgetsListFragment
            it.lifecycleOwner = viewLifecycleOwner
        }

        binding.navBar.profilePhotoCard.setOnClickListener {
            goToDetectNetworkView()
        }
    }

    private fun fillUserAndGadget() {
        viewModel.getUser(requireActivity()).observe(viewLifecycleOwner) {
            _user.value = it
            binding.navBar.profileName.text = it?.name.toString() ?: ""
            binding.navBar.gadgetName.text = getText(R.string.list_of_devices)
            binding.navBar.menuIcon.visibility = View.GONE
            binding.navBar.onlineCardView.visibility = View.GONE
        }
    }

    private fun goToDetectNetworkView() {
        val action = GadgetsListFragmentDirections.actionGadgetsListFragmentToDetectNetworkFragment()
        findNavController().navigate(action)
    }

    fun goToInsertGadget() {
        val action = GadgetsListFragmentDirections.actionGadgetsListFragmentToInsertGadgetFragment()
        findNavController().navigate(action)
    }

    private fun validateEvents(event: Event<GadgetsListViewModel.UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is GadgetsListViewModel.UiReactions.ShowErrorSnackBar -> reaction.run {
                    requireActivity().showLongSnackBar(
                        R.id.root_activity,
                        this.message,
                        ContextCompat.getColor(requireContext(), R.color.error)
                    )
                }
                is GadgetsListViewModel.UiReactions.ShowSuccessSnackBar -> reaction.run {
                    requireActivity().showLongSnackBar(
                        R.id.root_activity,
                        this.message,
                        ContextCompat.getColor(requireContext(), R.color.success)
                    )
                }
                is GadgetsListViewModel.UiReactions.ShowWarningSnackBar -> reaction.run {
                    requireActivity().showLongSnackBar(
                        R.id.root_activity,
                        this.message,
                        ContextCompat.getColor(requireContext(), R.color.warning)
                    )
                }
                else -> goToDetectNetworkView()
            }

        }

    }
    //endregion

}
