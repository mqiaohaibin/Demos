package cn.paper.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import cn.paper.demo.activities.BarChartActivity
import cn.paper.demo.activities.CircleImageActivity
import cn.paper.demo.widgets.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnBarChart: Button
    private lateinit var btnCircleImage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnBarChart = findViewById(R.id.btnBarChart)
        btnBarChart.setOnClickListener(this)

        btnCircleImage = findViewById(R.id.btnCircleImage)
        btnCircleImage.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btnBarChart -> {
                val intent = Intent(this, BarChartActivity::class.java)
                startActivity(intent)
            }

            R.id.btnCircleImage -> {
                val intent = Intent(this, CircleImageActivity::class.java)
                startActivity(intent)
            }
        }

    }

}
