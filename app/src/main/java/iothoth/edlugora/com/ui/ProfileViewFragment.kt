package iothoth.edlugora.com.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import iothoth.edlugora.com.R
import iothoth.edlugora.com.data.UsersApplication
import iothoth.edlugora.com.data.model.EntitiesCombined
import iothoth.edlugora.com.data.model.Users
import iothoth.edlugora.com.databinding.FragmentProfileViewBinding
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel
import iothoth.edlugora.com.viewModel.UserDatabaseViewModelFactory
import kotlinx.coroutines.launch


class ProfileViewFragment : Fragment() {

    private lateinit var binding: FragmentProfileViewBinding
    private val viewModel: UserDatabaseViewModel by activityViewModels {
        UserDatabaseViewModelFactory(
            (activity?.application as UsersApplication).database.userDao()
        )
    }

    private var _userId = 0
    private var _gadgetId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        changeColorStatusBar()
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile_view, container, false)
        bind()
        observeAndUpdateIds()
        return binding.root
    }

    private fun bind() {
        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this@ProfileViewFragment
            it.lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun changeColorStatusBar() {
        requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.blue)
    }

    private fun setInfoFromInputs(): EntitiesCombined {
        return EntitiesCombined(
            user = Users(
                id = _userId,
                name = binding.userNameInput.text.toString(),
                firstConf = 1,
                host = "",
                type = "user"
            ),
            gadget = Users(
                id = _gadgetId,
                name = binding.gadgetNameInput.text.toString(),
                firstConf = 1,
                host = binding.hostInput.text.toString(),
                type = "gadget"
            )
        )
    }

    fun insertOrUpdateData() {
        lifecycleScope.launch {
            if (viewModel.insertOrUpdateUser(setInfoFromInputs())) {
                goControlView()
            } else {
                showToast()
            }
        }
    }

    private fun showToast() {
        Toast.makeText(requireContext(), R.string.error_message, Toast.LENGTH_SHORT).show()
    }

    fun goControlView() {
        findNavController().navigate(R.id.action_profileViewFragment_to_controlViewFragment)
    }

    private fun observeAndUpdateIds() {

        viewModel.getUser.observe(viewLifecycleOwner) {
            if (it != null) {
                _gadgetId = it.id
            }
        }

        viewModel.getGadget.observe(viewLifecycleOwner) {
            if (it != null) {
                _userId = it.id
            }
        }
    }
}