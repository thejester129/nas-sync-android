package com.example.android_nas_sync.common

import java.util.*

class TimeUtils {
    companion object {
        fun unixTimestampToFormatted(timestamp:Double){
        }

        fun unixTimestampToHoursAndMins(timestampSecs:Long):String{
            Date(timestamp * 1000)
            return "1 hours 15 mins"
        }
        fun unixTimestampNowSecs():Long{
            return System.currentTimeMillis() / 1000
        }
    }
}