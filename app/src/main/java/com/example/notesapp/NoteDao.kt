package com.example.notesapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY notes.id DESC")
    fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE id LIKE :id LIMIT 1")
    suspend fun findById(id: Int): Note

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long

    @Delete
    suspend fun remove(note: Note)

    @Update
    suspend fun update(note: Note)
}