package com.example.gpsreceiver

class Model {
    private var lat : Double = 0.0
    private var lon: Double = 0.0
    private var height: Double = 0.0
    private var speed: Double = 0.0

    fun getLat(): Double {
        return lat
    }

    fun getLon(): Double {
        return lon
    }

    fun getHeight(): Double {
        return height
    }

    fun getSpeed(): Double {
        return speed
    }

    fun setLat(lat: Double) {
        this.lat = lat
    }

    fun setLon(lon: Double) {
        this.lon = lon
    }

    fun setHeight(height: Double) {
        this.height = height
    }

    fun setSpeed(speed: Double) {
        this.speed = speed
    }

}