package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = Citizen::class,
            parentColumns = ["id"],
            childColumns = ["citizenId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["citizenId"]),
        Index(value = ["timestamp"])
    ]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val citizenId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String, // e.g. "File Created", "Documents Added", "Status Changed"
    val details: String = ""
)
