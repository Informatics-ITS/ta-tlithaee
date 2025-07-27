package com.example.tugasakhir.presentation.auth.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tugasakhir.ResultState
import com.example.tugasakhir.ViewModelFactory
import com.example.tugasakhir.databinding.FragmentRegisterBinding
import com.example.tugasakhir.presentation.auth.AuthActivity
import com.example.tugasakhir.presentation.auth.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authViewModel: AuthViewModel
    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = Firebase.auth

        val factory = ViewModelFactory.getDefault(requireContext())
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        setupBinding()
        updateUI()

        return binding.root
    }

    private fun setupBinding(){
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPass.text.toString().trim()
            authViewModel.register(email, password)
        }
    }

    private fun updateUI(){
        authViewModel.authState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    binding.pbRegister.visibility = View.VISIBLE
                }
                is ResultState.Success -> {
                    binding.pbRegister.visibility = View.GONE
                    Toast.makeText(requireContext(), "Berhasil menambahkan, Selamat Datang", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    startActivity(intent)
                }
                is ResultState.Error -> {
                    binding.pbRegister.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message ?: "Gagal Register", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}