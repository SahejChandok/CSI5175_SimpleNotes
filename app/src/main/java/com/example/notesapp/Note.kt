package com.example.notesapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note(
  @PrimaryKey(autoGenerate = true) var id: Int?,
  @ColumnInfo(name = "title") val title: String?,
  @ColumnInfo(name = "note") val note: String?,
  @ColumnInfo(name = "date") val date: String?,
  @ColumnInfo(name= "isPush", defaultValue = "false") val isPush: Boolean?
) : java.io.Serializable
