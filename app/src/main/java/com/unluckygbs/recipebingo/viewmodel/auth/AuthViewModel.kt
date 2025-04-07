package com.unluckygbs.recipebingo.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    init {
        checAuthState()
    }

    fun login(email : String, password : String){

        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Please Insert Email or Password")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "error")
                }
            }
    }

    fun register(email : String, password : String){

        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Please Insert Email or Password")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "error")
                }
            }
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.unAuthenticated
    }


    fun checAuthState(){
        if (auth.currentUser==null){
            _authState.value = AuthState.unAuthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }

    }
}

sealed class AuthState{
    object Authenticated : AuthState()
    object unAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}