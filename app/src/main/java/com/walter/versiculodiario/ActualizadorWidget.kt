package com.walter.versiculodiario

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

object ActualizadorWidget {

    // Solo rellena los widgets que todavía no tienen versículo; no pisa los que ya tienen uno
    suspend fun completarVacios(context: Context) {
        val ids = GlanceAppWidgetManager(context)
            .getGlanceIds(VersiculoWidget::class.java)

        val vacios = ids.filter { id ->
            getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[VersiculoWidget.TEXTO_KEY] == null
        }
        if (vacios.isEmpty()) return

        val consulta = ReferenciasBiblicas.elegirPara()
        val versiculo = RepositorioVersiculos.obtener(context, consulta)
        vacios.forEach { id -> guardar(context, id, consulta, versiculo) }
    }

    // Actualización horaria: todos los widgets reciben el versículo de esta hora
    suspend fun actualizar(context: Context) {
        val ids = GlanceAppWidgetManager(context)
            .getGlanceIds(VersiculoWidget::class.java)
        if (ids.isEmpty()) return

        val consulta = ReferenciasBiblicas.elegirPara()
        val versiculo = RepositorioVersiculos.obtener(context, consulta)

        ids.forEach { id -> guardar(context, id, consulta, versiculo) }
    }

    // Botón de refrescar: solo el widget tocado pide un versículo distinto al actual
    suspend fun refrescar(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val actual = prefs[VersiculoWidget.CONSULTA_KEY]

        val nueva = ReferenciasBiblicas.lista.filter { it != actual }.random()
        val versiculo = RepositorioVersiculos.obtener(context, nueva)

        guardar(context, id, nueva, versiculo)
    }

    private suspend fun guardar(
        context: Context,
        id: GlanceId,
        consulta: String,
        versiculo: Versiculo
    ) {
        updateAppWidgetState(context, id) { prefs ->
            prefs[VersiculoWidget.TEXTO_KEY] = versiculo.texto
            prefs[VersiculoWidget.REFERENCIA_KEY] = versiculo.referencia
            prefs[VersiculoWidget.CONSULTA_KEY] = consulta
            prefs[VersiculoWidget.ULTIMA_KEY] = System.currentTimeMillis()
        }
        VersiculoWidget().update(context, id)
    }
}