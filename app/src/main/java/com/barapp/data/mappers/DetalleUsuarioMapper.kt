package com.barapp.data.mappers

import com.barapp.data.entities.DetalleUsuarioEntity
import com.barapp.model.DetalleUsuario
import java.util.LinkedList

object DetalleUsuarioMapper {
  @JvmStatic
  fun toEntity(detalleUsuario: DetalleUsuario): DetalleUsuarioEntity {
    return DetalleUsuarioEntity(
      detalleUsuario.id,
      detalleUsuario.mail,
      detalleUsuario.telefono,
      detalleUsuario.busquedasRecientes,
      ArrayList(detalleUsuario.idsRestaurantesFavoritos),
    )
  }

  @JvmStatic
  fun fromEntity(detalleUsuarioEntity: DetalleUsuarioEntity): DetalleUsuario {
    return DetalleUsuario(
      detalleUsuarioEntity.idDetalleUsuario,
      detalleUsuarioEntity.mail,
      detalleUsuarioEntity.telefono,
      detalleUsuarioEntity.busquedasRecientes as? LinkedList
        ?: LinkedList(detalleUsuarioEntity.busquedasRecientes),
      HashSet(detalleUsuarioEntity.idRestaurantesFavoritos),
    )
  }
}
