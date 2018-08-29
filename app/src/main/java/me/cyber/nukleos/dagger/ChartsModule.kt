package me.cyber.nukleos.dagger

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.charts.ChartInterface
import me.cyber.nukleos.ui.charts.ChartsPresenter

@Module
class ChartsModule {

    @Provides
    fun provideGraphPresenter(chartsView: ChartInterface.View,
                              sensorStuffManager: SensorStuffManager): ChartsPresenter = ChartsPresenter(chartsView, sensorStuffManager)
}
