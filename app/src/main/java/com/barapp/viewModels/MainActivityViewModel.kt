package com.barapp.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.barapp.model.Reserva
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteVistoRecientementeRepository
import com.barapp.data.repositories.UsuarioRepository
import com.barapp.model.DetalleUsuario
import java.util.*
import timber.log.Timber

class MainActivityViewModel : ViewModel() {

  lateinit var reserva: Reserva
  private val usuarioRepository = UsuarioRepository.instance
  private val detalleUsuarioRepository = DetalleUsuarioRepository.instance
  private val restauranteVistoRecientementeRepository =
    RestauranteVistoRecientementeRepository.instance

  private val _ubicacionUsuario: MutableLiveData<Location> = MutableLiveData()
  val ubicacionUsuario: LiveData<Location> = _ubicacionUsuario

  private val _usuario: MutableLiveData<Usuario> = MutableLiveData()
  val usuario: LiveData<Usuario> = _usuario

  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  /**
   * Se encarga de buscar en el repositorio el usuario con el id determinado y de guardarlo como
   * atributo para ternerlo disponible en el resto de la aplicación donde se lo necesite
   *
   * @author Valentin Reynoso
   */
  fun buscarYGuardarUsuarioPorId(idUsuario: String) {

    usuarioRepository.buscarPorId(
      idUsuario,
      object : FirestoreCallback<Usuario> {
        override fun onSuccess(result: Usuario) {
          val usuarioTemp = result
          Timber.d(usuarioTemp.toString())
          detalleUsuarioRepository.buscarPorId(
            usuarioTemp.idDetalleUsuario,
            object : FirestoreCallback<DetalleUsuario> {
              override fun onSuccess(result: DetalleUsuario) {
                Timber.d(result.toString())
                usuarioTemp.detalleUsuario = result
                _usuario.postValue(usuarioTemp)
              }

              override fun onError(exception: Throwable) {
                _error.postValue(exception)
                Timber.e(exception)
              }
            },
          )
        }

        override fun onError(exception: Throwable) {
          _error.postValue(exception)
          Timber.e(exception)
        }
      },
    )
  }

  fun guardarRestauranteVistoRecientemente(restaurante: Restaurante) {
    restauranteVistoRecientementeRepository.guardar(restaurante, usuario.value!!.id)
  }

  fun setearUbicacion(ubicacion: Location) {
    _ubicacionUsuario.value = ubicacion
  }

  fun guardarBusquedaReciente(texto: String) {
    usuario.value!!.detalleUsuario?.run {
      this.agregarBusquedaReciente(texto)
      detalleUsuarioRepository.actualizarBusquedasRecientes(this)
    }
  }

  fun eliminarBusquedasRecientes() {
    usuario.value!!.detalleUsuario?.run {
      this.eliminarBusquedasRecientes()
      detalleUsuarioRepository.actualizarBusquedasRecientes(this)
    }
  }

  class Factory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST") return MainActivityViewModel() as T
      }

      throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
    }
  }
}
