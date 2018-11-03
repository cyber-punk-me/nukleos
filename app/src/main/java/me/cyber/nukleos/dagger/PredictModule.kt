package me.cyber.nukleos.dagger

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.predict.PredictInterface
import me.cyber.nukleos.ui.predict.PredictPresenter

@Module
class PredictModule {

    @Provides
    fun providePredictPresenter(chartsView: PredictInterface.View,
                                sensorStuffManager: SensorStuffManager): PredictPresenter = PredictPresenter(chartsView, sensorStuffManager)
}
