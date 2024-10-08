package com.barapp.viewModels

import android.location.Location
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.barapp.model.Reserva
import com.barapp.data.entities.RestauranteInfoQR
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.ReservaRepository
import com.barapp.data.repositories.RestauranteVistoRecientementeRepository
import com.barapp.data.repositories.UsuarioRepository
import com.barapp.model.DetalleRestaurante
import com.barapp.model.DetalleUsuario
import com.barapp.util.Event
import timber.log.Timber

class MainActivityViewModel(var origen: String?) : ViewModel() {

  lateinit var reserva: Reserva

  private val usuarioRepository = UsuarioRepository.instance
  private val detalleUsuarioRepository = DetalleUsuarioRepository.instance
  private val restauranteVistoRecientementeRepository =
    RestauranteVistoRecientementeRepository.instance
  private val reservaRepository = ReservaRepository.instance

  private val _ubicacionUsuario: MutableLiveData<Location> = MutableLiveData()
  val ubicacionUsuario: LiveData<Location> = _ubicacionUsuario

  private val _usuario: MutableLiveData<Usuario> = MutableLiveData()
  val usuario: LiveData<Usuario> = _usuario

  private val _reservaLD: MutableLiveData<Reserva> = MutableLiveData()
  val reservaLD: LiveData<Reserva> = _reservaLD

  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  private val _commonMessages = MutableLiveData<Event<String>>()
  val commonMessages: LiveData<Event<String>> = _commonMessages

  /**
   * Busca el usuario y su detalle a partir de un idUsuario
   *
   * @author Valentin Reynoso
   */
  fun buscarUsuarioPorId(idUsuario: String) {

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

  fun guardarFcmToken(usuario: Usuario, fcmtoken: String?) {
    if (fcmtoken != null && fcmtoken != "") {
      if (!usuario.fcmTokens.contains(fcmtoken)) {
        usuario.fcmTokens.add(fcmtoken)
        usuarioRepository.actualizar(usuario)
      }
    } else {
      Timber.e(Throwable("fcmtoken no puede ser null ni vacio"))
    }
  }

  fun eliminarFcmToken(fcmtoken: String?) {
    if (usuario.value != null && fcmtoken != null) {
      usuario.value!!.fcmTokens.remove(fcmtoken)
      usuarioRepository.actualizar(usuario.value!!)
    }
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

  fun setReservaSync(reserva: Reserva) {
    _reservaLD.postValue(reserva)
  }

  fun searchReserva(idReserva: String) {
    reservaRepository.buscarPorId(idReserva, object : FirestoreCallback<Reserva> {
      override fun onSuccess(result: Reserva) {
        _reservaLD.postValue(result)
      }

      override fun onError(exception: Throwable) {
        _error.postValue(exception)
      }
    })
  }

  fun cancelarReserva(onComplete: () -> Unit) {
    reservaLD.value?. let {
      reservaRepository.cancelarReserva(it, object : FirestoreCallback<Reserva> {
        override fun onSuccess(result: Reserva) {
          onComplete()
        }

        override fun onError(exception: Throwable) {
          _error.postValue(exception)
          onComplete()
        }
      })
    }
  }

  fun concretarReserva(idUsuario: String, restauranteInfoQR: RestauranteInfoQR, callback: FirestoreCallback<Reserva>) {
    reservaRepository.concretarReserva(reservaLD.value!!.id, idUsuario, restauranteInfoQR, object : FirestoreCallback<Reserva> {
      override fun onSuccess(result: Reserva) {
        callback.onSuccess(result)
      }

      override fun onError(exception: Throwable) {
        callback.onError(exception)
      }
    })
  }

  fun postMessage(source: Fragment, message: String) {
    _commonMessages.postValue(Event(source, message))
  }

  class Factory(private val origen: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST") return MainActivityViewModel(origen) as T
      }

      throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
    }
  }
}
