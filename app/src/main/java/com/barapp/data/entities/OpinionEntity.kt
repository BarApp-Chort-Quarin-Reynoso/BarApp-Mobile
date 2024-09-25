package com.barapp.data.entities

import java.util.UUID

class OpinionEntity(
  var idOpinion: String,
  var comentario: String,
  var nota: Double,
  var idUsuario: String,
  var idRestaurante: String,
  var nombreUsuario: String,
  var apellidoUsuario: String,
  var foto: String,
  var fecha: String,
  var horario: String,
  var cantidadPersonas: Int,
  var tipoComida: String,
  var caracteristicas: Map<String, Int>,
) {
  constructor() : this(UUID.randomUUID().toString(), "", 1.0, "", "", "", "", "", "", "", 0, "", HashMap())

  override fun toString(): String {
    return "OpinionEntity(id='$idOpinion', comentario='$comentario', nota=$nota, idUsuario='$idUsuario', idRestaurante='$idRestaurante', nombreUsuario='$nombreUsuario', apellidoUsuario='$apellidoUsuario', foto='$foto', fecha='$fecha', horario='$horario', cantidadPersonas=$cantidadPersonas, tipoComida='$tipoComida', caracteristicas=$caracteristicas)"
  }
}
