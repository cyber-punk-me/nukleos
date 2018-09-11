package me.cyber.nukleos.ui.export

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView


interface NNLearningInterface {

    interface View : BaseView {

        fun enableStartCollectingButton()
        fun disableStartCollectingButton()
        fun showNotStreamingErrorMessage()
        fun showCollectionStarted()
        fun showCollectionStopped()
        fun showCollectedData(data: Int)
        fun enableResetButton()
        fun disableResetButton()
        fun hideSaveArea()
        fun showSaveArea()
        fun saveDataFile(content: String)
        fun saveDataStop(content: String)
        fun sendData(content: String)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {

        abstract fun onBufferDataPressed()
        abstract fun onResetPressed()
        abstract fun onSavePressed()
        abstract fun onSendPressed()
        abstract fun onStateButtonStartPressed()
        abstract fun onStateButtonStopPressed()

    }
}