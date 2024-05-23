package com.example.digitaldiary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldiary.model.Note
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ViewModel : ViewModel() {
    val editNavigation: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    private val noteDao = MainActivity.db.noteDao()
    var currentNote: Note? = null

    private val _notes: MutableLiveData<List<Note>> = MutableLiveData()
    val notesLiveData: LiveData<List<Note>> = _notes


    init {
        refreshNotes()
    }

    private fun refreshNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val allNotes = noteDao.getAll()
            _notes.postValue(allNotes)
        }
    }

    fun editNote(note: Note? = null) {
        editNavigation.value = true
        if (note != null) currentNote = note
    }

    fun onBackClicked(): Boolean {
        editNavigation.value = false
        currentNote = null
        return editNavigation.value!!
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        showErrorMessage(e.message!!)
    }

    fun saveNote(title: String, content: String) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")
        if (currentNote == null) createNote(
            Note(title = title, content = content, date = LocalDate.now())
        ) else updateNote(
            Note(
                currentNote!!.id, title, content, LocalDate.now()
            )
        )
    }

    private fun createNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            noteDao.insert(note)
        }.invokeOnCompletion { refreshNotes() }
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            noteDao.update(note)
        }.invokeOnCompletion { refreshNotes() }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.delete(note)
        }.invokeOnCompletion { refreshNotes() }
    }

    fun showErrorMessage(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            errorMessage.value = message
        }
    }

}
