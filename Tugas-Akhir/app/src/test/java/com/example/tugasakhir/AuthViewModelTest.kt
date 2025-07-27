package com.example.tugasakhir

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.tugasakhir.ResultState.Success
import com.example.tugasakhir.presentation.auth.AuthRepository
import com.example.tugasakhir.presentation.auth.AuthViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argThat

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AuthViewModel

    @Mock
    private lateinit var repository: AuthRepository

    @Mock
    private lateinit var observer: Observer<ResultState<FirebaseUser?>>

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
        viewModel.authState.observeForever(observer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with empty email returns error`() {
        viewModel.login(email = "", password = "password")

        verify(observer).onChanged(argThat() { result ->
            result is ResultState.Error && result.message == "Tidak Boleh Ada yang Kosong"
        })
    }

    @Test
    fun `register with empty password returns error`() {
        viewModel.register(email = "email@test.com", password = "")

        verify(observer).onChanged(argThat { result ->
            result is ResultState.Error && result.message == "Tidak Boleh Ada yang Kosong"
        })
    }


    @Test
    fun `login with valid input calls repository`() = runTest {
        val email = "email@test.com"
        val password = "123456"
        val result = ResultState.Success<FirebaseUser?>(null)

        `when`(repository.postLogin(email, password)).thenReturn(result)

        viewModel.login(email, password)

        advanceUntilIdle()

        verify(observer).onChanged(result)
    }


    @Test
    fun `register with valid input calls repository`() = runTest {
        val email = "email@test.com"
        val password = "123456"
        val result = Success<FirebaseUser?>(null)

        `when`(repository.postRegister(email, password)).thenReturn(result)

        viewModel.register(email, password)
        advanceUntilIdle()

        verify(observer).onChanged(result)
    }
}
