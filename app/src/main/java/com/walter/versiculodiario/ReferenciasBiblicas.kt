package com.walter.versiculodiario

import kotlin.random.Random
object ReferenciasBiblicas {

    val lista = listOf(
        "Gen1,1-3","Jos1,9", "Num6,24-26", "Deut6,6-7", "Sal23,1-3", "Sal23,4",
        "Sal27,1", "Sal46,1", "Sal46,2", "Sal118,24", "Sal121,1-2", "Sal139,13-14",
        "Sal73,25-26", "Is40,31", "Is41,10", "Is43,1", "Jr29,11", "Pr3,3-4",
        "Pr3,5-6", "Pr16,3", "Qo3,1", "Mt5,3-4", "Mt6,33", "Mt7,7",
        "Mt11,28", "Mt22,37-39", "Mt28,20", "Mc10,27", "Lc1,37", "Lc6,36",
        "Jn3,16", "Jn8,12", "Jn10,10", "Jn10,14-15", "Jn11,25", "Jn11,26",
        "Jn13,34", "Jn14,6", "Jn15,12", "Jn17,20", "Hech16,31", "Rm8,28",
        "Rm8,31", "Rm12,1-2", "Rm12,12", "1Co13,4", "1Co13,5", "1Co13,6",
        "1Co13,7", "1Co13,13", "1Co16,14", "2Co5,7", "2Co12,9", "Ga5,22-23",
        "Ef2,8-9", "Flp4,4-7", "Flp4,13", "Col3,23", "1Ts5,16-18", "Hb11,1",
        "2Tim1,7", "St1,5", "1Pe5,7", "1Jn4,8", "1Jn1,9", "Ap21,4"
    )

    private val ordenada = lista.shuffled(Random(2026))

    fun elegirPara(ahoraMillis: Long = System.currentTimeMillis()): String {
        val horasDesdeEpoca = ahoraMillis / 3_600_000L
        return ordenada[(horasDesdeEpoca % ordenada.size).toInt()]
    }
}