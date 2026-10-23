package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.notification.LoggingReminderNotifier
import pe.ecoscan.app.data.repository.CollectionScheduleRepositoryImpl
import pe.ecoscan.app.domain.notification.ReminderNotifier
import pe.ecoscan.app.domain.repository.CollectionScheduleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindCollectionScheduleRepository(
        impl: CollectionScheduleRepositoryImpl
    ): CollectionScheduleRepository

    // La parte 2 de la HU09 reemplaza este binding por la implementación con notificación
    // visual real; el Worker que la usa (ReminderWorker) no cambia.
    @Binds
    @Singleton
    abstract fun bindReminderNotifier(impl: LoggingReminderNotifier): ReminderNotifier
}
