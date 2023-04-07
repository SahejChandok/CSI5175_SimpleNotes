package com.example.notesapp

import androidx.lifecycle.LiveData

class activity_list (private val noteDao: NoteDao){
    val allnotes : LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note){
        noteDao.insert(note)
    }

    suspend fun delete(note: Note){
        noteDao.delete(note)
    }
    suspend fun update(note: Note){
        noteDao.update(note.id, note.title, note.note)
    }
}