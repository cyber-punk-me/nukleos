package me.cyber.nukleos.ui.export

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_export.*
import me.cyber.nukleos.App
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.api.CompleteResponse
import me.cyber.nukleos.api.DataRequest
import me.cyber.nukleus.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private const val REQUEST_WRITE_EXTERNAL_CODE = 2

class NNLearningFragment : BaseFragment<NNLearningInterface.Presenter>(), NNLearningInterface.View {

    companion object {
        fun newInstance() = NNLearningFragment()
        private const val THE_END_OF_TIME = "00:00:00"
        private const val TIMER_FORMAT = "%02d:%02d:%02d"
        const val TIMER_COUNT = 5L
        const val LEARNING_TIME = 10
        private const val REQUEST_WRITE_EXTERNAL_CODE = 2
        private const val DIR_NAME = "/nukleos"
        private const val FILE = "huihuihui.csv"
        private const val FILE_NAME = "myo_emg_export_"
        private const val EXTENSION = 0
        private const val FLEXION = 1
        private const val ADDUCTION = 2
        private const val ABDUCTION = 3
    }

    override fun sharePlainText(content: String) {
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

            button_save_and_send.setOnClickListener { onSavePressed() }

            extention_save.setOnClickListener { onBufferDataPressed(EXTENSION) }
            flexion_save.setOnClickListener { onBufferDataPressed(FLEXION) }
            adduction_save.setOnClickListener { onBufferDataPressed(ADDUCTION) }
            abduction_save.setOnClickListener { onBufferDataPressed(ABDUCTION) }
        }
    }


    override fun showSaveArea() {
        save_export_title.visibility = View.VISIBLE
    }

    @Throws(IOException::class)
    private fun writeFile(stringToSave: String) {
        var outStream: FileOutputStream? = null
        try {
            outStream = activity?.openFileOutput(FILE, Context.MODE_PRIVATE)
            outStream!!.write(stringToSave.toByteArray())
            sendData(stringToSave)
            val file = getFile()
            if (file.exists()) {
                Log.e("======", "                   ${file.absolutePath}                     File saved.")
//                sendCSVfile(getFile())
            } else {
                Log.e("======", "                                        File NOT      saved.")
            }
        } catch (e: Exception) {
            Log.e("======", "                   PIZDOSSS              ${e.localizedMessage}")
        } finally {
            outStream!!.close()
        }
    }

    private fun getFile() = File(activity?.filesDir, FILE)

    private fun sendData(data: String) = App.applicationComponent.getApiHelper().api.postData(UUID.randomUUID(), data, "csv")
            .subscribe({ Log.e("-----", "======${it.dataId}") }
                    , { Log.e("=Error=", "=============${it.message}============") })

    override fun saveDataFile(data: String) {
        writeFile(data)
    }

    override fun saveDataStop(content: String) {
        button_start_collecting.isEnabled = false
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

    override fun enableResetButton() {
//        button_reset_collecting.isEnabled = true
    }

    override fun disableResetButton() {
//        button_reset_collecting.isEnabled = false
    }

    override fun showCoundtown() {
        object : CountDownTimer(TIMER_COUNT * 1000, 1000) {
            override fun onFinish() {
                countdown_text.text = THE_END_OF_TIME
                countdown_layout.visibility = GONE
            }

            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                countdown_text.text = String.format(TIMER_FORMAT,
                        hours,
                        minutes - TimeUnit.HOURS.toMinutes(hours),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes))
            }
        }.apply { countdown_layout.visibility = VISIBLE }.start()
    }
}