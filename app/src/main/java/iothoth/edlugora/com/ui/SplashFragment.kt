package iothoth.edlugora.com.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentSplashBinding
import iothoth.edlugora.com.viewModel.UserDatabaseViewModel

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private val viewModel: UserDatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        selectDestiny()
        return binding.root
    }

    private fun selectDestiny() {
        /*lifecycleScope.launch {
            viewModel.getUser.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it.firstConf == 0) {
                        findNavController().navigate(R.id.action_splashFragment_to_profileViewFragment)
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_controlViewFragment)
                    }
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_profileViewFragment)
                }
            }
        }*/

    }
}
