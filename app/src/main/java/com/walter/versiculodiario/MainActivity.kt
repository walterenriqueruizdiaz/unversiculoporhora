package com.walter.versiculodiario

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.selection.SelectionContainer
import android.content.Intent
import androidx.compose.material3.Button
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colores = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colores) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaVersiculo()
                }
            }
        }
    }
}

private data class Cargado(
    val versiculo: Versiculo,
    val actualizadoMillis: Long?
)

private sealed interface Estado {
    data object Cargando : Estado
    data class Listo(val datos: Cargado) : Estado
    data object Error : Estado
}

@Composable
private fun PantallaVersiculo() {
    val context = LocalContext.current
    val estado by produceState<Estado>(initialValue = Estado.Cargando) {
        value = try {
            Estado.Listo(cargarVersiculo(context))
        } catch (e: Exception) {
            Estado.Error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val actual = estado) {
            Estado.Cargando -> Text("Cargando versículo…")
            Estado.Error -> Text("No se pudo cargar el versículo. Probá de nuevo más tarde.")
            is Estado.Listo -> {
                Text(
                    text = actual.datos.versiculo.texto,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = actual.datos.versiculo.referencia,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        val mensaje = "“${actual.datos.versiculo.texto}”\n" +
                                "— ${actual.datos.versiculo.referencia} (Libro del Pueblo de Dios)"

                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, mensaje)
                        }
                        context.startActivity(Intent.createChooser(envio, "Compartir versículo"))
                    },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Compartir")
                }
                actual.datos.actualizadoMillis?.let { millis ->
                    val formato = DateTimeFormatter.ofPattern("dd/MM HH:mm")
                    val hora = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .format(formato)
                    Text(
                        text = "Actualizado: $hora",
                        modifier = Modifier.padding(top = 24.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        val registro = remember {
            context.getSharedPreferences("diagnostico", Context.MODE_PRIVATE)
                .getString("log", "").orEmpty()
        }
        val ultimaFueExitosa = registro.lines().firstOrNull()?.endsWith("OK") == true
        if (registro.isNotBlank() && !ultimaFueExitosa) {
            SelectionContainer {
                Text(
                    text = "Registro de ejecuciones:\n$registro",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private suspend fun cargarVersiculo(context: Context): Cargado {
    val ids = GlanceAppWidgetManager(context).getGlanceIds(VersiculoWidget::class.java)
    val prefs = ids.firstOrNull()?.let {
        getAppWidgetState(context, PreferencesGlanceStateDefinition, it)
    }

    val texto = prefs?.get(VersiculoWidget.TEXTO_KEY)
    val referencia = prefs?.get(VersiculoWidget.REFERENCIA_KEY)

    return if (texto != null && referencia != null) {
        Cargado(Versiculo(referencia, texto), prefs.get(VersiculoWidget.ULTIMA_KEY))
    } else {
        Cargado(RepositorioVersiculos.obtener(context, ReferenciasBiblicas.elegirPara()), null)
    }
}