package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.core.network.ConnectivityObserver
import pe.ecoscan.app.core.network.NetworkConnectivityObserver
import pe.ecoscan.app.data.sync.WasteRecordSyncerImpl
import pe.ecoscan.app.domain.sync.WasteRecordSyncer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: NetworkConnectivityObserver): ConnectivityObserver

    // HU09, parte 3: interfaz para poder sustituir la sincronización real por un fake en las
    // pruebas de SyncWorker (androidTest).
    @Binds
    @Singleton
    abstract fun bindWasteRecordSyncer(impl: WasteRecordSyncerImpl): WasteRecordSyncer
}