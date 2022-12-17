package com.example.android_nas_sync.utils

import java.util.*

class TimeUtils {
    companion object {
        fun unixTimestampToFormatted(timestamp:Double){
        }

        fun unixTimestampToHoursAndMins(timestampSecs:Long):String{
            val date = Date(timestampSecs * 1000)
            val now = Date()
            val diffMs = now.time - date.time
            val diffMins = diffMs / 1000 / 60
            val hours = kotlin.math.floor(diffMins / 60.0 ).toInt()
            val hoursAsMins = hours * 60
            val mins = (diffMins - hoursAsMins).toInt()
            return if (hours > 0)  "$hours hours $mins mins"
                   else "$diffMins mins"
        }
        fun unixTimestampNowSecs():Long{
            return System.currentTimeMillis() / 1000
        }
    }
}