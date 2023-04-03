package com.example.notesapp

import androidx.room.Database

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            val temp = INSTANCE
            if(temp != null) {
                return temp
            }
            // if no database initialised then create one
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}