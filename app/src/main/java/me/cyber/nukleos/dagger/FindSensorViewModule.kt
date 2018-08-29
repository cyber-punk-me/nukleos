package me.cyber.nukleos.dagger
import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.find.FindSensorInterface
import me.cyber.nukleos.ui.find.FindSensorFragment

@Module
abstract class FindSensorViewModule {

    @Binds
    abstract fun provideFindSensorView(findFragment: FindSensorFragment): FindSensorInterface.View
}