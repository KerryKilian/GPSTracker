package com.example.gpsreceiver;

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.*
import java.time.Instant
import java.time.format.DateTimeFormatter

class MyLocationListener(
    private val context: Context,
    private val model: Model,
    private val Update: Update
) : LocationListener {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationChanged(location: Location) {
        model.setLat(location.latitude)
        model.setLon(location.longitude)
        model.setHeight(location.altitude)
        model.setSpeed(location.speed.toDouble())


        try {
            val file = File(context.filesDir, "gps_tracker.csv")
            // Create FileOutputStream with append mode set to true
            val fileOutputStream = FileOutputStream(file, true)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)

            // Write the text to the file
            outputStreamWriter.write("${DateTimeFormatter.ISO_INSTANT.format(Instant.now())};${model.getLat()};${model.getLon()};${model.getHeight()}\n")
            outputStreamWriter.flush()

            // Close the streams
            outputStreamWriter.close()
            fileOutputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        Update.getValueUpdates()
        Update.updatePath()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Handle status changes here
    }

    override fun onProviderEnabled(provider: String) {
        // Handle provider enabled
    }

    override fun onProviderDisabled(provider: String) {
        // Handle provider disabled
    }
}