package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.drive.DriveManager
import com.example.data.local.AppDatabase
import com.example.data.model.Citizen
import com.example.data.model.DocumentItem
import com.example.data.repository.CitizenRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CitizenRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val driveManager = DriveManager(context)
        val settingsRepo = SettingsRepository(context)
        repository = CitizenRepository(
            citizenDao = db.citizenDao(),
            documentDao = db.documentDao(),
            submissionDao = db.submissionDao(),
            activityLogDao = db.activityLogDao(),
            driveManager = driveManager,
            settingsRepository = settingsRepo
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("UP File Manager", appName)
    }

    @Test
    fun testSequentialFileIdGeneration() = runBlocking {
        val id1 = repository.generateNextFileId("জন্ম নিবন্ধন")
        assertTrue(id1.startsWith("BR-"))
        assertTrue(id1.endsWith("-00001"))

        // Create first citizen
        val c1Id = repository.createCitizen(
            fullName = "মোঃ রহিম",
            fatherMotherName = "পিতা: মোঃ করিম",
            dateOfBirth = "01-01-2020",
            mobileNumber = "01819000000",
            address = "রামপুর",
            wardNo = "০১",
            village = "রামপুর",
            serviceType = "জন্ম নিবন্ধন",
            applicationDate = "01-10-2026",
            remarks = "টেস্ট নোট"
        )
        assertTrue(c1Id > 0)

        // Generate next ID
        val id2 = repository.generateNextFileId("জন্ম নিবন্ধন")
        assertTrue(id2.endsWith("-00002"))
    }

    @Test
    fun testCitizenAndDocumentCreation() = runBlocking {
        val citizenId = repository.createCitizen(
            fullName = "আয়েশা সিদ্দিকা",
            fatherMotherName = "পিতা: সামসুল হক",
            dateOfBirth = "10-10-2022",
            mobileNumber = "01711000000",
            address = "চর রামপুর",
            wardNo = "০২",
            village = "চর রামপুর",
            serviceType = "জন্ম নিবন্ধন",
            applicationDate = "01-10-2026",
            remarks = ""
        )

        val citizen = repository.getCitizenById(citizenId).first()
        assertNotNull(citizen)
        assertEquals("আয়েশা সিদ্দিকা", citizen?.fullName)
        assertEquals(Citizen.STATUS_COLLECTING, citizen?.status)

        // Verify default documents were created
        val docs = repository.getDocumentsForCitizen(citizenId)
        assertTrue(docs.isNotEmpty())
        assertEquals(DocumentItem.DEFAULT_REQUIRED_DOCUMENTS.size, docs.size)

        // Verify activity log was recorded
        val logs = repository.getActivityLogs(citizenId).first()
        assertTrue(logs.isNotEmpty())
        assertTrue(logs.first().action.contains("ফাইল তৈরি করা হয়েছে"))
    }

    @Test
    fun testStandardDriveFileNameGeneration() {
        val driveManager = DriveManager(context)
        val fileName = driveManager.generateStandardFileName(
            fileId = "BR-2026-00125",
            documentType = "পিতা/মাতার NID",
            citizenName = "Md Rahim",
            pageNumber = 1
        )
        assertTrue(fileName.startsWith("BR-2026-00125_Parent-NID_Md-Rahim_"))
        assertTrue(fileName.endsWith(".jpg"))
    }
}
