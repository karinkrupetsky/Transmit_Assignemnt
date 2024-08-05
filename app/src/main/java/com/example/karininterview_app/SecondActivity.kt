package com.example.karininterview_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.karininterview_app.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        binding.apply {
            buttonBack.setOnClickListener {
                finish()
            }
            buttonHello.setOnClickListener {
                Toast.makeText(this@SecondActivity, "Hello World", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
