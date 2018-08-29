package me.cyber.nukleos.dagger

import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.cyber.nukleos.ui.MainActivity
import me.cyber.nukleos.ui.control.SensorControlFragment
import me.cyber.nukleos.ui.export.ExportFragment
import me.cyber.nukleos.ui.charts.ChartsFragment
import me.cyber.nukleos.ui.find.FindSensorFragment


@Module
abstract class BuildersModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [FindSensorViewModule::class, FindSensorModule::class])
    abstract fun bindScanDeviceFragment(): FindSensorFragment

    @ContributesAndroidInjector(modules = [SensorControlViewModule::class, SensorControlModule::class])
    abstract fun bindControlDeviceFragment(): SensorControlFragment

    @ContributesAndroidInjector(modules = [ChartsViewModule::class, ChartsModule::class])
    abstract fun bindGraphFragment(): ChartsFragment

    @ContributesAndroidInjector(modules = [NNLearningViewModule::class, NNLearningModule::class])
    abstract fun bindExportFragment(): ExportFragment
}