package me.cyber.nukleos.ui.export

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_export.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleus.R
import javax.inject.Inject



class NNLearningFragment : BaseFragment<NNLearningInterface.Presenter>(), NNLearningInterface.View {

    companion object {
        fun newInstance() = NNLearningFragment()
        private const val REQUEST_WRITE_EXTERNAL_CODE = 2
    }

    @Inject
    lateinit var exportPresenter: NNLearningPresenter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        attachPresenter(exportPresenter)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.layout_export, container, false).apply { setHasOptionsMenu(true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(exportPresenter) {
            button_start_collecting.setOnClickListener { onBufferDataPressed() }
            button_reset_collecting.setOnClickListener { onResetPressed() }
//            powerful_state_btn_start.setOnClickListener{onStateButtonStartPressed()}
//            powerful_state_btn_stop.setOnClickListener{onStateButtonStopPressed()}
          /*  button_share.setOnClickListener { onSendPressed() }
            button_save.setOnClickListener { onSavePressed() }*/
        }
    }

    override fun hideSaveArea() {
        /* button_save.visibility = View.INVISIBLE
        button_share.visibility = View.INVISIBLE
        save_export_title.visibility = View.INVISIBLE
        save_export_subtitle.visibility = View.INVISIBLE*/
    }

    override fun showSaveArea() {
        /*button_save.visibility = View.VISIBLE
        button_share.visibility = View.VISIBLE
        save_export_title.visibility = View.VISIBLE
        save_export_subtitle.visibility = View.VISIBLE*/
    }

    override fun sendData(content: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    override fun saveDataFile(content: String) {
   // TODO Сделай отправку. Не будь говном
                Log.e("---","========   уууууу   =======")
//                powerful_state_btn_start.startLoading()
    }

    override fun saveDataStop(content: String) {
//        powerful_state_btn_start.loadingFailed()
    }

    override fun enableStartCollectingButton() {
        button_start_collecting.isEnabled = true
    }

    override fun disableStartCollectingButton() {
        button_start_collecting.isEnabled = false
    }

    override fun showNotStreamingErrorMessage() {
        Toast.makeText(activity, "Please, connect sensor and start scanning", Toast.LENGTH_SHORT).show()
    }

    override fun showCollectionStarted() {
        button_start_collecting?.text = getString(R.string.stop_collecting)
    }

    override fun showCollectionStopped() {
        button_start_collecting?.text = getString(R.string.start_collecting)
    }

    override fun showCollectedData(data: Int) {
        points_count.text = data.toString()
    }

    override fun enableResetButton() {
        button_reset_collecting.isEnabled = true
    }

    override fun disableResetButton() {
        button_reset_collecting.isEnabled = false
    }
}