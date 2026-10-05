package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.repository.TFLiteWasteClassifierImpl
import pe.ecoscan.app.domain.repository.WasteClassifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassifierModule {

    @Binds
    @Singleton
    abstract fun bindWasteClassifier(impl: TFLiteWasteClassifierImpl): WasteClassifier
}