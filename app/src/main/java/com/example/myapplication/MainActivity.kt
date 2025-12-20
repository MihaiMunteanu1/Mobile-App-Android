package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.core.TAG
import com.example.myapplication.ui.theme.MyAppTheme
import kotlinx.coroutines.launch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : ComponentActivity() {
    private val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private var lightSensor: Sensor? = null
    private var lastReportedLux: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        createLightNotificationChannel()
        requestNotificationPermission()
        setContent {
            Log.d(TAG, "onCreate")
            MyApp {
                MyAppNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensor ->
            sensorManager.registerListener(
                lightSensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        lifecycleScope.launch {
            (application as MyApplication).container.itemRepository.openWsClient()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
        lifecycleScope.launch {
            (application as MyApplication).container.itemRepository.closeWsClient()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    /* requestCode = */ 0
                )
            }
        }
    }
    private fun createLightNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LIGHT_CHANNEL_ID,
                "Light sensor updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when ambient light changes."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showLightChangedNotification(lux: Float) {
        if (!canPostNotifications()) {
            Log.d(TAG, "Notification permission not granted; skipping light update.")
            return
        }
        val notification = NotificationCompat.Builder(this, LIGHT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Luminozitate schimbata")
            .setContentText("Nivel curent: %.1f lux".format(lux))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(LIGHT_NOTIFICATION_ID, notification)
        } catch (exception: SecurityException) {
            Log.w(TAG, "Unable to post notification for light sensor update.", exception)
        }
    }
    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }


    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_LIGHT) return
            val lux = event.values.firstOrNull() ?: return
            val lastLux = lastReportedLux
            val luxChanged = lastLux == null || lux!= lastLux
            if (luxChanged) {
                lastReportedLux = lux
                showLightChangedNotification(lux)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    companion object {
        private const val LIGHT_CHANNEL_ID = "light_sensor_channel"
        private const val LIGHT_NOTIFICATION_ID = 2001
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    Log.d("MyApp", "recompose")
    MyAppTheme {
        Surface {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewMyApp() {
    MyApp {
        MyAppNavHost()
    }
}
