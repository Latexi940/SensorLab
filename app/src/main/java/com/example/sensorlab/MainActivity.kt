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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var sm: SensorManager
    private var sStepSensor: Sensor? = null
    private var stepCount = 0
    private var averageStepLength = 0.0
    private var prevPos: Location? = null
    private var distanceMoved = 0f

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

        if (sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sStepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        } else if (sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            sStepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        } else {
            Toast.makeText(this, "No usable sensors found", Toast.LENGTH_SHORT)
        }

        resetButton.setOnClickListener {
            Log.i("STEPS", "Reset button pressed")
            stepCount = 0;
            averageStepLength = 0.0;
            distanceMoved = 0f
            stepView.text = "Steps taken: $stepCount"
            stepLengthView.text = "Your average step length is: $averageStepLength"
            distanceView.text = "Distance traveled: $distanceMoved"
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val sensorName = event?.sensor?.name
        Log.i("STEPS", "Sensor change detected in: $sensorName")

        if (event?.sensor == sStepSensor) {
            stepCount++
            Log.i("STEPS", "Steps taken: $stepCount")
            stepView.text = "Steps taken: $stepCount"

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
        Log.i("STEPS", "Accuracy changed")
    }

    override fun onLocationChanged(pos: Location?) {
        Log.i("STEPS", "Location changed")
        if (pos != null && prevPos != null) {
            distanceMoved += pos.distanceTo(prevPos)
            averageStepLength = (distanceMoved / stepCount).toDouble()
            var formattedSteps = String.format("%.2f", averageStepLength)
            var formattedDistance = String.format("%.2f", distanceMoved)
            stepLengthView.text = "Your average step length is: $formattedSteps"
            distanceView.text = "Distance traveled: $formattedDistance"
        }

        prevPos = pos
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
        Log.i("STEPS", "Location provider enabled")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.i("STEPS", "Location provider disabled")
    }

    override fun onResume() {
        super.onResume()
        Log.i("STEPS", "Resuming app")

        if (sStepSensor != null) {
            sStepSensor?.also {
                Log.i("STEPS", "Registering sensor")
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            Log.i("STEPS", "No sensor to register")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("STEPS", "Pausing app")
        sm.unregisterListener(this)
    }
}