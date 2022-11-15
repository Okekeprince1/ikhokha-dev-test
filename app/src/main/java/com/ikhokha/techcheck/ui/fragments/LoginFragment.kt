package com.ikhokha.techcheck.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ikhokha.techcheck.R
import com.ikhokha.techcheck.databinding.LoginFragmentBinding
import com.ikhokha.techcheck.utils.exhaustive
import com.ikhokha.techcheck.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "TAG"

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.login_fragment) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding : LoginFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginFragmentBinding.bind(view)
        setListeners()
        setObservers()
        Log.d(TAG, "Login")
    }

    private fun setObservers() {
        viewModel.progress.observe(viewLifecycleOwner) { binding.progress.visibility = it }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loginEvent.collect { event ->
                when (event) {
                    is LoginViewModel.LoginEvents.LoggedInEvent -> {
                        Snackbar.make(requireView(), getString(R.string.login_success), Snackbar.LENGTH_LONG).show()
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                    }
                    LoginViewModel.LoginEvents.LoginFailEvent ->
                        Snackbar.make(requireView(), getString(R.string.login_error), Snackbar.LENGTH_LONG).show()
                }.exhaustive
            }
        }
    }

    private fun setListeners() {
        binding.emailInput.requestFocus()
        binding.submit.setOnClickListener{
            viewModel.loginUser(getEmailText(), getPasswordText())
        }
    }

    private fun getEmailText(): String {
        return binding.email.editText?.text.toString().trim() {it <= ' '}
    }

    private fun getPasswordText(): String {
        return binding.password.editText?.text.toString().trim() {it <= ' '}
    }

}
