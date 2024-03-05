package com.barapp.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PantallaUbicacionBarViewModel : ViewModel() {

  private val _loading: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
  val loading: LiveData<Boolean> = _loading

  var ubicacionUsuario: Location? = null
}
