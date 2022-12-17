package com.example.android_nas_sync.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.android_nas_sync.MainActivity
import com.example.android_nas_sync.R
import com.example.android_nas_sync.viewmodels.MappingsViewModel

class SyncService(): Service()  {
    private var serviceLooper: Looper? = null
    private val context: Context? = null
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123

    override fun onCreate() {
//        HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND).apply {
            startSyncing()
            Log.w("1", "service started")
//        }

    }

    private fun startSyncing(){

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        generateForegroundNotification()
        return START_STICKY
    }


    private fun generateForegroundNotification() {
        val intentMainLanding = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intentMainLanding, 0)
        iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        if (mNotificationManager == null) {
            mNotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        assert(mNotificationManager != null)
        mNotificationManager?.createNotificationChannelGroup(
            NotificationChannelGroup("chats_group", "Chats")
        )
        val notificationChannel =
            NotificationChannel(
                "service_channel", "Service Notifications",
                NotificationManager.IMPORTANCE_MIN
            )
        notificationChannel.enableLights(false)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        mNotificationManager?.createNotificationChannel(notificationChannel)
        val builder = NotificationCompat.Builder(this, "service_channel")

        builder.setContentTitle(
            StringBuilder(resources.getString(R.string.app_name)).append(" service is running")
                .toString()
        )
            .setTicker(
                StringBuilder(resources.getString(R.string.app_name)).append("service is running")
                    .toString()
            )
            .setContentText("Touch to open") //                    , swipe down for more options.
            .setSmallIcon(R.drawable.arrow_right)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setWhen(0)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
        }
        builder.color = resources.getColor(R.color.purple_200)
        notification = builder.build()
        startForeground(mNotificationId, notification)
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}