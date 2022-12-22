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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*


class SyncService(): Service()  {
    private val NOTIFICATION_FOREGROUND_CHANNEL_ID = "129"
    private val NOTIFICATION_UPDATES_CHANNEL_ID = "130"
    private val NOTIFICATION_FOREGROUND_ID = 129
    private val NOTIFICATION_UPDATE_ID = 130
    private lateinit var db:MappingDatabase
    private lateinit var repository:MappingsRepository
    private var isSyncing = false

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            db = MappingDatabase.getInstance(applicationContext)
            repository = MappingsRepository(db, applicationContext)
            startSyncing()
            observeCurrentlySyncingStatus()
        }
    }

    private fun startSyncing(){
        val timer = Timer()
        val task = object: TimerTask() {
            override fun run() {
                runBlocking {
                    if(isSyncing){
                        return@runBlocking
                    }
                    isSyncing = true
                    updateForegroundNotification("Syncing now")
                    var attemptsLeft = 10
                    while(attemptsLeft > 0) {
                        val success = syncMappings()
                        if(success){
                            isSyncing = false
                            return@runBlocking
                        }
                        delay(1000 * 60 * 5)
                        attemptsLeft--
                    }
                    isSyncing = false
                    updateForegroundNotification("Failed to sync recently")
                }
            }
        }
        val period:Long = 1000 * 60 * 60 * 24 // 24 hours
        timer.scheduleAtFixedRate(task,0,period)
    }

    private suspend fun syncMappings():Boolean{
        val mappings = repository.getMappings()
        val results = mappings.map{mapping ->
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
                showStatusUpdateNotification("Syncing Complete!",resultMessage)
                updateForegroundNotification("Last Synced: ${TimeUtils.unixTimestampToFormattedDate(TimeUtils.unixTimestampNowSecs())}")
            }
            else{
                showStatusUpdateNotification("Syncing Error",result.errorMessage)
            }

            repository.update(mapping.apply{
                this.currentlySyncing = false
            })
            return@map result
        }
         val anyErrors = results.any { result -> result.errorMessage != null }
         return !anyErrors
    }



    private fun observeCurrentlySyncingStatus(){
        repository.currentlySyncingInfo.observeForever { info ->
            if(!(info.currentItem < info.totalItems)){
                return@observeForever
            }
            val builder = Notification.Builder(this, NOTIFICATION_UPDATES_CHANNEL_ID)
                .setContentTitle("${info.currentItem} out of ${info.totalItems} files added")
                .setProgress(info.totalItems,info.currentItem, false)
                .setSmallIcon(R.drawable.arrow_right)
            builder.build()

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_UPDATE_ID, builder.build())
            }
        }
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val builder = Notification.Builder(this, NOTIFICATION_FOREGROUND_CHANNEL_ID)
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
        val channelForeground = NotificationChannel(
            NOTIFICATION_FOREGROUND_CHANNEL_ID,
            "Foreground Notifications Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channelForeground.description = "Notification for foreground service"

        val channelUpdates = NotificationChannel(
            NOTIFICATION_UPDATES_CHANNEL_ID,
            "Syncing Notifications Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        channelUpdates.description = "Notification for syncing updates"

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channelUpdates)
    }

    private fun updateForegroundNotification(message:String) {
        val builder = Notification.Builder(this, NOTIFICATION_FOREGROUND_CHANNEL_ID)
            .setContentText(message)
            .setSmallIcon(R.drawable.arrow_right)
        builder.build()

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_FOREGROUND_ID, builder.build())
        }
    }

    private fun showStatusUpdateNotification(title:String, message:String){
        val builder = Notification.Builder(this, NOTIFICATION_UPDATES_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.arrow_right)
        builder.build()

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_UPDATE_ID, builder.build())
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // TODO be nice and cleanup yo mess
        super.onDestroy()
    }
}