package cz.splnsito.mrthom.loglantern.core.network.auth

import cz.splnsito.mrthom.loglantern.core.security.TokenStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProvider @Inject constructor(private val tokenStorage: TokenStorage) {
    fun blockingGet(): String? {
        return runBlocking {
            tokenStorage.getToken().first()
        }
    }
}
