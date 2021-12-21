package iothoth.edlugora.com.ui

import IoThoth.edlugora.barcodescannermanager.BarcodeScannerManager
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import iothoth.edlugora.com.IoThothApplication
import iothoth.edlugora.com.R
import iothoth.edlugora.com.databinding.FragmentInsertGadgetBinding
import iothoth.edlugora.com.utils.changeColorStatusBar
import iothoth.edlugora.com.utils.loadImage
import iothoth.edlugora.com.utils.setLightStatusBar
import iothoth.edlugora.com.utils.showLongToast
import iothoth.edlugora.com.viewModel.InsertGadgetViewModel
import iothoth.edlugora.com.viewModel.InsertGadgetViewModel.UiReactions
import iothoth.edlugora.com.viewModel.InsertGadgetViewModel.UiReactions.*
import iothoth.edlugora.com.viewModel.InsertGadgetViewModelFactory
import iothoth.edlugora.com.viewModel.ProfileViewModel
import iothoth.edlugora.com.viewModel.utils.Event
import iothoth.edlugora.cryptography.CryptographyManager.decrypt
import iothoth.edlugora.databasemanager.GadgetRoomDataSource
import iothoth.edlugora.databasemanager.GadgetsRoomDatabase
import iothoth.edlugora.domain.BarcodeScannerConstants.FAIL_QR
import iothoth.edlugora.domain.BarcodeScannerConstants.MAX_IMAGE_DIMENSION
import iothoth.edlugora.domain.BarcodeScannerConstants.SEARCHING_QR
import iothoth.edlugora.domain.BarcodeScannerConstants.SECRET
import iothoth.edlugora.domain.BarcodeScannerConstants.SUCCESS_QR
import iothoth.edlugora.domain.Gadget
import iothoth.edlugora.domain.UpdateUser
import iothoth.edlugora.domain.repository.LocalGadgetDataSource
import iothoth.edlugora.networkmanager.GadgetApiDataSource
import iothoth.edlugora.networkmanager.GadgetRequest
import iothoth.edlugora.repository.GadgetRepository
import iothoth.edlugora.repository.UserInfoRepository
import iothoth.edlugora.usecases.InsertGadget
import iothoth.edlugora.usecases.UpdateGadget
import iothoth.edlugora.usecases.UpdateUserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfo
import iothoth.edlugora.userpreferencesmanager.UserInfoDataSource
import kotlinx.coroutines.launch
import java.io.IOException


class InsertGadgetFragment : Fragment() {
    //region Declarations
    private lateinit var barcodeScanner: BarcodeScannerManager
    private lateinit var binding: FragmentInsertGadgetBinding
    private var torchOn = false
    private var isEnableBackPress = true

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var gadgetJsonAdapter: JsonAdapter<Gadget> =
        moshi.adapter(Gadget::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                barcodeScanner.start()
            } else {
                selectImageFromGallery()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.inputImageView.setImageDrawable(null)
                val inputBitmap: Bitmap?

                try {
                    inputBitmap = loadImage(
                        requireContext(), uri,
                        MAX_IMAGE_DIMENSION
                    )
                    barcodeScanner.analyzeImageFormBitmap(inputBitmap!!)
                } catch (e: IOException) {
                    onEvent(FAIL_QR)
                }
            }
        }
    //endregion

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

    private val insertGadget by lazy {
        InsertGadget(gadgetRepository)
    }
    private val updateGadget by lazy {
        UpdateGadget(gadgetRepository)
    }
    private val updateUserInfo by lazy {
        UpdateUserInfo(userInfoRepository)
    }

    private val viewModel: InsertGadgetViewModel by activityViewModels {
        InsertGadgetViewModelFactory(
            insertGadget,
            updateGadget,
            updateUserInfo
        )
    }
    //endregion

    //region Overrides Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEnableBackPress) {
                        findNavController().popBackStack()
                    }
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext().changeColorStatusBar(
            requireActivity(),
            R.color.black
        )
        requireActivity().setLightStatusBar()
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_insert_gadget, container, false)
        checkPermissionAndStartCamera()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        viewModel.events.observe(viewLifecycleOwner, Observer(this::validateEvents))
    }
    //endregion

    //region Methods
    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bind() {
        binding.bottomPromptChip.text = getText(R.string.searching_qr_message)
        binding.navBarLayout.galleryIcon.setOnClickListener {
            selectImageFromGallery()
        }
        binding.navBarLayout.upButtonIcon.setOnClickListener {
            barcodeScanner.stop()
            findNavController().popBackStack()
        }

    }

    private fun bindTorch() {
        binding.navBarLayout.flashButton.also { flashIcon ->
            run {
                flashIcon.visibility = if (barcodeScanner.hasTorch()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                flashIcon.setOnClickListener {
                    barcodeScanner.toggleTorch(!torchOn)
                    flashIcon.isSelected = !torchOn
                    torchOn = !torchOn
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermissionAndStartCamera() {
        barcodeScanner = BarcodeScannerManager(
            owner = requireActivity() as AppCompatActivity,
            context = requireContext(),
            viewFinder = binding.cameraView,
            onEvent = this::onEvent,
            onCameraReady = this::bindTorch
        )

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission.launch(Manifest.permission.CAMERA)
        } else {
            barcodeScanner.start()
        }
    }

    private fun clickableIcons(itIs: Boolean) {
        binding.navBarLayout.upButtonIcon.isClickable = itIs
        binding.navBarLayout.flashButton.isClickable = itIs
        binding.navBarLayout.galleryIcon.isClickable = itIs
        isEnableBackPress = itIs
    }

    private fun goToAllGadget() {
        val action = InsertGadgetFragmentDirections.actionInsertGadgetFragmentToGadgetsListFragment()
        findNavController().navigate(action)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onEvent(event: String, result: String? = null) {
        when (event) {
            SUCCESS_QR -> run {
                binding.bottomPromptChip.text = getText(R.string.success_qr_message)
                barcodeScanner.pause()
                try {
                    val resultDecrypted = decrypt(result, SECRET)!!
                    val gadgetData = gadgetJsonAdapter.fromJson(resultDecrypted)
                    clickableIcons(false)
                    lifecycleScope.launch {
                        viewModel.insertGadget(gadgetData!!).join()
                        goToAllGadget()
                        //findNavController().popBackStack()
                    }
                } catch (ex: Exception) {
                    requireContext().showLongToast(R.string.error_qr_message)
                    binding.bottomPromptChip.text = getText(R.string.searching_qr_message)
                    barcodeScanner.start()
                    clickableIcons(true)
                }

            }
            SEARCHING_QR -> run {
                binding.bottomPromptChip.text = getText(R.string.searching_qr_message)
                barcodeScanner.pause()
                clickableIcons(true)
            }
            else -> run {
                requireContext().showLongToast(R.string.error_qr_message)
                binding.bottomPromptChip.text = getText(R.string.searching_qr_message)
                clickableIcons(true)
            }
        }

    }

    private fun validateEvents(event: Event<UiReactions>?) {
        event?.getContentIfNotHandled().let { reaction ->
            when (reaction) {
                is ShowToast -> reaction.run {
                    requireContext().showLongToast(this.message)
                }
                is IdInsertedGadget -> reaction.run {
                    viewModel.updateUserInfo(
                        requireActivity(), UpdateUser(
                            lastGadgetAdded = this.id.toInt()
                        )
                    )
                }
                else -> {}
            }

        }

    }
    //endregion
}
