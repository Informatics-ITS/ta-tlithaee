package com.example.tugasakhir.presentation.auth

import com.example.tugasakhir.ResultState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepository {
    private val auth = Firebase.auth

    suspend fun postLogin(email: String, password: String): ResultState<FirebaseUser?> {
        return try {
            val user = suspendCoroutine<FirebaseUser?> { continuation ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(auth.currentUser)
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Tidak dapat login")
                            )
                        }
                    }
            }
            ResultState.Success(user)
        } catch (e: Exception) {
            ResultState.Error(e.message, null)
        }
    }

    suspend fun postRegister(email: String, password: String): ResultState<FirebaseUser?> {
        return try {
            val user = suspendCoroutine<FirebaseUser?> { continuation ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(auth.currentUser)
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Tidak dapat login")
                            )
                        }
                    }
            }
            ResultState.Success(user)
        } catch (e: Exception) {
            ResultState.Error(e.message, null)
        }
    }
}

