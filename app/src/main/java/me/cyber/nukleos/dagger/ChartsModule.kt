package me.cyber.nukleos.dagger

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.training.TrainingInterface
import me.cyber.nukleos.ui.training.TrainingPresenter

@Module
class ChartsModule {

    @Provides
    fun provideGraphPresenter(chartsView: TrainingInterface.View,
                              peripheryManager: PeripheryManager): TrainingPresenter = TrainingPresenter(chartsView, peripheryManager)
}
