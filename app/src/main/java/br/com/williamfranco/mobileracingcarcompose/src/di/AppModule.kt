package br.com.williamfranco.mobileracingcarcompose.src.di

import br.com.williamfranco.mobileracingcarcompose.src.features.game.repositories.HighscoreRepository
import br.com.williamfranco.mobileracingcarcompose.src.features.game.repositories.HighscoreRepositoryImpl
import br.com.williamfranco.mobileracingcarcompose.src.features.game.repositories.highscoreDataStore
import br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models.GameViewModel
import br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models.GameViewModelImpl
import br.com.williamfranco.mobileracingcarcompose.src.services.SoundService
import br.com.williamfranco.mobileracingcarcompose.src.services.SoundServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { androidContext().highscoreDataStore }
    single<HighscoreRepository> { HighscoreRepositoryImpl(get()) }
    single<SoundService> { SoundServiceImpl(androidContext()) }
    viewModelOf(::GameViewModelImpl) { bind<GameViewModel>() }
}
