package com.ikhokha.techcheck.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.getValue
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.data.datastore.GUEST_EMAIL
import com.ikhokha.techcheck.data.datastore.UserPreferences
import com.ikhokha.techcheck.data.entities.Product
import com.ikhokha.techcheck.repositories.FirebaseRepository
import com.ikhokha.techcheck.repositories.LocalRepository
import com.ikhokha.techcheck.utils.ValueEventResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fbRepo: FirebaseRepository,
    private val fbAuth: FirebaseAuth,
    userPref: UserPreferences,
    private val app: Application,
    private val localRepo: LocalRepository
): AndroidViewModel(app) {

    private val loginPref = userPref.loginPref
    lateinit var products: LiveData<List<Product>>
    private val homeChannel = Channel<HomeEvents>()
    val homeEvent = homeChannel.receiveAsFlow()
    var loggedIn = false

    init {
        viewModelScope.launch(IO) {
            checkLogin()
        }
    }

    private fun checkLogin() = viewModelScope.launch(IO) {
        if (fbAuth.currentUser != null) {
            homeChannel.send(HomeEvents.LoggedInEvent)
            loggedIn = true
        }
        else homeChannel.send(HomeEvents.NotLoggedInEvent)
    }

    private fun fetchProducts() {
        products = fbRepo.getProducts().asLiveData()
    }

    fun insertItem(code: String?) = viewModelScope.launch(IO) {
        if (code != null)
            when (val result = fbRepo.getItem(code)) {
                is ValueEventResult.Error -> homeChannel.send(HomeEvents.Error(app.getString(R.string.error)))
                is ValueEventResult.Success -> {
                    result.dataSnapshot.apply {
                        if (this.exists()) {
                            val product = getValue<Product>()
                            key?.let {
                                product?.id = it
                            }
                            product?.let {
                                localRepo.insertProduct(it)
                                homeChannel.send(HomeEvents.Success(app.getString(R.string.product_added, it.description)))
                            }
                        } else homeChannel.send(HomeEvents.Error(app.getString(R.string.error_dns)))
                    }
                }
            }
        else homeChannel.send(HomeEvents.Error(app.getString(R.string.error_barcode)))
    }

    sealed class HomeEvents {
        object NotLoggedInEvent: HomeEvents()
        object LoggedInEvent: HomeEvents()
        class Error(val error: String): HomeEvents()
        class Success(val success: String): HomeEvents()
    }

}