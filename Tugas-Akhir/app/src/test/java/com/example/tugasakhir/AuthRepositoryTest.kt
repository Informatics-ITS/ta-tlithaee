package com.example.tugasakhir

import com.example.tugasakhir.presentation.auth.AuthRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var authResultTask: Task<AuthResult>

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        repository = AuthRepository()

        val field = AuthRepository::class.java.getDeclaredField("auth")
        field.isAccessible = true
        field.set(repository, firebaseAuth)
    }

    @Test
    fun `postLogin returns Success when login is successful`() = runTest {
        `when`(firebaseAuth.signInWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(authResultTask)

        doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
            `when`(authResultTask.isSuccessful).thenReturn(true)
            `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
            listener.onComplete(authResultTask)
            null
        }.`when`(authResultTask).addOnCompleteListener(any())

        val result = repository.postLogin("email@test.com", "password")

        assertTrue(result is ResultState.Success)
        assertEquals(firebaseUser, (result as ResultState.Success).data)
    }

    @Test
    fun `postLogin returns Error when login fails`() = runTest {
        `when`(firebaseAuth.signInWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(authResultTask)

        doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnCompleteListener<AuthResult>
            `when`(authResultTask.isSuccessful).thenReturn(false)
            `when`(authResultTask.exception).thenReturn(Exception("Login error"))
            listener.onComplete(authResultTask)
            null
        }.`when`(authResultTask).addOnCompleteListener(any())

        val result = repository.postLogin("email@test.com", "wrongpass")

        assertTrue(result is ResultState.Error)
        assertEquals("Login error", (result as ResultState.Error).message)
    }
}
