package com.example.diarycourse.features.di

import com.example.diarycourse.data.repository_api.Repository
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.domain_impl.UseCaseImpl
import dagger.Module
import dagger.Provides

@Module
class FeaturesModule {

    @Provides
    fun provideUseCase(repository: Repository): UseCase {
        return UseCaseImpl(repository)
    }
}