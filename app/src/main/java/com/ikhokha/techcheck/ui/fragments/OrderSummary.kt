package com.ikhokha.techcheck.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.databinding.OrderSummaryLayoutBinding
import com.ikhokha.techcheck.viewmodels.OrderViewModel
import kotlinx.coroutines.flow.collect
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException


@AndroidEntryPoint
class OrderSummary: Fragment(R.layout.order_summary_layout) {

    private lateinit var binding : OrderSummaryLayoutBinding
    private val viewModel: OrderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = OrderSummaryLayoutBinding.bind(view)
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.share.setOnClickListener {
            viewModel.share()
            findNavController().navigate(OrderSummaryDirections.actionOrderSummaryToHomeFragment())
            viewModel.deleteAll()
        }
        lifecycleScope.launchWhenStarted {
            viewModel.orderEvent.collect{ event ->
                when (event) {
                    is OrderViewModel.OrderEvents.Share -> startActivity(event.intent)
                    OrderViewModel.OrderEvents.ShareError -> Snackbar
                        .make(requireView(), "Nothing to share", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.bmpPdf.observe(viewLifecycleOwner) {
            binding.pdfView.setImageBitmap(it)
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            viewModel.closeRenderer()
        } catch (e: IOException) {
            e.stackTrace
        }
    }
}