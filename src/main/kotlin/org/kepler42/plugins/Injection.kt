package org.kepler42.plugins

import io.ktor.application.*
import org.kepler42.controllers.CommunityController
import org.kepler42.controllers.CommunityRepository
import org.kepler42.database.repositories.CommunityRepositoryImpl
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger

fun Application.configureInjection() {
    val icModule = module {
        single { CommunityController(get()) }
        single<CommunityRepository> { CommunityRepositoryImpl() }
    }
    // Declare Koin
    install(Koin) {
        SLF4JLogger()
        modules(icModule)
    }
}