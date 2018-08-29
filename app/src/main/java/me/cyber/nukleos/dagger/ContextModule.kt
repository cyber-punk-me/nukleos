package me.cyber.nukleos.dagger
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ContextModule(private var context: Context) {

    @Provides
    fun context(): Context = context.applicationContext

}

