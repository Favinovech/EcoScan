package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.notification.VisualReminderNotifier
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

    // HU09, parte 2: notificación visual real (reemplaza la de solo log de la parte 1); el
    // Worker que la usa (ReminderWorker) no cambia.
    @Binds
    @Singleton
    abstract fun bindReminderNotifier(impl: VisualReminderNotifier): ReminderNotifier
}
