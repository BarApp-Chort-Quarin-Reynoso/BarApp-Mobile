package com.barapp.data.entities

import com.barapp.data.entities.RestauranteUbicacionEntity
import com.google.firebase.Timestamp
import java.util.UUID

class RestauranteUsuarioEntity(
  idRestaurante: String,
  nombre: String,
  puntuacion: Double,
  foto: String,
  logo: String,
  idDetalleRestaurante: String,
  idUbicacion: String,
  calle: String,
  numero: Int,
  latitud: Double,
  longitud: Double,
  nombreCiudad: String,
  nombreProvincia: String,
  nombrePais: String,
  var idUsuario: String,
) :
  RestauranteUbicacionEntity(
    idRestaurante,
    nombre,
    puntuacion,
    foto,
    logo,
    idDetalleRestaurante,
    idUbicacion,
    calle,
    numero,
    latitud,
    longitud,
    nombreCiudad,
    nombreProvincia,
    nombrePais,
  ) {
  var idRestauranteUsuario: String = idRestaurante + idUsuario
  var fechaGuardado: Timestamp = Timestamp.now()

  constructor() :
    this(
      UUID.randomUUID().toString(),
      "",
      -1.0,
      "",
      "",
      "",
      "",
      "",
      -1,
      -1.0,
      -1.0,
      "",
      "",
      "",
      UUID.randomUUID().toString(),
    )

  override fun toString(): String {
    return "RestauranteUsuarioEntity(idUsuario='$idUsuario', idRestauranteUsuario='$idRestauranteUsuario', fechaGuardado=$fechaGuardado)"
  }
}
