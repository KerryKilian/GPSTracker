package com.example.gps_tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.gps_tracker.ui.theme.GPS_TrackerTheme
import com.example.gpsreceiver.Model
import com.example.gpsreceiver.MyLocationListener
import com.example.gpsreceiver.Update
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : ComponentActivity() {

    // model values
    private val lat = mutableStateOf(0.0)
    private val lon = mutableStateOf(0.0)
    private val height = mutableStateOf(0.0)
    private val speed = mutableStateOf(0.0)
    private lateinit var model: Model

    // provider values
    private val receiverMethods = listOf("GPS", "Network")
    private val selectedText = mutableStateOf(receiverMethods[0])

    // location values
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: MyLocationListener
    private val locationPermissionCode = 2

    // line
    private var path: Path? = Path()
    private var csvValues = mutableListOf<List<String>>()
    private val csvValuesLiveData = MutableLiveData<MutableList<List<String>>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GPS_TrackerTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(64.dp)
                    ) {
                        MyUI()

                    }
                }
            }
        }

        // Create instance of our data model
        model = Model()

        // create listener
        locationListener = MyLocationListener(this, model, object : Update {
            override fun getValueUpdates() {
                lat.value = model.getLat()
                lon.value = model.getLon()
                height.value = model.getHeight()
                speed.value = model.getSpeed()
            }

            override fun updatePath() {
                csvValues = readXml()
                makePath()
            }
        })


        getLocation(LocationManager.GPS_PROVIDER)
    }

    /**
     * get Location with a specific provider
     */
    private fun getLocation(provider: String) {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }


        locationManager.requestLocationUpdates(provider, 5000, 0f, locationListener)


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * round a double value to 2 digits
     */
    fun round2Digits(number: Double): Double {
        return Math.round(number * 100.0) / 100.0
    }

    /**
     * round a double value to 6 digits
     */
    fun round6Digits(number: Double): Double {
        return Math.round(number * 1000000.0) / 1000000.0
    }

    @Composable
    fun MyUI() {
        var expanded by remember { mutableStateOf(false) }


        var textfieldSize by remember { mutableStateOf(Size.Zero) }

        val icon = if (expanded)
            Icons.Filled.KeyboardArrowUp
        else
            Icons.Filled.KeyboardArrowDown


        Text(
            text = "GPS Tracker",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
//        Spacer(modifier = Modifier.height(32.dp))
//        Label(text = "Latitude")
//        Value(text = "" + round6Digits(lat.value))
//        Label(text = "Longitude")
//        Value(text = "" + round6Digits(lon.value))
//        Label(text = "Height")
//        Value(text = "" + round2Digits(height.value))
//        Label(text = "Speed")
//        Value(text = "" + round2Digits(speed.value))
//        Box() {
//            OutlinedTextField(
//                value = selectedText.value,
//                onValueChange = { selectedText.value = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .onGloballyPositioned { coordinates ->
//                        //This value is used to assign to the DropDown the same width
//                        textfieldSize = coordinates.size.toSize()
//                    },
//                label = { Text("Receiver Method") },
//                trailingIcon = {
//                    Icon(icon, "contentDescription",
//                        Modifier.clickable { expanded = !expanded })
//                }
//            )
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false },
//                modifier = Modifier
//                    .width(with(LocalDensity.current) { textfieldSize.width.toDp() })
//            ) {
//                receiverMethods.forEach { label ->
//                    DropdownMenuItem(onClick = {
//                        selectedText.value = label
//
//                        // stop asking for location
//                        locationManager.removeUpdates(locationListener)
//                        if (label == receiverMethods[0]) {
//                            getLocation(LocationManager.GPS_PROVIDER)
//                        } else {
//                            getLocation(LocationManager.NETWORK_PROVIDER)
//                        }
//                    }) {
//                        Text(text = label)
//                    }
//                }
//            }
            Spacer(modifier = Modifier.padding(12.dp))
            DrawZoomedPath(coordinates = csvValues)
//            DrawPath(coordinates = csvValues)
//            CoordinateCanvas(coordinates = csvValues)
//            Canvas(
//                modifier = Modifier.fillMaxSize(),
//                onDraw = {
//
//
//                    try {
//                        var minMaxValues: Array<Float> = getMinMaxValues()
//                        val canvasWidth = size.width
//                        val canvasHeight = size.height
//
//                        val scaleX = canvasWidth / (minMaxValues[2] - minMaxValues[0])
//                        val scaleY = canvasHeight / (minMaxValues[3] - minMaxValues[1])
//
//                        // Apply scaling to the canvas
//                        scale(scaleX, scaleY) {
//                            val startX = minMaxValues[0]
//                            val startY = minMaxValues[1]
//                            val endX = minMaxValues[2]
//                            val endY = minMaxValues[3]
//
//                            drawLine(
//                                color = Color.Black,
//                                strokeWidth = 5f,
//                                start = Offset(startX, startY),
//                                end = Offset(endX, endY)
//                            )
//                        }
//                    } catch (e: IndexOutOfBoundsException){
//                        Log.d("CSV", "Cannot read CSV")
//                    }
//
//
//                        // Additional drawing operations within the transformed coordinate system
//
//
////                    drawLine(
////                        color = Color.Black,
////                        strokeWidth = 5f,
////                        start = Offset(10f, 10f),
////                        end = Offset(200f, 200f),
//////                        brush = Stroke(width = 5f)
////                    )
////                    getMinMaxValues()
////                    val geoBreite = csvValues
////                    for (i in 0..csvValues.size-2) {
////                        val startPoint = Offset(csvValues[i][1].toFloat(), csvValues[i][2].toFloat())
////                        val endPoint = Offset(csvValues[i+1][1].toFloat(), csvValues[i+1][2].toFloat())
////                        drawLine(
////                            color = Color.Black,
////                            strokeWidth = 5f,
////                            start = startPoint,
////                            end = endPoint,
////                        )
////                    }
//                }
//            )
//        }
    }

    fun getMinMaxValues(list: List<List<String>>): Array<Float> {
        var minX = list[0][1].toFloat()
        var minY = list[0][2].toFloat()
        var maxX = list[0][1].toFloat()
        var maxY = list[0][2].toFloat()
        for (i in 0..list.size - 2) {
            var currentX = list[i][1].toFloat()
            var currentY = list[i][2].toFloat()

            if (currentX < minX) {
                minX = currentX
            }
            if (currentY < minY) {
                minY = currentY
            }
            if (currentX > maxX) {
                minX = currentX
            }
            if (currentY > maxY) {
                minY = currentY
            }
        }
        return arrayOf(minX, minY, maxX, maxY)
    }

    fun makePath() {
//        for (i in 0..csvValues.size) {
//            path?.lineTo(csvValues[i][0].toFloat(), csvValues[i][1].toFloat())
//
//        }
//        Toast.makeText(this, csvValues[0][0], Toast.LENGTH_SHORT).show()
    }

    fun readXml(): MutableList<List<String>> {
        var csvValues = mutableListOf<List<String>>()
        try {
            val file = File(this.filesDir, "gps_tracker.csv")
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var line: String?

            // Read lines from the file
            while (bufferedReader.readLine().also { line = it } != null) {
                if (line?.isNotEmpty() == true) {
                    stringBuilder.append(line).append("\n")
                }
            }

            // Close the streams
            bufferedReader.close()
            inputStreamReader.close()
            fileInputStream.close()

            // Process the content of the file
            val fileContent = stringBuilder.toString()
            // Do something with the file content

            val rows = fileContent.split("\n")


            for (row in rows) {
                if (row.isEmpty()) {
                    break
                }
                val values = row.split(";")
                csvValues.add(values)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return csvValues

    }

    fun getContext(): Context {
        return this
    }

    @Composable
    fun Label(text: String) {
        Text(
            text = text,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(10.dp)
        )
    }

    @Composable
    fun Value(text: String) {
        Text(
            text = text,
            color = Color.DarkGray,
            fontSize = 22.sp
        )
    }

    @Composable
    fun CoordinateCanvas(coordinates: List<List<String>>) {
        // Create a mutable state to hold the current list of coordinates
        val currentCoordinates = remember { mutableStateOf(coordinates) }

        // Create a side effect to update the current coordinates whenever the list changes
        LaunchedEffect(coordinates) {
            currentCoordinates.value = coordinates
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Access the current coordinates from the mutable state
            val coords = currentCoordinates.value

            try {
                var minMaxValues: Array<Float> = getMinMaxValues(coords)

                Log.d("Canvas", minMaxValues.joinToString())

                var points = calculateXY(size, minMaxValues)

                scale(1f, 1f) {
                    val startX = minMaxValues[1]
                    val startY = minMaxValues[0]
                    val endX = minMaxValues[3]
                    val endY = minMaxValues[2]

                    drawLine(
                        color = Color.Black,
                        strokeWidth = 100f,
                        start = Offset(37.33409f, -122f),
                        end = Offset(400f, -122f)
                    )
                }


            } catch (e: IndexOutOfBoundsException) {
                Log.d("Canvas", "Index Out of Bounds")
            }

            // Draw the coordinates on the canvas
//            try {
//                drawLine(
//                    color = Color.Black,
//                    strokeWidth = 5f,
//                    start = Offset(coords[0][1].toFloat(), coords[0][2].toFloat()),
//                    end = Offset(coords[0][1].toFloat() + 100f, coords[0][2].toFloat() + 100f)
//                )
//            } catch (e: IndexOutOfBoundsException) {
//                Log.d("Error", "IndexOutOfBounds")
//            }


//            try {
//                        var minMaxValues: Array<Float> = getMinMaxValues(coords)
//                        val canvasWidth = size.width
//                        val canvasHeight = size.height
//
//                        val scaleX = canvasWidth / (minMaxValues[2] - minMaxValues[0])
//                        val scaleY = canvasHeight / (minMaxValues[3] - minMaxValues[1])
//
//                        // Apply scaling to the canvas
//                        scale(scaleX, scaleY) {
//                            val startX = minMaxValues[0]
//                            val startY = minMaxValues[1]
//                            val endX = minMaxValues[2]
//                            val endY = minMaxValues[3]
//
//                            drawLine(
//                                color = Color.Black,
//                                strokeWidth = 5f,
//                                start = Offset(startX, startY),
//                                end = Offset(endX, endY)
//                            )
//                        }
//                    } catch (e: IndexOutOfBoundsException){
//                        Log.d("CSV", "Cannot read CSV")
//                    }
        }
    }

    fun calculateXY(size: Size, minMaxValues: Array<Float>): Array<Float> {
        var geoBreite = minMaxValues[2] - minMaxValues[0]
        var geoLänge = minMaxValues[3] - minMaxValues[1]
        var geoSize: Float
        if (geoBreite > geoLänge) {
            geoSize = geoBreite
        } else {
            geoSize = geoLänge
        }

        var x = ((geoLänge - minMaxValues[0]) * size.height) / geoSize
        var yI = ((geoBreite - minMaxValues[1]) * size.height) / geoSize
        var y = size.height - yI
        return arrayOf(x, y)
    }

    @Composable
    fun DrawPath(coordinates: List<List<String>>) {
        Canvas(modifier = Modifier.padding(40.dp)) {
            val path = Path()

            coordinates.forEachIndexed { index, point ->
                val x = point[1].toFloat()
                val y = point[2].toFloat()

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 20.0f)
            )
        }
    }

    @Composable
    fun DrawZoomedPath(coordinates: List<List<String>>) {
        var path by remember { mutableStateOf(Path()) }
        Canvas(modifier = Modifier.padding(12.dp)) {
//            val path = Path()

//            val minX = coordinates.minByOrNull { it[1].toFloat() }?.get(1) ?: 0f
//            val minY = coordinates.minByOrNull { it[2].toFloat() }?.get(2) ?: 0f
//            val maxX = coordinates.maxByOrNull { it[1].toFloat() }?.get(1) ?: 0f
//            val maxY = coordinates.maxByOrNull { it[2].toFloat() }?.get(2) ?: 0f
            try {
                val minMaxValues = getMinMaxValues(coordinates)
                val minX = minMaxValues[1]
                val minY = minMaxValues[0]
                val maxX = minMaxValues[3]
                val maxY = minMaxValues[2]


                val xScalingFactor = if (maxX != minX) {
                    size.width / (maxX - minX)
                } else {
                    1f
                }

                val yScalingFactor = if (maxY != minY) {
                    size.height / (maxY - minY)
                } else {
                    1f
                }

                coordinates.forEachIndexed { index, point ->
                    val x = (point[1].toFloat() - minX) * xScalingFactor
                    val y = (point[2].toFloat() - minY) * yScalingFactor

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(width = 20.0f)
                )
            } catch (e: java.lang.IndexOutOfBoundsException) {
                Log.d("Main", "Index out of bounds")
            }


        }
    }


}

