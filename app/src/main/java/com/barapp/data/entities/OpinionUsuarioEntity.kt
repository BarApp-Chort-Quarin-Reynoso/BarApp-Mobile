package com.barapp.data.entities

import java.util.UUID

class OpinionUsuarioEntity(
  var idOpinion: String,
  var comentario: String,
  var nota: Double,
  var idUsuario: String,
  var nombre: String,
  var apellido: String,
  var foto: String,
  var idDetalleUsuario: String,
) {
  constructor() : this(UUID.randomUUID().toString(), "", 2.0, "", "", "", "", "")

  override fun toString(): String {
    return "OpinionUsuarioEntity(id='$idOpinion', comentario='$comentario', nota=$nota, idUsuario='$idUsuario', nombre='$nombre', apellido='$apellido', foto='$foto', idDetalleUsuario='$idDetalleUsuario')"
  }
}
