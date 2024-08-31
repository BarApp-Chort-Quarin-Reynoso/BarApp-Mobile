package com.barapp.viewModels.authViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.model.Usuario
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.UsuarioRepository
import com.barapp.data.utils.FirestoreCallback
import com.hadilq.liveevent.LiveEvent

class PantallaSignUpViewModel: ViewModel() {
  private val usuarioRepository = UsuarioRepository.instance
  private val detalleUsuarioRepository = DetalleUsuarioRepository.instance

  private val _error: LiveEvent<Throwable> = LiveEvent()
  val error: LiveData<Throwable> = _error

  private val _loading: MutableLiveData<Boolean> = MutableLiveData()
  val loading: LiveData<Boolean> = _loading

  private fun guardarUsuario(usuario: Usuario, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
    usuario.detalleUsuario!!.id = usuario.idDetalleUsuario
    detalleUsuarioRepository.guardar(usuario.detalleUsuario!!)
    usuarioRepository.guardar(usuario)
    onSuccess()
  }

  fun crearUsuarioEnFirebaseAuth(email: String, contrasenia: String, usuario: Usuario) {
    _loading.value = true
    usuarioRepository.crearUsuarioEnFirebaseAuth(email, contrasenia, object : FirestoreCallback<String> {
      override fun onSuccess(result: String) {
        usuario.id = result
        guardarUsuario(usuario, {
          _loading.value = false
        }, { exception ->
          _error.value = exception
          _loading.value = false
        })
      }

      override fun onError(exception: Throwable) {
        _error.value = exception
        _loading.value = false
      }
    })
  }
}
