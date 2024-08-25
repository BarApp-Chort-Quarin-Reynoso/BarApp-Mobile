package com.barapp.data.entities

import java.util.UUID

class UsuarioEntity(
  var idUsuario: String,
  var nombre: String,
  var apellido: String,
  var foto: String,
  var idDetalleUsuario: String,
  var fcmTokens: MutableSet<String>
) {
  constructor() : this(UUID.randomUUID().toString(), "", "", "", "", HashSet())

  override fun toString(): String {
    return "UsuarioEntity(idUsuario='$idUsuario', nombre='$nombre', apellido='$apellido', foto='$foto', idDetalleUsuario='$idDetalleUsuario')"
  }
}
