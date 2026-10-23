package com.walter.versiculodiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Versiculo(
    val referencia: String,
    val texto: String
)

class LimiteExcedidoException(mensaje: String) : IOException(mensaje)

object BibleGetClient {

    private const val BASE_URL = "https://query.bibleget.io/v3"
    private const val APP_ID = "versiculodiario"
    const val VERSION = "BLPD"

    suspend fun obtenerVersiculo(referencia: String): Versiculo =
        withContext(Dispatchers.IO) {
            val consulta = URLEncoder.encode(referencia, "UTF-8")
            val url = URL("$BASE_URL?query=$consulta&version=$VERSION&appid=$APP_ID&return=json")

            val conexion = url.openConnection() as HttpURLConnection
            try {
                conexion.requestMethod = "GET"
                conexion.connectTimeout = 10_000
                conexion.readTimeout = 10_000
                conexion.setRequestProperty("Accept", "application/json")

                if (conexion.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("Error HTTP ${conexion.responseCode}")
                }

                val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                parsear(cuerpo)
            } finally {
                conexion.disconnect()
            }
        }

    private fun parsear(json: String): Versiculo {
        val raiz = JSONObject(json)

        val errores = raiz.optJSONArray("errors")
        if (errores != null && errores.length() > 0) {
            val esLimite = (0 until errores.length()).any { i ->
                errores.optJSONObject(i)?.optInt("errNum") == 12
            }
            if (esLimite) throw LimiteExcedidoException("Límite de consultas de BibleGet excedido")
            throw IOException("BibleGet devolvió errores: $errores")
        }

        val resultados = raiz.getJSONArray("results")
        if (resultados.length() == 0) throw IOException("La respuesta no trae versículos")

        val primero = resultados.getJSONObject(0)
        val ultimo = resultados.getJSONObject(resultados.length() - 1)

        val libro = primero.getString("book")
        val capitulo = primero.getInt("chapter")
        val desde = primero.getString("verse")
        val hasta = ultimo.getString("verse")

        val referencia = if (resultados.length() == 1) {
            "$libro $capitulo,$desde"
        } else {
            "$libro $capitulo,$desde-$hasta"
        }

        val texto = (0 until resultados.length()).joinToString(" ") { i ->
            resultados.getJSONObject(i).getString("text").trim()
        }

        return Versiculo(referencia, texto)
    }
}