package com.ikhokha.techcheck.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ikhokha.techcheck.data.entities.Product
import com.ikhokha.techcheck.repositories.FirebaseRepository
import com.ikhokha.techcheck.repositories.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val app: Application,
    fbRepo: FirebaseRepository,
    private val localRepo: LocalRepository
): AndroidViewModel(app) {

    val products: LiveData<List<Product>> = fbRepo.getProducts().asLiveData()

    fun insertItem(product: Product) = viewModelScope.launch(IO) {
        localRepo.insertProduct(product)
    }
}