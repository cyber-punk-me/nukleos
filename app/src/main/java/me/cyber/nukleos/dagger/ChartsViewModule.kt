package me.cyber.nukleos.dagger

import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.charts.ChartInterface
import me.cyber.nukleos.ui.charts.ChartsFragment

@Module
abstract class ChartsViewModule {

    @Binds
    abstract fun provideChartsView(exportFragment: ChartsFragment): ChartInterface.View
}