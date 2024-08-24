package com.barapp.model

class Mesa (var cantidadDePersonasPorMesa: Int, var cantidadMesas: Int) {
    constructor() : this(1, 1)

    constructor(
        mesa: Mesa
    ) : this(mesa.cantidadDePersonasPorMesa, mesa.cantidadMesas)

    override fun toString(): String {
        return "Mesa{cantidadDePersonasPorMesa=$cantidadDePersonasPorMesa, cantidadMesas=$cantidadMesas}"
    }
}