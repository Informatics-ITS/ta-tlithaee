package com.example.tugasakhir.presentation.dicom

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.tugasakhir.R
import com.example.tugasakhir.databinding.ActivityDicomBinding
import com.example.tugasakhir.presentation.dicom.type.axial.AxialFragment
import com.example.tugasakhir.presentation.dicom.type.coronal.CoronalFragment
import com.example.tugasakhir.presentation.dicom.type.sagital.SagitalFragment
import com.example.tugasakhir.presentation.edit.EditActivity
import com.example.tugasakhir.presentation.home.HomeFragment

class DicomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDicomBinding
    private var filePath: String? = null
    private var dicomId: String? = null
    private var userId: String? = null
    private var currentOrientation: String = "Axial"
    private var currentSliceIndex: Int = 0
//    private lateinit var trace: Trace

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
//        trace = FirebasePerformance.getInstance().newTrace("dicom_activity_startup")
//        trace.start()

        binding = ActivityDicomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = intent.getStringExtra("FILE_URL")
        dicomId = intent.getStringExtra("DICOM_ID")
        userId = intent.getStringExtra("USER_ID")
//        Log.d("DicomActivity", "📦 FILE_URL: $filePath")
//        Log.d("DicomActivity", "📦 DICOM_ID: $dicomId")

        setupBinding()
        setupSpinner()
//        trace.stop()
    }

    private fun setupBinding() {
        binding.fbDicom.setOnClickListener {
//            Log.d("DicomActivity", "➡️ EditActivity | FILE_URL: $filePath | DICOM_ID: $dicomId")
            val intent = Intent(this, EditActivity::class.java).apply {
                putExtra("FILE_URL", filePath)
                putExtra("DICOM_ID", dicomId)
                putExtra("USER_ID", userId)
                putExtra("ORIENTATION", currentOrientation)
                putExtra("SLICE_INDEX", currentSliceIndex)
            }
            startActivity(intent)
        }
    }

    private fun setupSpinner() {
        val items = arrayOf("Axial", "Sagital", "Coronal")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        binding.acTipe.setAdapter(adapterSpinner)

        binding.acTipe.setOnItemClickListener { _, _, position, _ ->
            currentOrientation = when (position) {
                0 -> "Axial"
                1 -> "Sagital"
                2 -> "Coronal"
                else -> "Axial"
            }

            val fragmentType = when (currentOrientation) {
                "Axial" -> AxialFragment().apply {
                    arguments = Bundle().apply {
                        putString("FILE_URL", filePath)
                        putString("DICOM_ID", dicomId)
                        putInt("SLICE_INDEX", currentSliceIndex)
                    }
                }
                "Sagital" -> SagitalFragment().apply {
                    arguments = Bundle().apply {
                        putString("FILE_URL", filePath)
                        putString("DICOM_ID", dicomId)
                        putInt("SLICE_INDEX", currentSliceIndex)
                    }
                }
                "Coronal" -> CoronalFragment().apply {
                    arguments = Bundle().apply {
                        putString("FILE_URL", filePath)
                        putString("DICOM_ID", dicomId)
                        putInt("SLICE_INDEX", currentSliceIndex)
                    }
                }
                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.flDicom, fragmentType)
                .commit()
        }
    }
}