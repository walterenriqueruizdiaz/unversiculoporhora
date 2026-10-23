package com.walter.versiculodiario

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class RefrescarCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            ActualizadorWidget.refrescar(context, glanceId)
        } catch (e: Exception) {
            Log.e("RefrescarCallback", "No se pudo refrescar el versículo", e)
        }
    }
}