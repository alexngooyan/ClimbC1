package com.example.climbc1.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "DUDE WHAT THE FUCK GIVE ME A RECTAJNVLE"
    }
    val text: LiveData<String> = _text
}