package com.example.tugasakhir.presentation.edit

import android.R
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tugasakhir.databinding.ActivityEditBinding
import com.example.tugasakhir.presentation.dicom.type.axial.AxialFragment
import com.example.tugasakhir.presentation.dicom.type.coronal.CoronalFragment
import com.example.tugasakhir.presentation.dicom.type.sagital.SagitalFragment

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private var filePath: String? = null
    private var dicomId: String? = null
    private var orientation: String = "Axial"
    private var sliceIndex: Int = 0
//    private lateinit var trace: Trace

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        trace = FirebasePerformance.getInstance().newTrace("edit_activity_startup")
//        trace.start()

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = intent.getStringExtra("FILE_URL")
        dicomId = intent.getStringExtra("DICOM_ID") ?: ""
        orientation = intent.getStringExtra("ORIENTATION") ?: "Axial"
        sliceIndex = intent.getIntExtra("SLICE_INDEX", 0)

        setupEdit()
        setupSpinner()
        setupInitialFragment()
        setupBinding()

//        trace.stop()
    }

    private fun setupBinding() {
        binding.ibCheck.setOnClickListener {
            binding.pbEdit.visibility = View.VISIBLE
            val currentFragment = supportFragmentManager.findFragmentById(binding.flTipe.id)

            when (currentFragment) {
                is AxialFragment -> {
                    currentFragment.savePen { success ->
                        binding.pbEdit.visibility = View.GONE
                        Toast.makeText(
                            this,
                            if (success) "Anotasi berhasil di-upload" else "Gagal upload anotasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is CoronalFragment -> {
                    currentFragment.savePen { success ->
                        binding.pbEdit.visibility = View.GONE
                        Toast.makeText(
                            this,
                            if (success) "Anotasi berhasil di-upload" else "Gagal upload anotasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is SagitalFragment -> {
                    currentFragment.savePen { success ->
                        binding.pbEdit.visibility = View.GONE
                        Toast.makeText(
                            this,
                            if (success) "Anotasi berhasil di-upload" else "Gagal upload anotasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                else -> {
                    binding.pbEdit.visibility = View.GONE
                    Toast.makeText(this, "Fragment tidak dikenali", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun setupEdit() {
        val editFragment = EditFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.flEdit.id, editFragment)
            .commit()
    }

    private fun setupSpinner() {
        val items = arrayOf("Axial", "Sagital", "Coronal")
        val adapterSpinner = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, items)
        binding.acTipe.setAdapter(adapterSpinner)

        val position = items.indexOf(orientation)
        if (position >= 0) {
            binding.acTipe.setText(items[position], false)
        }

        binding.acTipe.setOnItemClickListener { _, _, pos, _ ->
            val selectedOrientation = items[pos]
            val fragmentType = when (selectedOrientation) {
                "Axial" -> AxialFragment().apply {
                    arguments = Bundle().apply {
                        putString("FILE_URL", filePath)
                        putString("DICOM_ID", dicomId)
                        putInt("SLICE_INDEX", sliceIndex)
                    }
                }
                "Sagital" -> SagitalFragment().apply {
                    arguments = Bundle().apply {
                        putString("FILE_URL", filePath)
                        putString("DICOM_ID", dicomId)
                        putInt("SLICE_INDEX", sliceIndex)
                    }
                }
                "Coronal" -> CoronalFragment().apply {
                    arguments = Bundle().apply {
                        putString("FILE_URL", filePath)
                        putString("DICOM_ID", dicomId)
                        putInt("SLICE_INDEX", sliceIndex)
                    }
                }
                else -> throw IllegalStateException("Unknown slice type")
            }

            supportFragmentManager.beginTransaction()
                .replace(binding.flTipe.id, fragmentType)
                .commit()
        }
    }

    private fun setupInitialFragment() {
        val fragment = when (orientation) {
            "Axial" -> AxialFragment().apply {
                arguments = Bundle().apply {
                    putString("FILE_URL", filePath)
                    putString("DICOM_ID", dicomId)
                    putInt("SLICE_INDEX", sliceIndex)
                }
            }
            "Sagital" -> SagitalFragment().apply {
                arguments = Bundle().apply {
                    putString("FILE_URL", filePath)
                    putString("DICOM_ID", dicomId)
                    putInt("SLICE_INDEX", sliceIndex)
                }
            }
            "Coronal" -> CoronalFragment().apply {
                arguments = Bundle().apply {
                    putString("FILE_URL", filePath)
                    putString("DICOM_ID", dicomId)
                    putInt("SLICE_INDEX", sliceIndex)
                }
            }
            else -> throw IllegalStateException("Unknown orientation")
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.flTipe.id, fragment)
            .commit()
    }

    fun setEditMode(mode: EditMode) {
        val currentFragment = supportFragmentManager.findFragmentById(binding.flTipe.id)
        if (currentFragment is EditModeToggle) {
            currentFragment.setEditMode(mode)
        }
    }
}