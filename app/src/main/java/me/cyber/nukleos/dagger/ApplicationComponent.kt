package me.cyber.nukleos.dagger

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import me.cyber.nukleos.App
import me.cyber.nukleos.api.ApiHelper
import javax.inject.Singleton


@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ContextModule::class,
    BuildersModule::class,
    MyoConnectorModule::class,
    ApiModule::class,
    SensorStuffManagerModule::class
])
interface ApplicationComponent : AndroidInjector<App>{
    fun getApiHelper(): ApiHelper
}
