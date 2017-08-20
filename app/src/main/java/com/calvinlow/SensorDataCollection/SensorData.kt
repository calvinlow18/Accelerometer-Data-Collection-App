package com.calvinlow.SensorDataCollection


/**
 * Created by calvinlow on 31/07/2017.
 */
class SensorData(val timestamp: Long, val mode: String, var x: Float, var y: Float, var z: Float) {
    override fun toString(): String {
        return "$timestamp,$mode,$x,$y,$z"
    }
}