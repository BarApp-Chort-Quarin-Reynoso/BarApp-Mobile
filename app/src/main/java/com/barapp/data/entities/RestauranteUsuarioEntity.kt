package com.barapp.data.entities

import com.barapp.model.EstadoRestaurante
import com.google.firebase.Timestamp
import java.util.UUID

class RestauranteUsuarioEntity(
  idRestaurante: String,
  nombre: String,
  estado: EstadoRestaurante,
  puntuacion: Double,
  portada: String,
  correo: String,
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
    estado,
    puntuacion,
    portada,
    correo,
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
  var idRestauranteUsuario: String = UUID.randomUUID().toString()
  var fechaGuardado: String = Timestamp.now().toString()

  constructor() :
    this(
      UUID.randomUUID().toString(),
      "",
      EstadoRestaurante.ESPERANDO_HABILITACION,
      -1.0,
      "",
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
