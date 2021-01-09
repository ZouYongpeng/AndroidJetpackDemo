package com.example.androidjetpackdemo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.androidjetpackdemo.workmanager.WorkManagerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        work_manager_btn.openDemo<WorkManagerActivity>()
    }


    private inline fun <reified T : Activity> Button.openDemo() {
        setOnClickListener {
            startActivity(Intent(this@MainActivity, T::class.java))
        }
    }

}