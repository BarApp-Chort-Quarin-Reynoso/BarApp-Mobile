package com.barapp.data.mappers

import com.barapp.data.entities.UsuarioEntity
import com.barapp.model.DetalleUsuario
import com.barapp.model.Usuario

object UsuarioMapper {
  @JvmStatic
  fun toEntity(usuario: Usuario): UsuarioEntity {
    return UsuarioEntity(
      usuario.id,
      usuario.nombre,
      usuario.apellido,
      usuario.foto,
      usuario.idDetalleUsuario,
    )
  }

  @JvmStatic
  fun fromEntity(usuarioEntity: UsuarioEntity, detalleUsuario: DetalleUsuario?): Usuario {
    return Usuario(
      usuarioEntity.idUsuario,
      usuarioEntity.nombre,
      usuarioEntity.apellido,
      usuarioEntity.foto,
      usuarioEntity.idDetalleUsuario,
      detalleUsuario,
    )
  }
}
