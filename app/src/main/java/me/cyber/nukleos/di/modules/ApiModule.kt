package me.cyber.nukleos.di.modules

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.api.ApiHelper

@Module
class ApiModule {

    @Provides
    fun provideNetworkHelper() = ApiHelper()

}