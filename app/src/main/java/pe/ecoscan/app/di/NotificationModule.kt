package pe.ecoscan.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.preferences.DataStoreNotificationPreferences
import pe.ecoscan.app.domain.preferences.NotificationPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationPreferences(
        impl: DataStoreNotificationPreferences
    ): NotificationPreferences
}
