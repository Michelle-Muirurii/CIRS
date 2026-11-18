package com.example.communityinfrastructurereportingsystem

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.communityinfrastructurereportingsystem.data.local.CirsDatabase
import com.example.communityinfrastructurereportingsystem.data.local.ReportDao
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

// Reads the current value of a LiveData in a test
fun <T> LiveData<T>.getOrAwaitValue(): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    observeForever(observer)
    if (!latch.await(2, TimeUnit.SECONDS)) {
        removeObserver(observer)
        throw TimeoutException("LiveData value was never set")
    }
    @Suppress("UNCHECKED_CAST")
    return data as T
}

@RunWith(AndroidJUnit4::class)
class ReportDaoTest {

    // Makes LiveData work instantly in tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: CirsDatabase
    private lateinit var dao: ReportDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CirsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.reportDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun sampleReport(title: String, category: String = ReportCategory.POTHOLE) = Report(
        title = title,
        category = category,
        description = "Test description for $title",
        area = "Westlands",
        reporterId = 1,
        reporterName = "Test User"
    )

    @Test
    fun insertReport_thenReadItBack() = runBlocking<Unit> {
        dao.insertReport(sampleReport("Pothole on Ngong Road"))

        val reports = dao.getAllReports().getOrAwaitValue()
        assertEquals(1, reports.size)
        assertEquals("Pothole on Ngong Road", reports[0].title)
        assertEquals(ReportStatus.PENDING, reports[0].status)
    }

    @Test
    fun updateReport_changesTheStatus() = runBlocking<Unit> {
        dao.insertReport(sampleReport("Broken light"))
        val saved = dao.getAllReports().getOrAwaitValue()[0]

        dao.updateReport(saved.copy(status = ReportStatus.RESOLVED))

        assertEquals(ReportStatus.RESOLVED, dao.getAllReports().getOrAwaitValue()[0].status)
    }

    @Test
    fun deleteReport_removesIt() = runBlocking<Unit> {
        dao.insertReport(sampleReport("Blocked drain"))
        val saved = dao.getAllReports().getOrAwaitValue()[0]

        dao.deleteReport(saved)

        assertEquals(0, dao.getAllReports().getOrAwaitValue().size)
    }

    @Test
    fun filterReports_byCategory() = runBlocking<Unit> {
        dao.insertReport(sampleReport("Pothole", ReportCategory.POTHOLE))
        dao.insertReport(sampleReport("Drain", ReportCategory.DRAINAGE))

        val drains = dao.filterReports(null, ReportCategory.DRAINAGE).getOrAwaitValue()

        assertEquals(1, drains.size)
        assertEquals("Drain", drains[0].title)
    }
}