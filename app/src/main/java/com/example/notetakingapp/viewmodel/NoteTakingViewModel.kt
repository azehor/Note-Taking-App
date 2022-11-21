package com.example.notetakingapp.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.notetakingapp.data.Note
import com.example.notetakingapp.data.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "NoteTakingViewModel"

enum class FilterType { CATEGORY, CONTENTS, NONE }

class NoteTakingViewModel(private val noteDao: NoteDao) : ViewModel() {

    class NoteTakingViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteTakingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteTakingViewModel(noteDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val notes: LiveData<List<Note>> = noteDao.getNotes().asLiveData()

    val noteCount: LiveData<Int> = noteDao.getCount().asLiveData()

    val noteCategories: LiveData<List<String>> = noteDao.getCategories().asLiveData()

    private val filterContent = MutableLiveData<String>("")
    private var filterType = FilterType.NONE

    private var selectedCategory = ""

    val displayedNotes: LiveData<List<Note>>
        get() = Transformations.switchMap(filterContent) { filter ->
            val noteList = notes.switchMap { noteList ->
                val filteredNotes = MutableLiveData<List<Note>>()
                val filteredList = when (filterType) {
                    FilterType.CATEGORY -> {
                        noteList.filter { note ->
                            note.category == filter
                        }
                    }
                    FilterType.CONTENTS -> {
                        noteList.filter { note ->
                            note.body.contains(filter, true) ||
                                    note.title.contains(filter, true)
                        }
                    }
                    else -> {
                        noteList
                    }
                }
                filteredNotes.value = filteredList
                filteredNotes
            }
            noteList
        }

    private val selectedNotes = mutableListOf<Note>()


    private fun insertNote(note: Note) {
        viewModelScope.launch {
            noteDao.insert(note)
        }
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch {
            noteDao.update(note)
        }
    }

    private fun getNewNote(
        noteTitle: String,
        noteBody: String,
        noteCategory: String?
    ): Note {
        return Note(
            title = noteTitle,
            body = noteBody,
            category = noteCategory
        )
    }


    private fun getUpdatedNote(
        noteId: Int,
        noteTitle: String,
        noteBody: String,
        noteCategory: String?
    ): Note {
        return Note(
            id = noteId,
            title = noteTitle,
            body = noteBody,
            category = noteCategory
        )
    }

    private fun filterNotesByCategory(filter: String) {
        if(filterType != FilterType.CONTENTS) {
            filterType = if(filter.isBlank()){
                FilterType.NONE
            } else {
                FilterType.CATEGORY
            }
            filterContent.value = filter
        }
        else{
            Log.d(TAG, "Tried filtering by category but content takes precedence")
        }
        selectedCategory = filter
    }


    private fun filterNotesByContent(filter: String?) {
        if(filter.isNullOrBlank()){
            filterType = if(selectedCategory.isBlank()) {
                FilterType.NONE
            } else {
                FilterType.CATEGORY
            }
            filterContent.value = selectedCategory
        } else {
            filterType = FilterType.CONTENTS
            filterContent.value = filter!!
        }
    }

    fun getNote(id: Int): LiveData<Note> {
        return noteDao.getNote(id).asLiveData()
    }

    fun updateNote(noteId: Int, noteTitle: String, noteBody: String, noteCategory: String) {
        val updatedNote = if (noteCategory.isBlank()) {
            getUpdatedNote(noteId, noteTitle.trim(), noteBody.trim(), null)
        } else {
            getUpdatedNote(noteId, noteTitle.trim(), noteBody.trim(), noteCategory.trim())
        }
        updateNote(updatedNote)
    }

    fun addNewNote(noteTitle: String, noteBody: String, noteCategory: String) {
        val newNote = if (noteCategory.isBlank()) {
            getNewNote(noteTitle.trim(), noteBody.trim(), null)
        } else {
            getNewNote(noteTitle.trim(), noteBody.trim(), noteCategory.trim())
        }
        insertNote(newNote)
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }

    fun deleteSelectedNotes(){
        Log.d(TAG, "List of notes to delete: $selectedNotes")
        val aux: List<Note> = selectedNotes.toList()
        removeAllSelectedNotes()
        viewModelScope.launch {
            noteDao.deleteMultiple(aux)
        }
    }

    fun addSelectedNote(note: Note){
        selectedNotes.add(note)
    }
    fun removeSelectedNote(note: Note){
        selectedNotes.remove(note)
    }
    fun removeAllSelectedNotes(){
        while(selectedNotes.isNotEmpty()){
            selectedNotes.removeAt(0)
        }
    }
    fun selectedNotesSize(): Int{
        return selectedNotes.size
    }

    fun isEntryValid(noteTitle: String, noteBody: String): Boolean {
        if (noteTitle.isBlank() && noteBody.isBlank()) {
            return false
        }
        return true
    }

    fun filterNotes(filterType: FilterType, filter: String?) {
        if (filterType == FilterType.CATEGORY) {
            filterNotesByCategory(filter!!)
        } else if (filterType == FilterType.CONTENTS) {
            filterNotesByContent(filter)
        }
    }


}
