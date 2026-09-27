package pe.ecoscan.app.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.core.analytics.CrashReporter
import pe.ecoscan.app.core.analytics.FirebaseCrashReporter
import android.content.Context
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseProvidersModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    abstract fun bindCrashReporter(impl: FirebaseCrashReporter): CrashReporter
}
