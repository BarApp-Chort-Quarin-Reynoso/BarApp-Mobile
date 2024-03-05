package com.barapp.data.mappers

import com.barapp.data.entities.ReservaBDEntity
import com.barapp.data.entities.ReservaEntity
import com.barapp.model.Horario
import com.barapp.barapp.model.Reserva
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

object ReservaMapper {
  @JvmStatic
  fun toEntity(reserva: Reserva): ReservaEntity {
    return ReservaEntity(
      reserva.id,
      reserva.cancelada,
      reserva.cantidadPersonas,
      reserva.fecha.toString(),
      reserva.idUsuario,
    )
  }

  @JvmStatic
  fun fromEntity(
    reservaEntity: ReservaEntity,
    restaurante: Restaurante,
    horario: Horario,
    usuario: Usuario?,
  ): Reserva {
    return Reserva(
      reservaEntity.idReserva,
      reservaEntity.cancelada,
      reservaEntity.cantidadPersonas,
      LocalDate.parse(reservaEntity.fecha),
      restaurante,
      horario,
      reservaEntity.idUsuario,
      usuario,
    )
  }

  @JvmStatic
  fun toReservaBDEntity(reserva: Reserva): ReservaBDEntity {
    return ReservaBDEntity(
      reserva.id,
      reserva.cancelada,
      reserva.cantidadPersonas,
      reserva.fecha.toString(),
      reserva.idUsuario,
      reserva.restaurante.id,
      reserva.restaurante.nombre,
      reserva.restaurante.puntuacion,
      reserva.restaurante.foto,
      reserva.restaurante.logo,
      reserva.restaurante.idDetalleRestaurante,
      reserva.restaurante.ubicacion.id,
      reserva.restaurante.ubicacion.calle,
      reserva.restaurante.ubicacion.numero,
      reserva.horario.id,
      reserva.horario.hora.toString(),
      reserva.horario.tipoComida.toString(),
      Timestamp(
        Date.from(
          reserva.horario.hora
            .atDate(reserva.fecha)
            .atZone(ZoneId.of("America/Buenos_Aires"))
            .toInstant()
        )
      ),
    )
  }
}
