package com.example.digitaldiary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldiary.model.Note
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ViewModel : ViewModel() {
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    private val noteDao = MainActivity.db.noteDao()

    private val _currentNote: MutableLiveData<Note?> = MutableLiveData()
    val currentNote: LiveData<Note?> = _currentNote

    private val _notes: MutableLiveData<List<Note>> = MutableLiveData()
    val notesLiveData: LiveData<List<Note>> = _notes

    private val _navigateTo: MutableLiveData<Event<Destination>> = MutableLiveData()
    val navigateTo: LiveData<Event<Destination>> = _navigateTo

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        showErrorMessage(e.message!!)
    }

    init {
        refreshNotes()
    }

    private fun refreshNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val allNotes = noteDao.getAll()
            _notes.postValue(allNotes)
        }
    }

    fun navigateEditNote(note: Note) {
        _navigateTo.value = Event(Destination.NOTE)
        _currentNote.value = note
    }

    fun navigateCreateNote() {
        _navigateTo.value = Event(Destination.NOTE)
        _currentNote.value = null
    }


    fun saveNote(title: String, content: String, activity: MainActivity) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")
        activity.getLocation { location ->
            val note: Note
            if (currentNote.value == null) {
                note = Note(
                    title = title, content = content, date = LocalDate.now(), location = location
                )
                createNote(note)
            } else {
                note = Note(currentNote.value!!.id, title, content, LocalDate.now(), location)
                updateNote(note)
            }
            _currentNote.value = note
        }
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

    enum class Destination {
        MAIN, NOTE, PAINT, AUDIO
    }

    class Event<out T>(private val content: T) {

        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }
}
