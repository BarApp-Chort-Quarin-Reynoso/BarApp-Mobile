package com.barapp.model

class CalificacionPromedio (
    var puntuacion: Double,
    var cantidadOpiniones: Int,
) {
    constructor() : this(0.0, 0)

    override fun toString(): String {
        return "CalificacionPromedio{" +
            "puntuacion=" +
            puntuacion +
            ", cantidadOpiniones=" +
            cantidadOpiniones +
            '}'
    }
}