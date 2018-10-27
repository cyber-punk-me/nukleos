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

        fun showCoundtown()

        fun enableResetButton()
        fun disableResetButton()
        fun showSaveArea()
        fun saveDataFile(data: String)
        fun saveDataStop(content: String)
        fun sharePlainText(content: String)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {

        abstract fun onBufferDataPressed(dataType: Int)
        abstract fun onResetPressed()
        abstract fun onSavePressed()
    }
}