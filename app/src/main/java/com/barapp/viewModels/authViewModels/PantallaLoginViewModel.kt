package com.barapp.viewModels.authViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.data.repositories.UsuarioRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.ui.AuthActivity
import com.hadilq.liveevent.LiveEvent
import timber.log.Timber

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

//    FirebaseAuth.getInstance()
//      .signInWithEmailAndPassword(email, contrasenia)
//      .addOnCompleteListener { task ->
//        if (task.isSuccessful) {
//          (activity as AuthActivity?)!!.onLoginExitoso(task.result.user!!.uid)
//        } else {
//          (activity as AuthActivity?)!!.errorAutenticacion()
//          Timber.e(task.exception!!.message)
//        }
//      }
  }
}