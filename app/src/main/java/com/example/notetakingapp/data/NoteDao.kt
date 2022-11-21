package com.example.notetakingapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM note WHERE id = :id")
    fun getNote(id: Int): Flow<Note>

    @Query("SELECT * FROM note ORDER BY last_edited ASC")
    fun getNotes(): Flow<List<Note>>

    @Query("SELECT DISTINCT category FROM note WHERE category IS NOT NULL ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT COUNT(id) FROM note")
    fun getCount(): Flow<Int>

    @Delete
    suspend fun deleteMultiple(notes: List<Note>)
}