package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.barapp.model.Reserva
import com.barapp.data.repositories.DetalleRestauranteRepository
import com.barapp.data.repositories.ReservaRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Opinion
import com.barapp.model.Usuario
import timber.log.Timber

class PantallaCrearOpinionViewModel(var reserva: Reserva) : ViewModel() {
    private val detalleRestauranteRepo: DetalleRestauranteRepository = DetalleRestauranteRepository.instance
    private val reservaRepository: ReservaRepository = ReservaRepository.instance

    private val _detalleRestaurante: MutableLiveData<DetalleRestaurante> = MutableLiveData()
    val detalleRestaurante: LiveData<DetalleRestaurante> = _detalleRestaurante

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading

    private val _error: MutableLiveData<Throwable?> = MutableLiveData()
    val error: LiveData<Throwable?> = _error

    init {
        buscarDetalleRestaurante()
    }

    private fun buscarDetalleRestaurante() {
        _loading.value = true

        Timber.d("Buscando detalle restaurante de la reserva: $reserva")

        detalleRestauranteRepo.buscarPorId(
             reserva.restaurante.id,
            object : FirestoreCallback<DetalleRestaurante> {
                override fun onSuccess(result: DetalleRestaurante) {
                    _loading.postValue(false)
                    _detalleRestaurante.postValue(result)
                }

                override fun onError(exception: Throwable) {
                    _loading.postValue(false)
                    _error.postValue(exception)
                }
            },
        )
    }

    fun crearYEnviarOpinion(comentario: String, nota: Double, usuario: Usuario, caracteristicasValoradas: Map<String, Float>) {
        _loading.value = true

        val opinion = Opinion(
            comentario,
            nota,
            usuario,
            reserva.restaurante.id,
            reserva.horario,
            reserva.fecha,
            reserva.cantidadPersonas,
            caracteristicasValoradas
        )

        reservaRepository.enviarOpinion(reserva.id, opinion, object : FirestoreCallback<Opinion> {
            override fun onSuccess(result: Opinion) {
                _loading.postValue(false)
                reserva.idOpinion = result.id
            }

            override fun onError(exception: Throwable) {
                _loading.postValue(false)
                _error.postValue(exception)
            }
        })
    }

    class Factory(private val reserva: Reserva) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PantallaCrearOpinionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return PantallaCrearOpinionViewModel(reserva) as T
            }

            throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
        }
    }
}