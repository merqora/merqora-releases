package com.mercora.app.ui.screens.auth

import androidx.biometric.BiometricManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(BiometricManager::class)
        val mockBiometricManager = mockk<BiometricManager>(relaxed = true)
        every { BiometricManager.from(any()) } returns mockBiometricManager
        every { mockBiometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS

        val app = RuntimeEnvironment.getApplication()
        viewModel = LoginViewModel(app)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertFalse(state.isAnonymous)
        assertTrue(state.errorMessage.isEmpty())
        assertTrue(state.successMessage.isEmpty())
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `validateEmail with valid email clears error`() {
        viewModel.validateEmail("test@example.com")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `validateEmail with invalid email format sets error`() {
        viewModel.validateEmail("not@valid")
        assertEquals("Correo electrónico no válido", viewModel.uiState.value.emailError)
    }

    @Test
    fun `validateEmail with empty string clears error`() {
        viewModel.validateEmail("")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `validateEmail with username clears error`() {
        viewModel.validateEmail("usuario_sin_arroba")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `validatePassword with long enough password clears error`() {
        viewModel.validatePassword("123456")
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `validatePassword with short password sets error`() {
        viewModel.validatePassword("12345")
        assertEquals("Mínimo 6 caracteres", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `validatePassword with empty string clears error`() {
        viewModel.validatePassword("")
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `clearError resets error message`() {
        viewModel.clearError()
        assertTrue(viewModel.uiState.value.errorMessage.isEmpty())
    }

    @Test
    fun `isBiometricAvailable defaults to true`() {
        assertTrue(viewModel.uiState.value.isBiometricAvailable)
    }

    @Test
    fun `isBiometricAvailable is false when hardware unavailable`() {
        unmockkAll()

        mockkStatic(BiometricManager::class)
        val mockBiometricManager = mockk<BiometricManager>(relaxed = true)
        every { BiometricManager.from(any()) } returns mockBiometricManager
        every { mockBiometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        val app = RuntimeEnvironment.getApplication()
        val vm = LoginViewModel(app)

        assertFalse(vm.uiState.value.isBiometricAvailable)
    }

    @Test
    fun `login with empty email shows error`() {
        viewModel.login("", "password123", false)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Ingresa tu correo o usuario", viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login with empty password shows error`() {
        viewModel.login("test@test.com", "", false)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Ingresa tu contraseña", viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login with short password shows error`() {
        viewModel.login("test@test.com", "12345", false)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Contraseña demasiado corta", viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
