package me.cyber.nukleos.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val mContext: Context) {

    @Provides
    fun provideAppContext() = mContext
}