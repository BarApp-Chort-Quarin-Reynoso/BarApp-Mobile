package com.barapp.data.entities

import java.util.UUID

class RestauranteEntity(
  var idRestaurante: String,
  var nombre: String,
  var puntuacion: Double,
  var foto: String,
  var logo: String,
  var idDetalleRestaurante: String,
) {
  constructor() : this(UUID.randomUUID().toString(), "", -1.0, "", "", "")

  override fun toString(): String {
    return "RestauranteEntity(idRestaurante='$idRestaurante', nombre='$nombre', puntuacion=$puntuacion, foto='$foto', logo='$logo', idDetalleRestaurante='$idDetalleRestaurante')"
  }
}
