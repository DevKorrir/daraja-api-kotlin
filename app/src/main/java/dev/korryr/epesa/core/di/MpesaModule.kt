package dev.korryr.epesa.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.korryr.epesa.feature.payment.data.remote.MpesaApiService
import dev.korryr.epesa.feature.payment.data.repository.MpesaRepositoryImpl
import dev.korryr.epesa.feature.payment.domain.repository.MpesaRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MpesaModule {

    @Provides
    @Singleton
    fun provideMpesaApiService(retrofit: Retrofit): MpesaApiService =
        retrofit.create(MpesaApiService::class.java)

    @Provides
    @Singleton
    fun provideMpesaRepository(
        api: MpesaApiService,
        @ApplicationContext context: Context
    ): MpesaRepository = MpesaRepositoryImpl(api, context)
}
