package cn.paper.demo.scroll

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnSystemScroll: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSystemScroll = findViewById(R.id.btnSystemScroll)
        btnSystemScroll.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btnSystemScroll -> {
                val intent = Intent(this, SystemScrollActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
