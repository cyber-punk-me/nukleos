package me.cyber.nukleos.ui.export

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView


interface NNLearningInterface {

    interface View : BaseView {

        fun enableStartCollectingButton()
        fun disableStartCollectingButton()
        fun showNotStreamingErrorMessage()

        fun flexionStart()
        fun flexionStop()

        fun extensionStart()
        fun extensionStop()

        fun adductionStart()
        fun adductionStop()

        fun abductionStart()
        fun abductionStop()

        fun showCollectionStarted()
        fun showCollectionStopped()

        fun showCoundtown()

        fun enableResetButton()
        fun disableResetButton()
        fun hideSaveArea()
        fun showSaveArea()
        fun saveDataFile(data: String)
        fun saveDataStop(content: String)
        fun sendData(content: String)

        fun saveCsvFile(content: String)
        fun sharePlainText(content: String)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {

        abstract fun onBufferDataPressed(dataType: Int)
        abstract fun onResetPressed()
        abstract fun onSavePressed()
        abstract fun onSendPressed()
    }
}