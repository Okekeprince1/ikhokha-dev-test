package com.ikhokha.techcheck.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.data.entities.Product
import com.ikhokha.techcheck.databinding.ProductsLayoutBinding
import com.ikhokha.techcheck.utils.adapters.ProductAdapter
import com.ikhokha.techcheck.viewmodels.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductsFragment: Fragment(R.layout.products_layout), ProductAdapter.OnItemClickedListener {

    private lateinit var binding: ProductsLayoutBinding
    private val viewModel: ProductsViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    @Inject lateinit var fbStorage: FirebaseStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProductsLayoutBinding.bind(view)
        recyclerSetup()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.products.observe(viewLifecycleOwner) {
            productAdapter.submitList(it)
            binding.progress.visibility = View.GONE
        }
    }

    private fun setListeners() = binding.basket.setOnClickListener { findNavController()
        .navigate(ProductsFragmentDirections.actionProductsFragmentToBasketDialogFragment()) }

    private fun recyclerSetup() = binding.recycler.apply {
        val gridManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
        layoutManager = gridManager
        productAdapter = ProductAdapter(this@ProductsFragment, requireActivity().application, fbStorage)
        adapter = productAdapter
    }

    override fun onItemClick(product: Product) {
        viewModel.insertItem(product)
        Snackbar.make(requireView(), getString(R.string.product_added, product.description), Snackbar.LENGTH_LONG).show()
    }
}