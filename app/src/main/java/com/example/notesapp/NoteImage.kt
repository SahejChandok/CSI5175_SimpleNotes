package com.example.notesapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_images")
data class NoteImage (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val uri: String?,
    var noteId: Int?
)