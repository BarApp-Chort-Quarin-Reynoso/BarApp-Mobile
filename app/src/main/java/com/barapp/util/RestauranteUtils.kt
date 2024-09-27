package com.barapp.util

import com.barapp.model.Restaurante

object RestauranteUtils {
    // Si el restaurante posee idRestaurante es un RestauranteUsuario (favorito o vistoRecientemente)
    fun getRealIdRestaurante(restaurante: Restaurante): String {
        return if (restaurante.idRestaurante != "") {
            restaurante.idRestaurante
        } else {
            restaurante.id
        }
    }
}