package com.barapp.barapp.model

import com.barapp.model.BaseClass
import com.barapp.model.Horario
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class Reserva(
  id: String,
  var cancelada: Boolean,
  var cantidadPersonas: Int,
  var fecha: String,
  var restaurante: Restaurante,
  var horario: Horario,
  var idUsuario: String,
  var usuario: Usuario?,
) : BaseClass(id) {
  constructor() :
    this(UUID.randomUUID().toString(), false, -1, LocalDate.MAX.toString(), Restaurante(), Horario(), "", null)

  constructor(
    cantidadPersonas: Int,
    fecha: String,
    restaurante: Restaurante,
    horario: Horario,
    usuario: Usuario,
  ) : this(
    UUID.randomUUID().toString(),
    false,
    cantidadPersonas,
    fecha,
    restaurante,
    horario,
    usuario.id,
    usuario,
  )

  override fun toString(): String {
    return "Reserva{" +
      "id='" +
      id +
      '\'' +
      ", cancelada=" +
      cancelada +
      ", cantidadPersonas=" +
      cantidadPersonas +
      ", fecha=" +
      fecha +
      ", restaurante=" +
      restaurante +
      ", horario=" +
      horario +
      ", idUsuario='" +
      idUsuario +
      '\'' +
      ", usuario=" +
      usuario +
      '}'
  }

  fun getFechaAsLocalDate(): LocalDate {
    return LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE)
  }
}
