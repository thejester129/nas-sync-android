package com.example.android_nas_sync.common

import java.lang.Math.floor
import java.lang.Math.round
import java.util.*
import kotlin.math.roundToInt

class TimeUtils {
    companion object {
        fun unixTimestampToFormatted(timestamp:Double){
        }

        fun unixTimestampToHoursAndMins(timestampSecs:Long):String{
            val date = Date(timestampSecs * 1000)
            val now = Date()
            val diffMs = now.time - date.time
            val diffMins = diffMs / 1000 / 60
            val hours = kotlin.math.floor(diffMins / 60.0 )
            val hoursAsMins = hours * 60
            val mins = diffMins - hoursAsMins
            return if (hours > 0)  "$hours hours $mins mins"
                   else "$diffMins mins"
        }
        fun unixTimestampNowSecs():Long{
            return System.currentTimeMillis() / 1000
        }
    }
}