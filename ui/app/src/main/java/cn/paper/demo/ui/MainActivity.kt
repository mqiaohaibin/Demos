package cn.paper.demo.ui

import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import cn.paper.demo.ui.notification.NotificationActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnNotification: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnNotification = findViewById(R.id.btnNotification)
        btnNotification.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }
}
