package com.unluckygbs.recipebingo.viewmodel.auth

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.dataclass.UserProfile
import com.unluckygbs.recipebingo.data.entity.UserEntity

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    val currentUser: FirebaseUser?
        get() = auth.currentUser


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

    fun register(email: String, password: String, username: String) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            _authState.value = AuthState.Error("Please insert all boxes")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Create user document in Firestore after registration
                        createUserDocument(user, username, email)
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun signout(context: Context){
        auth.signOut()
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID")
                .requestEmail()
                .build()
        ).signOut()

        _authState.value = AuthState.unAuthenticated
    }


    fun checAuthState(){
        if (auth.currentUser==null){
            _authState.value = AuthState.unAuthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }

    }

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Create user document in Firestore if it's the first time login
                        createUserDocument(user, user.displayName ?: "Google User", user.email ?: "No Email")
                    }
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Google sign-in failed")
                }
            }
    }

    private fun createUserDocument(user: FirebaseUser, username: String, email: String) {
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "uid" to user.uid,
            "profileImageBase64" to ""
        )

        firestore.collection("users")
            .document(user.uid) // Use the UID as the document ID
            .set(userData)
            .addOnCompleteListener { firestoreTask ->
                if (firestoreTask.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Error creating user document: ${firestoreTask.exception?.message}")
                }
            }
    }

    // Check authentication state
    fun checkAuthState() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.unAuthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username") ?: "No Name"
                val email = doc.getString("email") ?: "No Email"
                val photo = doc.getString("profileImageBase64")?.takeIf { it.isNotBlank() && it != "null" }
                _userProfile.value = UserProfile(username, email, photo)
            }
            .addOnFailureListener {
                _userProfile.value = null
            }
    }

    fun updateProfileImageBase64(base64String: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .update("profileImageBase64", base64String)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }

}

sealed class AuthState{
    object Authenticated : AuthState()
    object unAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}