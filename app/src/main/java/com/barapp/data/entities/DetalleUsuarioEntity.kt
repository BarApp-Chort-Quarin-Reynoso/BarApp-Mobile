package com.barapp.data.entities

import java.util.UUID

class DetalleUsuarioEntity(
  var idDetalleUsuario: String,
  var mail: String,
  var telefono: String,
  var busquedasRecientes: List<String>,
  var idRestaurantesFavoritos: List<String>,
) {
  constructor() : this(UUID.randomUUID().toString(), "", "", ArrayList(), ArrayList())

  override fun toString(): String {
    return "DetalleUsuarioEntity(id='$idDetalleUsuario', mail='$mail', telefono='$telefono', busquedasRecientes=$busquedasRecientes, idRestaurantesFavoritos=$idRestaurantesFavoritos)"
  }
}
