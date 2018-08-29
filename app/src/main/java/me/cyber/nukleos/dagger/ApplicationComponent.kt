package me.cyber.nukleos.dagger

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import me.cyber.nukleos.App
import javax.inject.Singleton


@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ContextModule::class,
    BuildersModule::class,
    MyoConnectorModule::class,
    SensorStuffManagerModule::class
])
interface ApplicationComponent : AndroidInjector<App>