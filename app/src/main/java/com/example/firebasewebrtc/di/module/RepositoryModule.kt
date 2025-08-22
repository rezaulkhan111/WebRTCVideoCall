package com.example.firebasewebrtc.di.module

import com.example.firebasewebrtc.domain.repository.CallRepositoryImpl
import com.example.firebasewebrtc.domain.repository.ICallRepository
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
    abstract fun bindCallRepository(
        callRepository: CallRepositoryImpl
    ): ICallRepository
}