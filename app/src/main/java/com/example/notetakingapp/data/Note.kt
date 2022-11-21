package com.example.notetakingapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "category")
    val category: String? = null,
    @ColumnInfo(name = "creation_date")
    val creationDate: Long = 0,
    @ColumnInfo(name = "last_edited")
    val lastEdited: Long = 0
)
