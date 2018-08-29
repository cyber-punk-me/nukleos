package me.cyber.nukleos.dagger

import dagger.Binds
import dagger.Module
import me.cyber.nukleos.ui.export.ExportFragment
import me.cyber.nukleos.ui.export.NNLearningInterface

@Module
abstract class NNLearningViewModule {

    @Binds
    abstract fun provideNNLearningView(exportFragment: ExportFragment): NNLearningInterface.View
}