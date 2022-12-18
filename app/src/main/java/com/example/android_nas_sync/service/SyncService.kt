package com.example.android_nas_sync.service

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.*
import androidx.core.app.NotificationManagerCompat
import com.example.android_nas_sync.MainActivity
import com.example.android_nas_sync.R
import com.example.android_nas_sync.db.MappingDatabase
import com.example.android_nas_sync.repository.MappingsRepository
import com.example.android_nas_sync.utils.TimeUtils
import kotlinx.coroutines.runBlocking
import java.util.*


class SyncService(): Service()  {
    private val NOTIFICATION_CHANNEL_ID = "129"
    private val NOTIFICATION_FOREGROUND_ID = 129
    private var notificationId = 1
    private lateinit var db:MappingDatabase
    private lateinit var repository:MappingsRepository

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            db = MappingDatabase.getInstance(applicationContext)
            repository = MappingsRepository(db, applicationContext)
            startSyncing()
        }
    }

    private fun startSyncing(){
        val timer = Timer()
        val task = object: TimerTask() {
            override fun run() {
                runBlocking {
                    val mappings = repository.getMappings()
                    mappings.forEach{mapping ->
                        repository.update(mapping.apply{
                            this.currentlySyncing = true
                        })

                        val result = repository.syncMapping(mapping)

                        if(result.errorMessage == null){
                            var resultMessage = ""

                            if(result.filedAdded > 0 && result.filesFailedToAdd == 0){
                                resultMessage = "${result.filedAdded} files added"
                            }
                            if(result.filedAdded > 0 && result.filesFailedToAdd > 0){
                                resultMessage = "${result.filedAdded} files added, ${result.filesFailedToAdd} failed to add"
                            }
                            if(result.filedAdded == 0 && result.filesFailedToAdd == 0 && result.errorMessage == null){
                                resultMessage = "No new files found"
                            }
                            showNotification("Syncing Complete!",resultMessage)
                            updateForegroundNotification("Last Synced: ${TimeUtils.unixTimestampToHoursAndMins(TimeUtils.unixTimestampNowSecs())}")
                        }
                        else{
                            showNotification("Syncing Error",result.errorMessage)
                        }

                        repository.update(mapping.apply{
                            this.currentlySyncing = false
                        })
                    }
                }
            }
        }
        val period:Long = 1000 * 60 * 60 * 24 // 24 hours
        timer.scheduleAtFixedRate(task,period,period)
    }

    private fun showNotification(title:String, message:String){
        val builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.arrow_right)
        builder.build()

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
        notificationId++
    }

    private fun updateForegroundNotification(message:String) {
        val builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText(message)
            .setSmallIcon(R.drawable.arrow_right)
        builder.build()

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_FOREGROUND_ID, builder.build())
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Nas Sync Running")
            .setContentText("Last Synced: Never")
            .setSmallIcon(R.drawable.arrow_right)
            .setContentIntent(pendingIntent)
            .setTicker("Ticker")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        startForeground(NOTIFICATION_FOREGROUND_ID, builder.build())

        return START_STICKY
    }

    private fun createNotificationChannel(){
        val channel1 = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Syncing Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel1.description = "Sends notifications about syncing results"
        channel1.enableLights(true)
        channel1.lightColor = Color.BLACK

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel1)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}