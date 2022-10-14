package de.hsfl.kevinblaue.musicrun.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainMenuViewModel : ViewModel() {
    val bluetoothEnabled = MutableLiveData(false)
    val beltConnected = MutableLiveData(false)
}