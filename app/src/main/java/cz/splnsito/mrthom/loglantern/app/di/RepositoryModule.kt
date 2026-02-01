package cz.splnsito.mrthom.loglantern.app.di

import cz.splnsito.mrthom.loglantern.data.repository.AuthRepositoryImpl
import cz.splnsito.mrthom.loglantern.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository
}
