package com.walter.versiculodiario

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
class VersiculoWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            ActualizadorWidget.actualizar(applicationContext)
            registrar("OK")
            Result.success()
        } catch (e: LimiteExcedidoException) {
            registrar("LIMITE de la API (error 12): sin reintentos, se espera al próximo ciclo")
            Result.success()
        } catch (e: Exception) {
            registrar("ERROR intento ${runAttemptCount + 1}: ${e.javaClass.simpleName} ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun registrar(resultado: String) {
        val prefs = applicationContext.getSharedPreferences("diagnostico", Context.MODE_PRIVATE)
        val hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"))
        val anteriores = prefs.getString("log", "").orEmpty().lines().filter { it.isNotBlank() }
        val nuevo = (listOf("$hora $resultado") + anteriores).take(10)
        prefs.edit().putString("log", nuevo.joinToString("\n")).apply()
    }

    companion object {
        private const val NOMBRE_TRABAJO = "versiculo_horario"

        fun programar(context: Context) {
            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val pedido = PeriodicWorkRequestBuilder<VersiculoWorker>(1, TimeUnit.HOURS)
                .setConstraints(restricciones)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                pedido
            )
        }

        fun cancelar(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NOMBRE_TRABAJO)
        }
    }
}