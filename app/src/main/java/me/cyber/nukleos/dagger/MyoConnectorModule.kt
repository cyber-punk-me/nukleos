package me.cyber.nukleos.dagger
import android.content.Context
import dagger.Module
import dagger.Provides
import me.cyber.nukleos.bluetooth.BluetoothConnector
import javax.inject.Singleton

@Module(includes = [ContextModule::class])
class MyoConnectorModule {

    @Provides
    @Singleton
    fun provideMyonnaise(context: Context): BluetoothConnector {
        return BluetoothConnector(context)
    }

}
