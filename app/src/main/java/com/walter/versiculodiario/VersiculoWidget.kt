package com.walter.versiculodiario

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import android.content.Intent
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Row
import androidx.datastore.preferences.core.longPreferencesKey
class VersiculoWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(PEQUENO, MEDIANO, GRANDE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            VersiculoContent(
                texto = prefs[TEXTO_KEY],
                referencia = prefs[REFERENCIA_KEY]
            )
        }
    }

    companion object {
        val TEXTO_KEY = stringPreferencesKey("texto")
        val REFERENCIA_KEY = stringPreferencesKey("referencia")
        val CONSULTA_KEY = stringPreferencesKey("consulta")   // <- nueva

        val PEQUENO = DpSize(120.dp, 60.dp)
        val MEDIANO = DpSize(250.dp, 110.dp)
        val GRANDE = DpSize(250.dp, 220.dp)

        val ULTIMA_KEY = longPreferencesKey("ultima_actualizacion")
    }
}

private data class Estilo(
    val tamanioTexto: TextUnit,
    val tamanioReferencia: TextUnit,
    val maxLineas: Int,
    val margen: Dp
)

@Composable
private fun VersiculoContent(texto: String?, referencia: String?) {
    val size = LocalSize.current
    val context = LocalContext.current

    val estilo = when {
        size.height >= VersiculoWidget.GRANDE.height -> Estilo(18.sp, 13.sp, 10, 16.dp)
        size.height >= VersiculoWidget.MEDIANO.height -> Estilo(14.sp, 12.sp, 5, 16.dp)
        else -> Estilo(12.sp, 10.sp, 2, 8.dp)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(estilo.margen)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = texto ?: "Esperando el primer versículo…\n(necesita conexión a internet)",
            maxLines = estilo.maxLineas,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = estilo.tamanioTexto
            )
        )
        if (referencia != null) {
            Row(
                modifier = GlanceModifier.padding(top = 6.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = referencia,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFCBA6F7)),
                        fontSize = estilo.tamanioReferencia,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "↻",
                    modifier = GlanceModifier
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable(actionRunCallback(RefrescarCallback::class.java)),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = estilo.tamanioTexto,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            Text(
                text = "↻ Reintentar",
                modifier = GlanceModifier
                    .padding(top = 6.dp)
                    .clickable(actionRunCallback(RefrescarCallback::class.java)),
                style = TextStyle(
                    color = ColorProvider(Color(0xFFCBA6F7)),
                    fontSize = estilo.tamanioReferencia,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}