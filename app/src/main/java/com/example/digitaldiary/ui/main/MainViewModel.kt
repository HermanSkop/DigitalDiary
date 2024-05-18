package com.example.digitaldiary.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val editNavigation: LiveData<Boolean> = MutableLiveData()

    fun onEntryClicked(view: android.widget.Button) {
        (editNavigation as MutableLiveData).value = true
    }
}