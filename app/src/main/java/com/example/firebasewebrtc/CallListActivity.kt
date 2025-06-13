package com.example.firebasewebrtc

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firebasewebrtc.databinding.ActivityCallListBinding

class CallListActivity : BaseActivity() {

    private lateinit var binding: ActivityCallListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

        }
    }
}