package com.ikhokha.techcheck.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.ikhokha.techcheck.data.datastore.UserPreferences
import com.ikhokha.techcheck.repositories.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "myT"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val fbLoginRepo: FirebaseRepository,
    private val userPref: UserPreferences
): ViewModel(
) {

    private val _progress: MutableLiveData<Int> = MutableLiveData()
    val progress: LiveData<Int> = _progress
    private var password = ""
    private val loginChannel = Channel<LoginEvents>()
    val loginEvent = loginChannel.receiveAsFlow()

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _progress.postValue(View.VISIBLE)
        try {
            fbLoginRepo.signInUser(email, password).let {
                loginChannel.send(LoginEvents.LoggedInEvent)
                userPref.updateLoginData(email, password, it.uid)
            }
        } catch (e: FirebaseAuthException) {
            loginChannel.send(LoginEvents.LoginFailEvent)
        } finally {
            _progress.postValue(View.GONE)
        }
    }

    sealed class LoginEvents {
        object LoginFailEvent: LoginEvents()
        object LoggedInEvent: LoginEvents()
    }

}