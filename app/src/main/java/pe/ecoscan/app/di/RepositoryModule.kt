package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.repository.WasteRecordRepositoryImpl
import pe.ecoscan.app.domain.repository.WasteRecordRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWasteRecordRepository(impl: WasteRecordRepositoryImpl): WasteRecordRepository
}
