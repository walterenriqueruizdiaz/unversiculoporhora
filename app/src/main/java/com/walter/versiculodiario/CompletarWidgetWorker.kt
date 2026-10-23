package com.walter.versiculodiario

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class CompletarWidgetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            ActualizadorWidget.completarVacios(applicationContext)
            Result.success()
        } catch (e: LimiteExcedidoException) {
            Result.success() // lo completará el ciclo horario o el botón Reintentar
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val NOMBRE = "versiculo_inicial"

        fun encolar(context: Context) {
            val pedido = OneTimeWorkRequestBuilder<CompletarWidgetWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.KEEP, pedido)
        }
    }
}