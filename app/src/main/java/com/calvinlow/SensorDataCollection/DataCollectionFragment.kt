package com.calvinlow.SensorDataCollection


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.google.common.io.Files
import kotlinx.android.synthetic.main.fragment_data_collection.*
import java.io.File
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter
import java.text.DecimalFormat


/**
 * A simple [Fragment] subclass.
 */
class DataCollectionFragment : Fragment(), SensorEventListener2 {

    var currentMode: String = "walking"
//    private var lpfGravity: AveragingFilter? = null
//    private var linearAccelerationFilterLpf: LinearAccelerationAveraging? = null
    private var lpfAccelerationSmoothing: LowPassFilter? = null
//    SENSOR_DELAY_GAME (20,000 microsecond delay) - About 50 data per second
//    SENSOR_DELAY_UI (60,000 microsecond delay) - About 17 data per second
//    SENSOR_DELAY_FASTEST (0 microsecond delay)
    private var storeSnackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_data_collection, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        init()
    }

    private val sensorDataList: ArrayList<SensorData> = arrayListOf()
    var isAccelerometerStarted: Boolean = false

    override fun onFlushCompleted(p0: Sensor?) {
        Log.d("onFlushCompleted", "Flush Completed.")    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("onAccuracyChanged", "Accuracy Changed.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when(event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    if (isAccelerometerStarted) {
                        val lpfData = lpfAccelerationSmoothing?.filter(event.values)

                        lpfData?.let {
                            sensorDataList.add(SensorData(System.currentTimeMillis(), currentMode, it[0], it[1], it[2]))
                            updateSensorDataUI(it)
                        }

                    }
                }
            }
        }
    }

    private fun updateSensorDataUI(value: FloatArray) {
        accelerometer_x_value_label.text = DecimalFormat("###.##").format(value[0]).toString()
        accelerometer_y_value_label.text = DecimalFormat("###.##").format(value[1]).toString()
        accelerometer_z_value_label.text = DecimalFormat("###.##").format(value[2]).toString()
    }

    private fun resetSensorDataUI() {
        accelerometer_x_value_label.text = "0.0"
        accelerometer_y_value_label.text = "0.0"
        accelerometer_z_value_label.text = "0.0"
    }

    val sensorManager: SensorManager by lazy {
        activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun init() {
        storeSnackbar = Snackbar.make(view!!, "Storing data to csv...", Snackbar.LENGTH_INDEFINITE)

        lpfAccelerationSmoothing = LowPassFilter()

        registerSensorListener(SensorManager.SENSOR_DELAY_FASTEST)

        delay_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("Item Selected", position.toString())
                when(position) {
                    0 -> registerSensorListener(SensorManager.SENSOR_DELAY_FASTEST)
                    1 -> registerSensorListener(SensorManager.SENSOR_DELAY_GAME)
                    2 -> registerSensorListener(SensorManager.SENSOR_DELAY_UI)
                    3 -> registerSensorListener(SensorManager.SENSOR_DELAY_NORMAL)
                }
            }
        }

        accelerometer_start_button.setOnClickListener {
            if (isAccelerometerStarted) {
                stopAccelerometer()
                resetSensorDataUI()
                storeSnackbar?.show()
                val filename = filename_editText.text.toString()
                Thread({ storeToFile(list2CSV(), filename) }).start()
                filename_editText.setText("")
            } else{
                startAccelerometer()
            }
        }
        filename_editText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                text?.let {
                    filename_editText.error = null
                    if (checkFileExists(text.toString())) {
                        filename_editText.error = "File exists"
                    }
                }
            }

        })
    }

    fun startAccelerometer() {
        if (filename_editText.text.toString().isNullOrBlank()) {
            filename_editText.error = "Enter File Name First!"
            return
        }
        if (!filename_editText.text.toString().contains("_")) {
            filename_editText.error = "Must contain at least one _mode!"
            return
        } else {
            val textList = filename_editText.text.toString().split("_")
            currentMode = textList.last()
        }
        filename_editText.isEnabled = false
        sensorDataList.clear()
        accelerometer_start_button.text = "Stop Accelerometer"
        delay_spinner.isEnabled = false
        isAccelerometerStarted = true
    }

    fun stopAccelerometer() {
        filename_editText.isEnabled = true
        accelerometer_start_button.text = "Start Accelerometer"
        delay_spinner.isEnabled = true
        isAccelerometerStarted = false
    }

    fun registerSensorListener(delayMode: Int) {
        sensorManager.unregisterListener(this)

        lpfAccelerationSmoothing?.let {
            Log.d("Filter", "lpfAcceleration reset")
            it.reset()
        }

        val accelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)



//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, accelerometer, delayMode)
        when(delayMode) {
            SensorManager.SENSOR_DELAY_FASTEST -> println("SensorManager.SENSOR_DELAY_FASTEST")
            SensorManager.SENSOR_DELAY_GAME -> println("SensorManager.SENSOR_DELAY_GAME")
            SensorManager.SENSOR_DELAY_UI -> println("SensorManager.SENSOR_DELAY_UI")
            SensorManager.SENSOR_DELAY_NORMAL -> println("SensorManager.SENSOR_DELAY_NORMAL")
        }
    }

    fun list2CSV(): String {
        var sensorDataCSVStr = "timestamp,mode,x,y,z\n"
        sensorDataList.forEachIndexed { index, sensorData ->
            sensorDataCSVStr += sensorData
            if (index != sensorDataList.size - 1) {
                sensorDataCSVStr += "\n"
            }
        }
        Log.d("Filter", sensorDataCSVStr)
        return sensorDataCSVStr
    }

    fun checkFileExists(name: String): Boolean {
        val file = File(activity.filesDir, "$name.csv")
        return file.exists()
    }

    fun storeToFile(sensorDataStr: String, fileName: String) {
        val file = File(activity.filesDir, "$fileName.csv")
        Files.write(sensorDataStr.toByteArray(), file)
        storeSnackbar?.dismiss()
    }
}
