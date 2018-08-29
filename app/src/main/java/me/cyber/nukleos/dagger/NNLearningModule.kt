package me.cyber.nukleos.dagger

import dagger.Module
import dagger.Provides
import me.cyber.nukleos.ui.export.NNLearningInterface
import me.cyber.nukleos.ui.export.NNLearningPresenter

@Module
class NNLearningModule {

    @Provides
    fun provideExportPresenter(exportView: NNLearningInterface.View, sensorStuffManager: SensorStuffManager) = NNLearningPresenter(exportView, sensorStuffManager)

}