package com.barapp.model

import java.util.UUID

class Opinion(id: String, var comentario: String, var nota: Double, var usuario: Usuario, var idRestaurante: String, var horario: Horario, var fecha: String, var cantidadPersonas: Int, var caracteristicas: Map<String, Float>) :
  BaseClass(id) {
  constructor() : this(UUID.randomUUID().toString(), "", -1.0, Usuario(), "", Horario(), "", -1, HashMap())

  constructor(
    comentario: String,
    nota: Double,
    usuario: Usuario,
    idRestaurante: String,
    horario: Horario,
    fecha: String,
    cantidadPersonas: Int,
    caracteristicas: Map<String, Float>
  ) : this(UUID.randomUUID().toString(), comentario, nota, usuario, idRestaurante, horario, fecha, cantidadPersonas, caracteristicas)

  override fun toString(): String {
    return "Opinion{" +
      "id='" +
      id +
      ", comentario='" +
      comentario +
      ", nota=" +
      nota +
      ", usuario=" +
      usuario +
      ", idRestaurante='" +
      idRestaurante +
      ", horario=" +
      horario +
      ", fecha='" +
      fecha +
      ", cantidadPersonas=" +
      cantidadPersonas +
      ", caracteristicas=" +
      caracteristicas +
      '}'
  }
}
