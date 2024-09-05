package com.barapp.model

import java.util.UUID

open class Restaurante(
  id: String,
  var nombre: String,
  var estado: EstadoRestaurante,
  var puntuacion: Double,
  var cantidadOpiniones: Int,
  var portada: String,
  var correo: String,
  var logo: String,
  var ubicacion: Ubicacion,
  var idDetalleRestaurante: String,
  var detalleRestaurante: DetalleRestaurante?,
  var idRestaurante: String = ""
) : BaseClass(id) {

  constructor() : this(UUID.randomUUID().toString(), "", EstadoRestaurante.ESPERANDO_HABILITACION, -1.0, 0, "", "", "", Ubicacion(), "", null)

  constructor(
    nombre: String,
    estado: EstadoRestaurante,
    puntuacion: Double,
    cantidadOpiniones: Int,
    portada: String,
    correo: String,
    logo: String,
    ubicacion: Ubicacion,
    detalleRestaurante: DetalleRestaurante,
    idRestaurante: String = ""
  ) : this(
    UUID.randomUUID().toString(),
    nombre,
    estado,
    puntuacion,
    cantidadOpiniones,
    portada,
    correo,
    logo,
    ubicacion,
    detalleRestaurante.id,
    detalleRestaurante,
    idRestaurante
  )

  fun copy(detalleRestaurante: DetalleRestaurante): Restaurante {
    return Restaurante(
      id,
      nombre,
      estado,
      puntuacion,
      cantidadOpiniones,
      portada,
      correo,
      logo,
      ubicacion,
      idDetalleRestaurante,
      detalleRestaurante,
      idRestaurante
    )
  }

  override fun toString(): String {
    return "Restaurante(nombre='$nombre', estado=$estado, puntuacion=$puntuacion, cantidadOpiniones=$cantidadOpiniones, portada='$portada', correo='$correo', logo='$logo', ubicacion=$ubicacion, idDetalleRestaurante='$idDetalleRestaurante', detalleRestaurante=$detalleRestaurante, idRestaurante='$idRestaurante')"
  }
}
