package com.walter.versiculodiario

import android.content.Context
import org.json.JSONObject

object RepositorioVersiculos {

    private const val ARCHIVO = "cache_versiculos"

    suspend fun obtener(context: Context, consulta: String): Versiculo {
        val prefs = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        val clave = "${BibleGetClient.VERSION}|$consulta"

        val guardado = prefs.getString(clave, null)?.let { texto ->
            runCatching {
                val json = JSONObject(texto)
                Versiculo(json.getString("referencia"), json.getString("texto"))
            }.getOrNull()
        }
        if (guardado != null) return guardado

        val versiculo = BibleGetClient.obtenerVersiculo(consulta)
        val json = JSONObject()
            .put("referencia", versiculo.referencia)
            .put("texto", versiculo.texto)
        prefs.edit().putString(clave, json.toString()).apply()
        return versiculo
    }
}