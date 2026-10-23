package com.walter.versiculodiario
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class VersiculoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = VersiculoWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        VersiculoWorker.programar(context)
        CompletarWidgetWorker.encolar(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        VersiculoWorker.cancelar(context)
    }
}