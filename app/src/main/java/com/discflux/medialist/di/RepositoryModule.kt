package com.discflux.medialist.di

import com.discflux.medialist.data.repository.AuthRepositoryImpl
import com.discflux.medialist.data.repository.MediaRepositoryImpl
import com.discflux.medialist.data.repository.MediaSearchRepositoryImpl
import com.discflux.medialist.domain.repository.AuthRepository
import com.discflux.medialist.domain.repository.MediaRepository
import com.discflux.medialist.domain.repository.MediaSearchRepository
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
    abstract fun bindMediaRepository(
        mediaRepositoryImpl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMediaSearchRepository(
        mediaSearchRepositoryImpl: MediaSearchRepositoryImpl
    ): MediaSearchRepository
}
