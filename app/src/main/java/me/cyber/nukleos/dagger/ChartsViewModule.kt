package me.cyber.nukleos.dagger

import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.training.TrainingInterface
import me.cyber.nukleos.ui.training.TrainingFragment

@Module
abstract class ChartsViewModule {

    @Binds
    abstract fun provideChartsView(exportFragment: TrainingFragment): TrainingInterface.View
}