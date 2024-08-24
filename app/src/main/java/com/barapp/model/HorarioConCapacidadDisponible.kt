package com.barapp.model

class HorarioConCapacidadDisponible (var horarios: List<String>, var tipoComida: TipoComida, var mesas: List<Mesa>){
    constructor() : this(emptyList(), TipoComida.NINGUNO, emptyList())

    override fun toString(): String {
        return "HorarioConCapacidadDisponible{horarios=$horarios, tipoComida=$tipoComida, mesas=$mesas}"
    }
}