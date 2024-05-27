package com.example.digitaldiary

import android.net.Uri
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

    private val _currentNote: MutableLiveData<Note> = MutableLiveData()
    val currentNote: LiveData<Note> = _currentNote

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
            _notes.postValue(noteDao.getAll())
        }
    }
    private fun refreshNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentNote.postValue(noteDao.get(id))
        }
    }

    fun navigateEditNote(note: Note?) {
        _navigateTo.value = Event(Destination.NOTE)
        note?.let { _currentNote.value = it }
    }

    fun navigateCreateNote() {
        _navigateTo.value = Event(Destination.NOTE)
        _currentNote.value = Note(
            title = "",
            content = "",
            date = LocalDate.now(),
            location = "",
            imageUri = "",
            audioUri = ""
        )
    }


    fun saveNote(title: String, content: String, activity: MainActivity) {
        if (title.isBlank()) throw IllegalArgumentException(activity.getString(R.string.empty_title_error))
        activity.getLocation { location ->
            val note: Note
            if (currentNote.value?.id?.toInt() == 0) {
                note = Note(
                    title = title,
                    content = content,
                    date = LocalDate.now(),
                    location = location,
                    imageUri = currentNote.value?.imageUri,
                    audioUri = currentNote.value?.audioUri
                )
                createNote(note)
            } else {
                note = Note(
                    currentNote.value!!.id,
                    title,
                    content,
                    LocalDate.now(),
                    location,
                    currentNote.value?.imageUri,
                    currentNote.value?.audioUri
                )
                updateNote(note)
            }
        }
    }

    private fun createNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val id = noteDao.insert(note)
            refreshNote(id)
            refreshNotes()
        }
    }

    private fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            noteDao.update(note)
            refreshNote(note.id)
            refreshNotes()
        }
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

    fun navigateToPaint() {
        _navigateTo.value = Event(Destination.PAINT)
    }

    fun navigateToAudio() {
        _navigateTo.value = Event(Destination.AUDIO)
    }

    fun attachImage(uri: Uri) {
        _currentNote.value = _currentNote.value?.copy(imageUri = uri.toString())
    }
    fun attachAudio(uri: Uri) {
        _currentNote.value = _currentNote.value?.copy(audioUri = uri.toString())
    }

    fun removeImage() {
        _currentNote.value = _currentNote.value?.copy(imageUri = null)
    }

    fun removeAudio() {
        _currentNote.value = _currentNote.value?.copy(audioUri = null)
    }

    enum class Destination {
        NOTE, PAINT, AUDIO
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
    }
}
