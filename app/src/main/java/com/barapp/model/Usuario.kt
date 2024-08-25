package com.barapp.model

import com.barapp.model.BaseClass
import com.barapp.model.DetalleUsuario
import java.util.UUID

class Usuario(
  id: String,
  var nombre: String,
  var apellido: String,
  var foto: String,
  var idDetalleUsuario: String,
  var detalleUsuario: DetalleUsuario?,
  var fcmTokens: MutableSet<String>
) : BaseClass(id) {

  constructor() : this(UUID.randomUUID().toString(), "", "", "", "", null, HashSet())

  override fun toString(): String {
    return "Usuario{" +
      "id='" +
      id +
      '\'' +
      ", nombre='" +
      nombre +
      '\'' +
      ", apellido='" +
      apellido +
      '\'' +
      ", foto='" +
      foto +
      '\'' +
      ", idDetalleUsuario='" +
      idDetalleUsuario +
      '\'' +
      ", detalleUsuario=" +
      detalleUsuario +
      '}'
  }
}
