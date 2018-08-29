package me.cyber.nukleos.dagger
import android.content.Context
import dagger.Module
import dagger.Provides
import me.cyber.nukleos.myosensor.MyoConnector
import javax.inject.Singleton

@Module(includes = [ContextModule::class])
class MyoConnectorModule {

    @Provides
    @Singleton
    fun provideMyonnaise(context: Context): MyoConnector {
        return MyoConnector(context)
    }

}
