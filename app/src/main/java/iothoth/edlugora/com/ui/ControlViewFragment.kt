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
import iothoth.edlugora.com.database.GadgetsEntity
import iothoth.edlugora.com.databinding.FragmentControlViewBinding
import iothoth.edlugora.com.domain.ResponseApi
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class ControlViewFragment : Fragment() {
    private lateinit var binding: FragmentControlViewBinding

    private val viewModel: UserDatabaseViewModel by activityViewModels()

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
        return binding.root
    }

    fun reload() {
        binding.progressBar.visibility = View.VISIBLE
        Timer().schedule(2000) {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun preventReSend(it: GadgetsEntity, action: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            if (!isSent) {
                binding.powerIcon.isClickable = false
                binding.carIcon.isClickable = false
                binding.peopleIcon.isClickable = false
                binding.personIcon.isClickable = false
//                handlerSnackBar(viewModel.sendActionToApi(it, action))
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


    fun doActionCar() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "A")
        }*/
    }

    fun doActionPerson() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "P")

        }*/
    }

    fun doActionPeople() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "M")

        }*/
    }

    fun doActionCommon() {
        /*viewModel.getGadget.observe(viewLifecycleOwner) {
            preventReSend(it, "C")
        }*/
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

        when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.white)}
            Configuration.UI_MODE_NIGHT_YES -> {requireActivity().window.statusBarColor = getColor(requireActivity(), R.color.gray_background)}
        }

    }

    private fun verifyFirstConf() {
        /*viewModel.getUser.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.firstConf == 0) {
                    goToProfileView()
                }
            } else {
                goToProfileView()
            }
        }*/


    }

    fun goToProfileView() {
        findNavController().navigate(R.id.action_controlViewFragment_to_profileViewFragment)
    }

}