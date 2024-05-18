package com.example.digitaldiary.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val editNavigation: MutableLiveData<Boolean> = MutableLiveData()

    fun onEntryClicked() {
        editNavigation.value = true
    }

    fun onBackClicked(): Boolean {
        editNavigation.value = false
        return editNavigation.value!!
    }
}