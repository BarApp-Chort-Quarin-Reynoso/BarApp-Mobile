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
  var estado: String,
  var cantidadPersonas: Int,
  var fecha: String,
  var restaurante: Restaurante,
  var horario: Horario,
  var usuario: Usuario?,
) : BaseClass(id) {
  constructor() :
    this(UUID.randomUUID().toString(), "PENDIENTE", -1, LocalDate.MAX.toString(), Restaurante(), Horario(), null)

  constructor(
    estado: String,
    cantidadPersonas: Int,
    fecha: String,
    restaurante: Restaurante,
    horario: Horario,
    usuario: Usuario,
  ) : this(
    UUID.randomUUID().toString(),
    estado,
    cantidadPersonas,
    fecha,
    restaurante,
    horario,
    usuario,
  )

  override fun toString(): String {
    return "Reserva{" +
      "id='" +
      id +
      '\'' +
      ", estado='" +
      estado +
      '\'' +
      ", cantidadPersonas=" +
      cantidadPersonas +
      ", fecha=" +
      fecha +
      ", restaurante=" +
      restaurante +
      ", horario=" +
      horario +
      '\'' +
      ", usuario=" +
      usuario +
      '}'
  }

  fun getFechaAsLocalDate(): LocalDate {
    return LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE)
  }
}
