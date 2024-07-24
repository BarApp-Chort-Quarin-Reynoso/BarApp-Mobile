package com.barapp.model

import com.google.firebase.Timestamp
import java.util.UUID

class RestauranteUsuario(
    id: String,
    nombre: String,
    puntuacion: Double,
    portada: String,
    correo: String,
    logo: String,
    ubicacion: Ubicacion,
    idDetalleRestaurante: String,
    detalleRestaurante: DetalleRestaurante?,
    idRestaurante: String,
) : Restaurante(
    id,
    nombre,
    puntuacion,
    portada,
    correo,
    logo,
    ubicacion,
    idDetalleRestaurante,
    detalleRestaurante,
    idRestaurante,
) {
    var idRestauranteUsuario: String = UUID.randomUUID().toString()
    var fechaGuardado: String = Timestamp.now().toString()
    var idUsuario: String = ""

    constructor() : this(UUID.randomUUID().toString(),
        "",
        -1.0,
        "",
        "",
        "",
        Ubicacion(),
        "",
        null,
        "",
    )
}