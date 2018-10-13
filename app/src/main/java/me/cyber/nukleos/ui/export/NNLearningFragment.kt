package me.cyber.nukleos.ui.export

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.support.v4.content.ContextCompat
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val REQUEST_WRITE_EXTERNAL_CODE = 2
class NNLearningFragment : BaseFragment<NNLearningInterface.Presenter>(), NNLearningInterface.View {


    companion object {
        fun newInstance() = NNLearningFragment()
        private const val THE_END_OF_TIME = "00:00:00"
        private const val TIMER_FORMAT = "%02d:%02d:%02d"
        const val TIMER_COUNT = 5L
        const val LEARNING_TIME = 3
        private const val REQUEST_WRITE_EXTERNAL_CODE = 2
        private const val DIR_NAME = "/nukleos"
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
    private var fileContentToSave: String? = null

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
            //            button_start_collecting.setOnClickListener { onBufferDataPressed() }
//            button_reset_collecting.setOnClickListener { onResetPressed() }
//            button_share.setOnClickListener { onSendPressed() }
//            button_save.setOnClickListener { onSavePressed() }
            button_save_and_send.setOnClickListener { onSavePressed() }


            extention_save.setOnClickListener { onBufferDataPressed(EXTENSION) }
            flexion_save.setOnClickListener { onBufferDataPressed(FLEXION) }
            adduction_save.setOnClickListener { onBufferDataPressed(ADDUCTION) }
            abduction_save.setOnClickListener { onBufferDataPressed(ABDUCTION) }
        }
    }

    override fun hideSaveArea() {
//        button_save.visibility = View.INVISIBLE
//        button_share.visibility = View.INVISIBLE
//        save_export_title.visibility = View.INVISIBLE
//        save_export_subtitle.visibility = View.INVISIBLE
    }

    override fun showSaveArea() {
//        button_save.visibility = View.VISIBLE
//        button_share.visibility = View.VISIBLE
        save_export_title.visibility = View.VISIBLE
//        save_export_subtitle.visibility = View.VISIBLE
    }

    override fun sendData(content: String) =
            startActivity(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.CATEGORY_APP_MESSAGING, content)
                type = "text/plain"
            })


    override fun saveCsvFile(content: String) {
        context?.apply {
            val hasPermission = (ContextCompat.checkSelfPermission(this,
                    WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED)
            if (hasPermission) {
                saveDataFile(content)
            } else {
                fileContentToSave = content
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_EXTERNAL_CODE)
            }
        }
    }

    override fun saveDataFile(data: String) {
        val storageDir =
                File("${Environment.getExternalStorageDirectory().absolutePath}/myo_emg")
        storageDir.mkdir()
        val outfile = File(storageDir, "myo_emg_export_${System.currentTimeMillis()}.csv")
        val fileOutputStream = FileOutputStream(outfile)
        fileOutputStream.write(data.toByteArray())
        fileOutputStream.close()
        Toast.makeText(activity, "Saved to: ${outfile.path}", Toast.LENGTH_LONG).show()

    }

    override fun flexionStart() {
        TODO("СГИБАНИЕ")
    }

    override fun flexionStop() {
        TODO("СГИБАНИЕ СТОП")
    }

    override fun extensionStart() {
        TODO("РАЗГИБАНИЕ")
    }

    override fun extensionStop() {
        TODO("РАЗГИБАНИЕ СТОП")
    }

    override fun adductionStart() {
        TODO("Приведение")
    }

    override fun adductionStop() {
        TODO("Приведение стоп")
    }

    override fun abductionStart() {
        TODO("Отведение")
    }

    override fun abductionStop() {
        TODO("Отведение")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fileContentToSave?.apply { saveDataFile(this) }
                } else {
                    Log.e("notPErmission", "     grantResults.isNotEmpty()= ${grantResults.isNotEmpty()}")
                    Log.e("notPErmission", "     grantResults[0] == PackageManager.PERMISSION_GRANTED= ${grantResults[0] == PackageManager.PERMISSION_GRANTED}")
                    Log.e("notPErmission", "     grantResults=  ${grantResults[0].toString()}")
                    Toast.makeText(activity, "ТОБИ ПИЗДА", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}