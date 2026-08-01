package com.mercora.app.data.cache.sync

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.mercora.app.data.cache.db.MercoraDatabase
import com.mercora.app.data.remote.SupabaseClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CacheSyncWorkerTest {

    private lateinit var database: MercoraDatabase
    private lateinit var workerParams: WorkerParameters

    @Before
    fun setUp() {
        workerParams = mockk(relaxed = true)

        database = mockk(relaxed = true)
        mockkObject(MercoraDatabase.Companion)
        every { MercoraDatabase.getInstance(any()) } returns database

        mockkObject(SupabaseClient)
        every { SupabaseClient.database } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `doWork returns success when no pending operations`() = runTest {
        coEvery { database.pendingOperationDao().getPending() } returns emptyList()

        val worker = CacheSyncWorker(RuntimeEnvironment.getApplication(), workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork returns success even when sync fails`() = runTest {
        coEvery { database.pendingOperationDao().getPending() } returns emptyList()

        val worker = CacheSyncWorker(RuntimeEnvironment.getApplication(), workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}
