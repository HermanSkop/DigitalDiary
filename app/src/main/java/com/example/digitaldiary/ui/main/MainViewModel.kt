package com.example.digitaldiary.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.digitaldiary.MainActivity
import com.example.digitaldiary.database.NoteDao
import com.example.digitaldiary.model.Note

class MainViewModel : ViewModel() {
    val editNavigation: MutableLiveData<Boolean> = MutableLiveData()
    private val noteDao = MainActivity.db.noteDao()
    private val notes : LiveData<List<Note>> = noteDao.getAll()

    fun onEntryClicked() {
        editNavigation.value = true
    }

    fun onBackClicked(): Boolean {
        editNavigation.value = false
        return editNavigation.value!!
    }

    fun getNotes(): LiveData<List<Note>> {
        return notes
    }

    fun insert(note: Note) {
        noteDao.insert(note)
    }

    fun delete(note: Note) {
        noteDao.delete(note)
    }
}