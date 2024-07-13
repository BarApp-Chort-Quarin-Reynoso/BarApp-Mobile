package com.barapp.data.entities

import java.util.UUID

class RestauranteEntity(
  var nombre: String,
  var puntuacion: Double,
  var portada: String,
  var correo: String,
  var logo: String,
  var idDetalleRestaurante: String,
) {
  constructor() : this("", -1.0, "", "", "", "")

  override fun toString(): String {
    return "RestauranteEntity(nombre='$nombre', puntuacion=$puntuacion, portada='$portada', correo='$correo', logo='$logo', idDetalleRestaurante='$idDetalleRestaurante')"
  }
}
