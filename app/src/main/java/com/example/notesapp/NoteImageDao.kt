package com.example.notesapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoteImageDao {
    @Query("SELECT * FROM note_images WHERE noteId LIKE :noteId")
    fun getAllForNote(noteId: Int): List<NoteImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteImages: List<NoteImage>)

    @Query("DELETE FROM note_images WHERE id in (:noteImageIdList)")
    suspend fun deleteImages(noteImageIdList: List<Int>)

    @Query("DELETE FROM note_images WHERE noteId LIKE :noteId")
    suspend fun deleteForNote(noteId: Int)
}