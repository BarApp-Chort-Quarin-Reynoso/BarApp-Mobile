package com.barapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.model.Opinion

class PantallaOpinionesViewModel : ViewModel() {

    private val restauranteRepository: RestauranteRepository = RestauranteRepository.instance

    private val _opinionesRestaurante = MutableLiveData<MutableList<Opinion>>()
    val opinionesRestaurante = _opinionesRestaurante

    private val _loading = MutableLiveData<Boolean>()
    val loading = _loading

    private val _error = MutableLiveData<Throwable>()
    val error = _error

    fun buscarOpiniones(idRestaurante: String) {
        _loading.value = true
        restauranteRepository.buscarOpinionesRestaurante(
            idRestaurante,
            object : com.barapp.data.utils.FirestoreCallback<List<Opinion>> {
                override fun onSuccess(result: List<Opinion>) {
                    _loading.postValue(false)
                    _opinionesRestaurante.postValue(result.toMutableList())
                }

                override fun onError(exception: Throwable) {
                    _loading.postValue(false)
                    _error.postValue(exception)
                }
            }
        )
    }
}