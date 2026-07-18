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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val app = RuntimeEnvironment.getApplication()
        viewModel = RegisterViewModel(app)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has default values`() {
        val s = viewModel.uiState.value
        assertFalse(s.isLoading)
        assertFalse(s.isSuccess)
        assertTrue(s.errorMessage.isEmpty())
        assertNull(s.emailError)
        assertNull(s.usernameError)
        assertNull(s.passwordError)
        assertNull(s.confirmPasswordError)
        assertNull(s.generoError)
        assertNull(s.fechaError)
        assertEquals(0f, s.passwordStrength)
        assertTrue(s.passwordStrengthLabel.isEmpty())
    }

    @Test
    fun `validateEmail with valid email clears error`() {
        viewModel.validateEmail("user@example.com")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `validateEmail with invalid format sets error`() {
        viewModel.validateEmail("not@valid")
        assertEquals("Correo electrÃ³nico no vÃ¡lido", viewModel.uiState.value.emailError)
    }

    @Test
    fun `validateEmail with empty clears error`() {
        viewModel.validateEmail("")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `validateUsername with valid username clears error`() {
        viewModel.validateUsername("john_doe")
        assertNull(viewModel.uiState.value.usernameError)
    }

    @Test
    fun `validateUsername with short username sets error`() {
        viewModel.validateUsername("ab")
        assertEquals("MÃ­nimo 3 caracteres", viewModel.uiState.value.usernameError)
    }

    @Test
    fun `validateUsername with invalid chars sets error`() {
        viewModel.validateUsername("user name!")
        assertEquals("Solo letras, nÃºmeros y _", viewModel.uiState.value.usernameError)
    }

    @Test
    fun `validateUsername with empty clears error`() {
        viewModel.validateUsername("")
        assertNull(viewModel.uiState.value.usernameError)
    }

    @Test
    fun `validatePassword with valid password clears error`() {
        viewModel.validatePassword("123456")
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `validatePassword with short password sets error`() {
        viewModel.validatePassword("12345")
        assertEquals("MÃ­nimo 6 caracteres", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `validatePassword with empty clears error and resets strength`() {
        viewModel.validatePassword("")
        assertNull(viewModel.uiState.value.passwordError)
        assertEquals(0f, viewModel.uiState.value.passwordStrength)
    }

    @Test
    fun `validateConfirmPassword matching clears error`() {
        viewModel.validateConfirmPassword("pass123", "pass123")
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `validateConfirmPassword not matching sets error`() {
        viewModel.validateConfirmPassword("pass123", "pass456")
        assertEquals("Las contraseÃ±as no coinciden", viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `validateConfirmPassword empty clears error`() {
        viewModel.validateConfirmPassword("pass123", "")
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `validateDate with valid date clears error`() {
        viewModel.validateDate("15/06/2000")
        assertNull(viewModel.uiState.value.fechaError)
    }

    @Test
    fun `validateDate too young sets error`() {
        viewModel.validateDate("15/06/2022")
        assertEquals("Debes tener al menos 13 aÃ±os", viewModel.uiState.value.fechaError)
    }

    @Test
    fun `validateDate invalid format sets error`() {
        viewModel.validateDate("aa/bb/cccc")
        assertEquals("Fecha invÃ¡lida", viewModel.uiState.value.fechaError)
    }

    @Test
    fun `validateDate wrong length sets error`() {
        viewModel.validateDate("01/01/20")
        assertEquals("Formato: DD/MM/YYYY", viewModel.uiState.value.fechaError)
    }

    @Test
    fun `validateDate empty clears error`() {
        viewModel.validateDate("")
        assertNull(viewModel.uiState.value.fechaError)
    }

    @Test
    fun `password strength is weak for simple password`() {
        viewModel.validatePassword("abc")
        assertTrue(viewModel.uiState.value.passwordStrength < 0.25f)
        assertEquals("DÃ©bil", viewModel.uiState.value.passwordStrengthLabel)
    }

    @Test
    fun `password strength is strong for complex password`() {
        viewModel.validatePassword("Str0ng!Pass#99")
        assertTrue(viewModel.uiState.value.passwordStrength > 0.75f)
        assertEquals("Segura", viewModel.uiState.value.passwordStrengthLabel)
    }

    @Test
    fun `password strength labels are correct for weak password`() {
        viewModel.validatePassword("abc")
        assertEquals("DÃ©bil", viewModel.uiState.value.passwordStrengthLabel)
    }

    @Test
    fun `password strength labels are correct for medium password`() {
        viewModel.validatePassword("Pass1")
        assertEquals("Regular", viewModel.uiState.value.passwordStrengthLabel)
    }

    @Test
    fun `password strength labels are correct for good password`() {
        viewModel.validatePassword("Pass1234")
        assertEquals("Buena", viewModel.uiState.value.passwordStrengthLabel)
    }
}
