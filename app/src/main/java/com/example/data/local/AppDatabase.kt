package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.Citizen
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentPage
import com.example.data.model.SubmissionBatch

@Database(
    entities = [
        Citizen::class,
        DocumentItem::class,
        DocumentPage::class,
        SubmissionBatch::class,
        ActivityLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
    abstract fun documentDao(): DocumentDao
    abstract fun submissionDao(): SubmissionDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "up_file_manager.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
