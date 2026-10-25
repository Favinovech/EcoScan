package pe.ecoscan.app.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    // WorkManager se inicializa a demanda la primera vez que se llama a getInstance(), usando
    // la Configuration que EcoScanApplication expone como Configuration.Provider (con el
    // HiltWorkerFactory), ya que se quitó el inicializador automático en el manifest.
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
