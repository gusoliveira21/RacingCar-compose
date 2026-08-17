package br.com.williamfranco.mobileracingcarcompose

import android.app.Application
import br.com.williamfranco.mobileracingcarcompose.src.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RacingCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RacingCarApplication)
            modules(appModule)
        }
    }
}
