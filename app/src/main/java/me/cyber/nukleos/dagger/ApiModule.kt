package me.cyber.nukleos.dagger

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.api.ApiHelper

@Module
class ApiModule {

    @Provides
    fun provideNetworkHelper() = ApiHelper()

}