package com.barapp.data.entities

import com.barapp.model.EstadoRestaurante
import java.util.UUID

class RestauranteEntity(
  var nombre: String,
  var estado: EstadoRestaurante,
  var puntuacion: Double,
  var cantidadOpiniones: Int,
  var portada: String,
  var correo: String,
  var logo: String,
  var idDetalleRestaurante: String,
) {
  constructor() : this("", EstadoRestaurante.ESPERANDO_HABILITACION, -1.0, 0, "", "", "", "")

  override fun toString(): String {
    return "RestauranteEntity(nombre='$nombre', estado='$estado', puntuacion=$puntuacion, cantidadOpiniones=$cantidadOpiniones, portada='$portada', correo='$correo', logo='$logo', idDetalleRestaurante='$idDetalleRestaurante')"
  }
}
