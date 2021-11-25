package iothoth.edlugora.com.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import iothoth.edlugora.com.R
import iothoth.edlugora.com.data.UsersApplication
import iothoth.edlugora.com.data.model.Users
import iothoth.edlugora.com.databinding.FragmentControlViewBinding
import iothoth.edlugora.com.network.model.ResponseApi
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel
import iothoth.edlugora.com.viewModel.UserDatabaseViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class ControlViewFragment : Fragment() {
    private lateinit var binding: FragmentControlViewBinding
    private val viewModel: UserDatabaseViewModel by activityViewModels {
        UserDatabaseViewModelFactory(
            (activity?.application as UsersApplication).database.userDao()
        )
    }

    private var isSent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        changeColorStatusBar()
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_control_view, container, false)
        verifyFirstConf()
        bind()
        doTestConnection()
        return binding.root
    }

    private fun doTestConnection() {
        viewModel.getGadget.observe(viewLifecycleOwner) {
            if (it != null) {
                lifecycleScope.launch {
                    setVisibilityOnlineBadge(viewModel.sendTestConnection(it))
                }
            }
        }
    }

    fun reload() {
        doTestConnection()
        binding.progressBar.visibility = View.VISIBLE
        Timer().schedule(2000) {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun preventReSend(it: Users, action: String) {
        lifecycleScope.launch {
            if (!isSent) {
                binding.powerIcon.isClickable = false
                binding.carIcon.isClickable = false
                binding.peopleIcon.isClickable = false
                binding.personIcon.isClickable = false
                handlerSnackBar(viewModel.sendActionToApi(it, action))
                binding.progressBar.visibility = View.VISIBLE
                isSent = true
                Timer().schedule(2000) {
                    lifecycleScope.launch {
                        binding.progressBar.visibility = View.GONE
                        isSent = false
                        binding.powerIcon.isClickable = true
                        binding.carIcon.isClickable = true
                        binding.peopleIcon.isClickable = true
                        binding.personIcon.isClickable = true
                    }
                }
            }
        }
    }

    private fun setVisibilityOnlineBadge(res: ResponseApi) {
        if (!res.data.isNullOrEmpty()) {
            binding.onlineCardView.visibility = View.VISIBLE
            binding.powerIcon.setTextColor(getColor(requireContext(), R.color.blue))
        } else {
            binding.powerIcon.setTextColor(getColor(requireContext(), R.color.error))
            binding.onlineCardView.visibility = View.GONE
        }

    }

    fun doActionCar() {
        doTestConnection()
        viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "A")
        }
    }

    fun doActionPerson() {
        doTestConnection()
        viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "P")

        }
    }

    fun doActionPeople() {
        doTestConnection()
        viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "M")

        }
    }

    fun doActionCommon() {
        doTestConnection()
        viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "C")
        }
    }

    private fun handlerSnackBar(res: ResponseApi) {
        if (!res.data.isNullOrEmpty()) {
            showSnackBarSuccess(res.data)
            if (res.data == "Busy") {
                showSnackBarWarning(res.data)
            }
        } else {
            showSnackBarError(res.error ?: "Net Error")
        }
    }

    private fun showSnackBarSuccess(text: String) {
        showSnackBar(text).setBackgroundTint(getColor(requireContext(), R.color.success)).show()
    }

    private fun showSnackBarWarning(text: String) {
        showSnackBar(text).setBackgroundTint(getColor(requireContext(), R.color.warning)).show()
    }

    private fun showSnackBarError(text: String) {
        showSnackBar(text).setBackgroundTint(getColor(requireContext(), R.color.error)).show()
    }

    private fun showSnackBar(text: String): Snackbar {
        return Snackbar.make(
            requireActivity().findViewById(R.id.root_activity),
            text, Snackbar.LENGTH_LONG
        )
    }

    private fun bind() {
        binding.also {
            it.viewModel = viewModel
            it.codeBehind = this@ControlViewFragment
            it.lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun changeColorStatusBar() {
        val currentNightMode =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.white)}
            Configuration.UI_MODE_NIGHT_YES -> {requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.gray_background)}
        }

    }

    private fun verifyFirstConf() {
        viewModel.getUser.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.firstConf == 0) {
                    goToProfileView()
                }
            } else {
                goToProfileView()
            }
        }


    }

    fun goToProfileView() {
        findNavController().navigate(R.id.action_controlViewFragment_to_profileViewFragment)
    }

}