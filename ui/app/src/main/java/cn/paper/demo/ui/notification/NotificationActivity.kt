package cn.paper.demo.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cn.paper.demo.ui.R

class NotificationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnBasic: Button
    private lateinit var btnCustomContentArea: Button
    private lateinit var btnFullCustom: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        btnBasic = findViewById(R.id.btnBasic)
        btnBasic.setOnClickListener(this)

        btnCustomContentArea = findViewById(R.id.btnCustomContentArea)
        btnCustomContentArea.setOnClickListener(this)

        btnFullCustom = findViewById(R.id.btnFullCustom)
        btnFullCustom.setOnClickListener(this)

        createNotificationChannel()
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btnBasic -> {
                sendBasicNotification()
            }
            R.id.btnCustomContentArea -> {
                sendCustomContentAreaNotification()
            }
            R.id.btnFullCustom -> {
                sendFullCustomNotification()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelList = listOf(NotificationChannel(CHANNEL_1_ID, CHANNEL_1_NAME, CHANNEL_1_IMPORTANCE),
                NotificationChannel(CHANNEL_2_ID, CHANNEL_2_NAME, CHANNEL_2_IMPORTANCE)
            )
            val desList = listOf(CHANNEL_1_DES, CHANNEL_2_DES)
            val noficationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channelList.forEachIndexed { index, channel ->
                channel.description = desList[index]
                noficationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun sendBasicNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_1_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("title")
        builder.setContentText("text")
        builder.priority = NotificationCompat.PRIORITY_HIGH
        NotificationManagerCompat.from(this).notify(BASIC_ID, builder.build())
    }

    private fun sendCustomContentAreaNotification() {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_content_area)
        val builder = NotificationCompat.Builder(this, CHANNEL_1_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
        NotificationManagerCompat.from(this).notify(CUSTOM_CONTENT_AREA_ID, builder.build())
    }

    private fun sendFullCustomNotification() {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_content_area)
        val builder = NotificationCompat.Builder(this, CHANNEL_1_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setCustomHeadsUpContentView(notificationLayout)
        NotificationManagerCompat.from(this).notify(FULL_CUSTOM_ID, builder.build())
    }

    companion object {
        const val CHANNEL_1_NAME = "channel_1_name"
        const val CHANNEL_1_DES = "channel_1_description"
        const val CHANNEL_1_ID = "channel_1_id"
        const val CHANNEL_1_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH

        const val CHANNEL_2_NAME = "channel_2_name"
        const val CHANNEL_2_DES = "channel_2_description"
        const val CHANNEL_2_ID = "channel_2_id"
        const val CHANNEL_2_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT

        const val BASIC_ID = 1
        const val CUSTOM_CONTENT_AREA_ID = 2
        const val FULL_CUSTOM_ID = 3
    }
}
