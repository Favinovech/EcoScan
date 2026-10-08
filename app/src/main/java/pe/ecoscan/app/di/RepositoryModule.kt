package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.repository.FirebaseAuthRepositoryImpl
import pe.ecoscan.app.data.repository.ProductRepositoryImpl
import pe.ecoscan.app.data.repository.ProfileRepositoryImpl
import pe.ecoscan.app.data.repository.WasteRecordRepositoryImpl
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProductRepository
import pe.ecoscan.app.domain.repository.ProfileRepository
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWasteRecordRepository(impl: WasteRecordRepositoryImpl): WasteRecordRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}