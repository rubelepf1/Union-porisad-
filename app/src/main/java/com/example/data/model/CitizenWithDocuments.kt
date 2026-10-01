package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class DocumentWithPages(
    @Embedded
    val documentItem: DocumentItem,

    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val pages: List<DocumentPage> = emptyList()
)

data class CitizenWithDocuments(
    @Embedded
    val citizen: Citizen,

    @Relation(
        entity = DocumentItem::class,
        parentColumn = "id",
        entityColumn = "citizenId"
    )
    val documents: List<DocumentItem> = emptyList()
)

data class CitizenFullDetails(
    val citizen: Citizen,
    val documentsWithPages: List<DocumentWithPages> = emptyList(),
    val activityLogs: List<ActivityLog> = emptyList()
)
