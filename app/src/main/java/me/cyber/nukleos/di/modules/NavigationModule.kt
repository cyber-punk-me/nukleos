package me.cyber.nukleos.di.modules

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.navigation.INavigator
import me.cyber.nukleos.navigation.Navigator
import javax.inject.Singleton


/**
 * Модуль для подтягивания навигации. В нашем случаю заюзаем Cicerone (только запрятав его вглубь)
 *  @link https://github.com/terrakok/Cicerone
 */

@Module
class NavigationModule {

    @Provides
    @Singleton
    fun provideNavigator(): INavigator = Navigator()
}