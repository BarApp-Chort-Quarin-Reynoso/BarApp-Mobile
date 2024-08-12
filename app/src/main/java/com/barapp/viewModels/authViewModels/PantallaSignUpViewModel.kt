package com.barapp.viewModels.authViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.model.Usuario
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.UsuarioRepository
import com.barapp.data.utils.FirestoreCallback
import com.hadilq.liveevent.LiveEvent

class PantallaSignUpViewModel: ViewModel() {
  private val usuarioRepository = UsuarioRepository.instance
  private val detalleUsuarioRepository = DetalleUsuarioRepository.instance

  private val _idUsuario: MutableLiveData<String> = MutableLiveData()
  val idUsuario: LiveData<String> = _idUsuario

  private val _error: LiveEvent<Throwable> = LiveEvent()
  val error: LiveData<Throwable> = _error

  private fun guardarUsuario(usuario: Usuario) {
    usuario.detalleUsuario!!.id = usuario.idDetalleUsuario
    detalleUsuarioRepository.guardar(usuario.detalleUsuario!!)
    usuarioRepository.guardar(usuario)
  }

  fun crearUsuarioEnFirebaseAuth(email: String, contrasenia: String, usuario: Usuario) {
    usuarioRepository.crearUsuarioEnFirebaseAuth(email, contrasenia, object : FirestoreCallback<String> {
      override fun onSuccess(result: String) {
        usuario.id = result
        guardarUsuario(usuario)
      }

      override fun onError(exception: Throwable) {
        _error.value = exception
      }
    })
  }
}
