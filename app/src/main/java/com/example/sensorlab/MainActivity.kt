package com.example.sensorlab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var sm: SensorManager
    private var sStepDetector: Sensor? = null
    private var stepCount = 0
    private var averageStepLength = 0.0
    var prevPos: Location? = null
    var distanceMoved = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("STEPS", "App starting")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                0
            )
        }

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this)

        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sStepDetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        var sensorName = sStepDetector?.name
        Log.i("STEPS", "Sensor name: $sensorName")

        resetButton.setOnClickListener {
            Log.i("STEPS", "Reset button pressed")
            stepCount = 0;
            averageStepLength = 0.0;
            distanceMoved = 0f
            stepView.text = "Steps taken: $stepCount"
            stepLengthView.text = "Your average step length is: $averageStepLength"

        }

    }

    override fun onSensorChanged(event: SensorEvent?) {

        val sensorName = event?.sensor?.name
        Log.i("STEPS", "Sensor change detected in: $sensorName")

        if (event?.sensor == sStepDetector) {
            var sensorChange = event?.values?.size
            Log.i("STEPS", "Sensor change: $sensorChange")
            if (sensorChange != null && sensorChange > 0) {
                stepCount++
                Log.i("STEPS", "Steps taken: $stepCount")
                stepView.text = "Steps taken: $stepCount"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
        Log.i("STEPS", "Accuracy changed")
    }

    override fun onLocationChanged(pos: Location?) {
        if (pos != null && prevPos != null) {
            distanceMoved += pos.distanceTo(prevPos)
            averageStepLength = (distanceMoved / stepCount).toDouble()
            var formattedSteps = String.format("%.2f", averageStepLength)
            stepLengthView.text = "Your average step length is: $formattedSteps"
        }

        prevPos = pos
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    override fun onResume() {
        super.onResume()
        Log.i("STEPS", "Resuming app")

        if (sStepDetector != null) {
            sStepDetector?.also {
                Log.i("STEPS", "Registering detector")
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            Log.i("STEPS", "No detector")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("STEPS", "Pausing app")
        sm.unregisterListener(this)
    }
}