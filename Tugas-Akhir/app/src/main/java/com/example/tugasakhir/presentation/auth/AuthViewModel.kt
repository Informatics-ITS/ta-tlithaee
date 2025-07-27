package com.example.tugasakhir.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhir.ResultState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<ResultState<FirebaseUser?>>()
    val authState: LiveData<ResultState<FirebaseUser?>> get() = _authState

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = ResultState.Error("Tidak Boleh Ada yang Kosong", null)
            return
        }
        viewModelScope.launch {
            _authState.value = authRepository.postLogin(email, password)
        }
    }

    fun register(email: String, password: String){
        if (email.isEmpty() || password.isEmpty()){
            _authState.value = ResultState.Error("Tidak Boleh Ada yang Kosong", null)
            return
        }

        viewModelScope.launch {
            _authState.value = authRepository.postRegister(email, password)
        }


    }
}

