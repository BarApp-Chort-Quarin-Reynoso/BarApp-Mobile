package com.barapp.data.mappers

import com.barapp.data.entities.OpinionEntity
import com.barapp.data.entities.OpinionUsuarioEntity
import com.barapp.data.entities.UsuarioEntity
import com.barapp.model.Opinion
import com.barapp.model.Usuario

object OpinionMapper {
  @JvmStatic
  fun toEntity(opinion: Opinion): OpinionEntity {
    return OpinionEntity(opinion.id, opinion.comentario, opinion.nota)
  }

  @JvmStatic
  fun fromEntity(opinionEntity: OpinionEntity, usuario: Usuario?): Opinion {
    return Opinion(opinionEntity.idOpinion, opinionEntity.comentario, opinionEntity.nota, usuario!!)
  }

  @JvmStatic
  fun toOpinionUsuarioEntity(opinion: Opinion): OpinionUsuarioEntity {
    return OpinionUsuarioEntity(
      opinion.id,
      opinion.comentario,
      opinion.nota,
      opinion.usuario.id,
      opinion.usuario.nombre,
      opinion.usuario.apellido,
      opinion.usuario.foto,
      opinion.usuario.idDetalleUsuario,
    )
  }

  @JvmStatic
  fun fromOpinionUsuarioToList(
    opinionUsuarioEntityList: List<OpinionUsuarioEntity>
  ): List<Opinion> {
    val resultado: MutableList<Opinion> = ArrayList()
    var opinionEntity: OpinionEntity
    var usuarioEntity: UsuarioEntity
    var opinion: Opinion
    for (opinionUsuarioEntity in opinionUsuarioEntityList) {
      opinionEntity =
        OpinionEntity(
          opinionUsuarioEntity.idOpinion,
          opinionUsuarioEntity.comentario,
          opinionUsuarioEntity.nota,
        )
      usuarioEntity =
        UsuarioEntity(
          opinionUsuarioEntity.idUsuario,
          opinionUsuarioEntity.nombre,
          opinionUsuarioEntity.apellido,
          opinionUsuarioEntity.foto,
          opinionUsuarioEntity.idDetalleUsuario,
          HashSet()
        )
      opinion = fromEntity(opinionEntity, UsuarioMapper.fromEntity(usuarioEntity, null))
      resultado.add(opinion)
    }
    return resultado
  }
}
