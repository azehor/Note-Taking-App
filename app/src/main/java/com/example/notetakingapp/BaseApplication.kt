package com.example.notetakingapp

import android.app.Application
import com.example.notetakingapp.data.NoteDatabase

class BaseApplication: Application() {
    val database: NoteDatabase by lazy { NoteDatabase.getDatabase(this) }
}