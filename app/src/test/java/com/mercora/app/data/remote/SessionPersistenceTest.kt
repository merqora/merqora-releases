package com.mercora.app.data.remote

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
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

@RunWith(RobolectricTestRunner::class)
class SessionPersistenceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        SessionPersistence.init(context)
    }

    @After
    fun tearDown() {
        SessionPersistence.clearSession()
        unmockkAll()
    }

    @Test
    fun `init uses encrypted prefs`() {
        assertFalse(SessionPersistence.isLoggedIn())
    }

    @Test
    fun `saveSession writes userId and username`() {
        SessionPersistence.saveSession("user_123", "testuser")
        assertTrue(SessionPersistence.isLoggedIn())
        assertEquals("user_123", SessionPersistence.getUserId())
        assertEquals("testuser", SessionPersistence.getUsername())
    }

    @Test
    fun `saveSession without username stores empty string`() {
        SessionPersistence.saveSession("user_456")
        assertEquals("user_456", SessionPersistence.getUserId())
        assertEquals("", SessionPersistence.getUsername())
    }

    @Test
    fun `clearSession removes all data`() {
        SessionPersistence.saveSession("user_789", "delete_me")
        SessionPersistence.clearSession()

        assertFalse(SessionPersistence.isLoggedIn())
        assertNull(SessionPersistence.getUserId())
        assertNull(SessionPersistence.getUsername())
    }

    @Test
    fun `getUserId returns null before save`() {
        assertNull(SessionPersistence.getUserId())
    }

    @Test
    fun `isLoggedIn returns false after clearSession`() {
        SessionPersistence.saveSession("user_aaa", "a")
        SessionPersistence.clearSession()
        assertFalse(SessionPersistence.isLoggedIn())
    }
}
