package me.cyber.nukleos.ui.export

import android.content.Context
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
import me.cyber.nukleus.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class NNLearningFragment : BaseFragment<NNLearningInterface.Presenter>(), NNLearningInterface.View {

    companion object {
        fun newInstance() = NNLearningFragment()
        private const val THE_END_OF_TIME = "00:00:00"
        private const val TIMER_FORMAT = "%02d:%02d:%02d"
        const val TIMER_COUNT = 5L
        const val LEARNING_TIME = 10
        private const val FILE = "huihuihui.csv"
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


    init {
        val huial = "ЖОПА  36,-23,2,2,1,-5,7,-14,-17,31,4,-2,1,-3,-1,11,18,-41,-2,0,3,-7,23,-5,2,23,-3,-2,-3,-5,-3,-17,-70,37,-2,-5,-1,16,7,4,7,-70,2,5,-4,-4,-7,1\n" +
                "19,36,-23,2,2,1,-5,7,-14,-17,31,4,-2,1,-3,-1,11,18,-41,-2,0,3,-7,23,-5,2,23,-3,-2,-3,-5,-3,-17,-70,37,-2,-5,-1,16,7,4,7,-70,2,5,-4,-4,-7,2,2,19,1,-2,-1,-8,6,25,14,26,4,2,3,9,0,1\n" +
                "11,18,-41,-2,0,3,-7,23,-5,2,23,-3,-2,-3,-5,-3,-17,-70,37,-2,-5,-1,16,7,4,7,-70,2,5,-4,-4,-7,2,2,19,1,-2,-1,-8,6,25,14,26,4,2,3,9,0,-12,11,-9,-1,0,-1,-4,-1,-8,13,17,-4,-1,-1,-11,-18,1\n" +
                "11,18,-41,-2,0,3,-7,23,-5,2,23,-3,-2,-3,-5,-3,-17,-70,37,-2,-5,-1,16,7,4,7,-70,2,5,-4,-4,-7,2,2,19,1,-2,-1,-8,6,25,14,26,4,2,3,9,0,-12,11,-9,-1,0,-1,-4,-1,-8,13,17,-4,-1,-1,-11,-18,1\n" +
                "-12,11,-9,-1,0,-1,-4,-1,-8,13,17,-4,-1,-1,-11,-18,3,-2,42,5,0,0,-6,-3,-5,-7,-41,-1,-2,-3,-2,2,4,-2,-17,-8,-2,3,36,13,-16,-23,28,-1,-2,-4,-26,-7,8,19,-2,0,2,-4,-8,-9,-9,-1,-50,1,-2,-2,10,5,1\n" +
                "3,-2,42,5,0,0,-6,-3,-5,-7,-41,-1,-2,-3,-2,2,4,-2,-17,-8,-2,3,36,13,-16,-23,28,-1,-2,-4,-26,-7,8,19,-2,0,2,-4,-8,-9,-9,-1,-50,1,-2,-2,10,5,-1,-21,-35,-12,-2,1,-29,-26,18,2,22,-1,0,4,13,5,1\n" +
                "4,-2,-17,-8,-2,3,36,13,-16,-23,28,-1,-2,-4,-26,-7,8,19,-2,0,2,-4,-8,-9,-9,-1,-50,1,-2,-2,10,5,-1,-21,-35,-12,-2,1,-29,-26,18,2,22,-1,0,4,13,5,-14,-4,-9,-6,-8,-7,4,10,4,10,36,0,2,2,1,0,1"

        sendData(huial)

    }
    private fun getFile() = File(activity?.filesDir, FILE)

    private fun sendData(data: String) = App.applicationComponent.getApiHelper().api.postData(UUID.randomUUID(), data, "csv")
            .subscribe({ Log.e("-----", "======${it.id}")
                Log.e("-----", "======${it.path}")
                Log.e("-----", "======${it.name}")
                Log.e("-----", "======${it.tags}")
            }
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