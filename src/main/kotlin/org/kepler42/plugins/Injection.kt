package org.kepler42.plugins

import io.ktor.application.*
import org.kepler42.controllers.TagController
import org.kepler42.database.repositories.*
import org.kepler42.utils.TokenValidator
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger

fun Application.configureInjection() {
    val icModule = module {
        single { TagController(get()) }
        single { TokenValidator() }
        single<CommunityRepository> { CommunityRepositoryImpl() }
        single<UserRepository> { UserRepositoryImpl() }
        single<TagRepository> { TagRepositoryImpl() }
    }
    // Declare Koin
    install(Koin) {
        SLF4JLogger()
        modules(icModule)
    }
}
