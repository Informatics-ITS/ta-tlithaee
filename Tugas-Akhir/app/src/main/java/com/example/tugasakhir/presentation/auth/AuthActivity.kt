package com.example.tugasakhir.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tugasakhir.MainActivity
import com.example.tugasakhir.adapter.AuthPagerAdapter
import com.example.tugasakhir.databinding.ActivityAuthBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
//    private lateinit var trace: Trace
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        trace = FirebasePerformance.getInstance().newTrace("auth_activity_startup")
//        trace.start()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = AuthPagerAdapter(this)
        binding.vpAuth.adapter = adapter

        setupViewPager()
//        trace.stop()
    }

    private fun setupViewPager(){
        TabLayoutMediator(binding.tlAuth, binding.vpAuth) { tab, position ->
            tab.text = when(position) {
                0 -> "Login"
                1 -> "Register"
                else -> throw IllegalStateException("Invalid")
            }
        }.attach()
    }
}