package me.cyber.nukleos.ui.find

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView
import me.cyber.nukleos.ui.control.SensorModel


interface FindSensorInterface {

    interface View : BaseView {
        fun showPreparingText()
        fun showEmptyListText()
        fun hideEmptyListText()
        fun showFindLoader()
        fun hideFindLoader()
        fun addSensorToList(sensorModel: SensorModel)
        fun getSensorModel(index: Int) : SensorModel?
        fun clearSensorList()
        fun populateSensorList(list: List<SensorModel>)
        fun showFindError(reason: String?)
        fun showFindSuccess()
        fun goToSensorControl()
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {
        abstract fun onFindButtonClicked()
        abstract fun onSensorSelected(index: Int)
    }
}