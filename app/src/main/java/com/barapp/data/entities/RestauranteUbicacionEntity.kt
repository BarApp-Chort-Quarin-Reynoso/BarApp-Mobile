package com.barapp.data.entities

import com.barapp.model.EstadoRestaurante
import java.util.UUID

open class RestauranteUbicacionEntity(
  var idRestaurante: String,
  var nombre: String,
  var estado: EstadoRestaurante,
  var puntuacion: Double,
  var portada: String,
  var correo: String,
  var logo: String,
  var idDetalleRestaurante: String,
  var idUbicacion: String,
  var calle: String,
  var numero: Int,
  var latitud: Double,
  var longitud: Double,
  var nombreCiudad: String,
  var nombreProvincia: String,
  var nombrePais: String,
) {
  constructor() :
    this(UUID.randomUUID().toString(), "", EstadoRestaurante.ESPERANDO_HABILITACION, -1.0, "", "", "", "", "", "", -1, -1.0, -1.0, "", "", "")

  override fun toString(): String {
    return "RestauranteUbicacionEntity(idRestaurante='$idRestaurante', nombre='$nombre', estado='$estado', puntuacion=$puntuacion, portada='$portada', correo='$correo', logo='$logo', idDetalleRestaurante='$idDetalleRestaurante', idUbicacion='$idUbicacion', calle='$calle', numero=$numero, latitud=$latitud, longitud=$longitud, nombreCiudad='$nombreCiudad', nombreProvincia='$nombreProvincia', nombrePais='$nombrePais')"
  }
}
