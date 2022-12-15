package com.example.android_nas_sync.common

class TimeUtils {
    companion object {
        fun unixTimestampToFormatted(timestamp:Double){
        }

        fun unixTimestampToHoursAndMins(timestamp:Long):String{
            return "1 hours 15 mins"
        }
        fun unixTimestampNow():Long{
            return System.currentTimeMillis() / 1000
        }
    }
}