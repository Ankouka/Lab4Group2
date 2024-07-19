package edu.msudenver.cs3013.lab4group2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel(){
    private val _parkingLocation = MutableLiveData<String>()
    val parkingLocation: LiveData<String> get() = _parkingLocation

    fun updateParkingLocation(location: String) {
        _parkingLocation.value = location
    }
}