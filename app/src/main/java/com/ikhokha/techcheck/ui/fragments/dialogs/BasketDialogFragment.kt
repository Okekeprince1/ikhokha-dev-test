package com.ikhokha.techcheck.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.data.entities.Product
import com.ikhokha.techcheck.databinding.BasketLayoutBinding
import com.ikhokha.techcheck.utils.adapters.BasketAdapter
import com.ikhokha.techcheck.utils.then
import com.ikhokha.techcheck.viewmodels.BasketViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BasketDialogFragment : DialogFragment(R.layout.basket_layout), BasketAdapter.OnItemClickedListener {

    private lateinit var binding : BasketLayoutBinding
    private val viewModel: BasketViewModel by viewModels()
    private lateinit var basketAdapter: BasketAdapter
    @Inject lateinit var fbStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BasketLayoutBinding.inflate(LayoutInflater.from(requireContext()))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
            .setView(binding.root)
            .create()

    override fun onStart() {
        super.onStart()
        setListeners()
        adapterSetup()
        setObservers()
    }

    private fun adapterSetup() {
        binding.basketRecycler.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            layoutManager = linearLayoutManager
            basketAdapter = BasketAdapter(this@BasketDialogFragment, requireActivity().application, fbStorage)
            adapter = basketAdapter
        }
    }

    private fun setListeners() {
        binding.checkout.setOnClickListener {
//            Snackbar.make(requireView(), "Success!! Thank you for your purchase.", Snackbar.LENGTH_LONG).show()
            findNavController().navigate(BasketDialogFragmentDirections.actionBasketDialogFragmentToOrderSummary())
        }

    }

    private fun setObservers() {
        viewModel.products.observe(viewLifecycleOwner) {
            basketAdapter.submitList(it)
            binding.apply {
                progress.visibility = View.GONE
                if (it.isNotEmpty()) empty.visibility = View.GONE
            }
        }
        viewModel.total.observe(viewLifecycleOwner) {
            binding.total.text = getString(R.string.product_price, it)
        }
    }

    override fun onItemClick(product: Product) {
        // allow to add, subtract or remove item
    }
}