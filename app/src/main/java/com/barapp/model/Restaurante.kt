package com.barapp.model

import java.util.UUID

class Restaurante(
  id: String,
  var nombre: String,
  var puntuacion: Double,
  var foto: String,
  var logo: String,
  var ubicacion: Ubicacion,
  var idDetalleRestaurante: String,
  var detalleRestaurante: DetalleRestaurante?,
) : BaseClass(id) {

  constructor() : this(UUID.randomUUID().toString(), "", -1.0, "", "", Ubicacion(), "", null)

  constructor(
    nombre: String,
    puntuacion: Double,
    foto: String,
    logo: String,
    ubicacion: Ubicacion,
    detalleRestaurante: DetalleRestaurante,
  ) : this(
    UUID.randomUUID().toString(),
    nombre,
    puntuacion,
    foto,
    logo,
    ubicacion,
    detalleRestaurante.id,
    detalleRestaurante,
  )

  override fun toString(): String {
    return "Restaurante{" +
      "id='" +
      id +
      '\'' +
      ", nombre='" +
      nombre +
      '\'' +
      ", puntuacion=" +
      puntuacion +
      ", foto='" +
      foto +
      '\'' +
      ", logo='" +
      logo +
      '\'' +
      ", detalleRestaurante=" +
      detalleRestaurante +
      ", ubicacion=" +
      ubicacion +
      '}'
  }

  fun copy(detalleRestaurante: DetalleRestaurante): Restaurante {
    return Restaurante(
      id,
      nombre,
      puntuacion,
      foto,
      logo,
      ubicacion,
      idDetalleRestaurante,
      detalleRestaurante,
    )
  }
}
