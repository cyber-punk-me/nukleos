package me.cyber.nukleos.ui.find

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView
import me.cyber.nukleos.model.SensorStuff


interface FindSensorInterface {

    interface View : BaseView {
        fun showPreparingText()
        fun showEmptyListText()
        fun hideEmptyListText()
        fun showFindLoader()
        fun hideFindLoader()
        fun addSensorToList(sensorStuff: SensorStuff)
        fun clearSensorList()
        fun populateSensorList(list: List<SensorStuff>)
        fun showFindError(reason: String?)
        fun showFindSuccess()
        fun goToSensorControl()
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {
        abstract fun onFindButtonClicked()
        abstract fun onSensorSelected(index: Int)
    }
}