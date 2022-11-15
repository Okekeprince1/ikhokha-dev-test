package com.ikhokha.techcheck.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.databinding.HomeFragmentBinding
import com.ikhokha.techcheck.utils.*
import com.ikhokha.techcheck.utils.adapters.CodeAnalyser
import com.ikhokha.techcheck.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.home_fragment), CodeAnalyser.OnItemScannedListener {

    private lateinit var binding : HomeFragmentBinding
    private val viewModel: HomeViewModel by viewModels()
    @Inject lateinit var fbStorage: FirebaseStorage
    private val requestCode = 1;
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = HomeFragmentBinding.bind(view)
        requestPermission()
        checkLogin()
        setListeners()
    }

    private fun checkLogin() {
        viewModel.loggedIn then showButton()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect { event ->
                when (event) {
                    is HomeViewModel.HomeEvents.LoggedInEvent -> {
                        showButton()
                    }
                    is HomeViewModel.HomeEvents.NotLoggedInEvent -> findNavController()
                            .navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
                    is HomeViewModel.HomeEvents.Error -> {
                        if (event.error == getString(R.string.error_barcode))
                            Snackbar.make(requireView(), getString(R.string.error_barcode), Snackbar.LENGTH_LONG)
                            .setAction("Retry") { startCamera() }
                            .show()
                        else showSnackbar(event.error)
                    }
                    is HomeViewModel.HomeEvents.Success -> showSnackbar(event.success)
                }.exhaustive
            }
        }
    }

    private fun showSnackbar(msg: String, length: Int = Snackbar.LENGTH_LONG) =
        Snackbar.make(requireView(), msg, length).show()

    private fun setListeners() {
        binding.apply {
            login.setOnClickListener { findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment()) }
            basket.setOnClickListener { findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToBasketDialogFragment()) }
            centreImg.setOnClickListener {
                if ( previewView.visibility == View.GONE) startCamera()
                centreImg.visibility = View.GONE
                scan.text = "Scan an item"
            }
            allItems.setOnClickListener { findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToProductsFragment())}
        }
    }

    private fun requestPermission() {
        if (hasCameraPermission(requireContext()) && hasReadPermission(requireContext()) &&
            hasWritePermission(requireContext())) return
        else askPermission()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            this.requestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED
                    && !hasCameraPermission(requireContext())
                ) { askPermission() }
                return
            }
        }
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
    }

    private fun startCamera() {
        binding.previewView.visibility = View.VISIBLE
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, CodeAnalyser(this@HomeFragment))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onItemScanned(code: String?) {
        cameraProvider.unbindAll()
        binding.apply {
            previewView.visibility = View.GONE
            centreImg.visibility = View.VISIBLE
            scan.text = "Scan an item"
        }
        viewModel.insertItem(code)
    }

    private fun showButton() = binding.apply {
        centreImg.setImageDrawable(getTransition(requireContext()))
        centreImg.visibility = View.VISIBLE
        login.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::cameraExecutor.isInitialized) cameraExecutor.shutdown()
    }

}