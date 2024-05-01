package com.barapp.viewModels.authViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.data.repositories.UsuarioRepository
import com.barapp.data.utils.FirestoreCallback
import com.hadilq.liveevent.LiveEvent

class PantallaLoginViewModel : ViewModel() {
  private val usuarioRepository = UsuarioRepository.instance

  private val _idUsuario: MutableLiveData<String> = MutableLiveData()
  val idUsuario: LiveData<String> = _idUsuario

  private val _error: LiveEvent<Throwable> = LiveEvent()
  val error: LiveData<Throwable> = _error

  fun signInUsuarioEnFirebase(email: String, contrasenia: String) {
    usuarioRepository.signInUsuarioEnFirebase(email, contrasenia, object : FirestoreCallback<String> {
      override fun onSuccess(result: String) {
        _idUsuario.value = result
      }

      override fun onError(exception: Throwable) {
        _error.value = exception
      }
    })
  }

  fun updateIdUsuario(id: String) {
    _idUsuario.value = id
  }
}