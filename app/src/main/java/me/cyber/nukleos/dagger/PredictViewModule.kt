package me.cyber.nukleos.dagger

import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.predict.PredictFragment
import me.cyber.nukleos.ui.predict.PredictInterface

@Module
abstract class PredictViewModule {

    @Binds
    abstract fun providePredictView(exportFragment: PredictFragment): PredictInterface.View
}