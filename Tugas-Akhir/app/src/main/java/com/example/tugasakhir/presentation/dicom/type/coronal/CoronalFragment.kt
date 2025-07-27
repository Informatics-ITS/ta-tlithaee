package com.example.tugasakhir.presentation.dicom.type.coronal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tugasakhir.ResultState
import com.example.tugasakhir.ViewModelFactory
import com.example.tugasakhir.databinding.FragmentCoronalBinding
import com.example.tugasakhir.presentation.edit.EditMode
import com.example.tugasakhir.presentation.edit.EditModeToggle
import com.example.tugasakhir.presentation.edit.OrientationType

class CoronalFragment : Fragment(), EditModeToggle {

    private var currentSliceIndex = 0
    private var _binding : FragmentCoronalBinding? = null
    private val binding get() = _binding!!
    private lateinit var coronalViewModel: CoronalViewModel
    private var isInitialDisplayDone = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCoronalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentSliceIndex = arguments?.getInt("SLICE_INDEX", 0) ?: 0
        val factory = ViewModelFactory.withOrientation(requireContext(), OrientationType.CORONAL)
        coronalViewModel = ViewModelProvider(this, factory)[CoronalViewModel::class.java]

        val dicomId = arguments?.getString("DICOM_ID")
        arguments?.getString("FILE_URL")?.let { fileUrl ->
            coronalViewModel.loadFile(
                fileUrl = fileUrl,
                context = requireContext(),
                isFromFirebase = true,
                dicomId = dicomId,
                onAnnotationLoaded = { index ->
                    if (index == currentSliceIndex) {
                        updateCanvas()
                    }
                }
            )
        }

        setupSeekBar()
        updateUI()
    }

    private fun setupSeekBar() {
        binding.sbCoronal.max = 255
        binding.sbCoronal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSliceNumberCoronal.text = "Slice: $progress"
                if (fromUser) {
                    currentSliceIndex = progress
                    coronalViewModel.fileState.value?.data?.let { binaryData ->
                        coronalViewModel.updateBitmapForY(progress, binaryData)
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        var isBitmapReady = false
        var areAnnotationsReady = false

        coronalViewModel.bitmap.observe(viewLifecycleOwner) {
            isBitmapReady = it is ResultState.Success
            if (isBitmapReady && areAnnotationsReady) {
                updateCanvas()
            }
        }

        coronalViewModel.annotationsLoaded.observe(viewLifecycleOwner) {
            areAnnotationsReady = it
            if (areAnnotationsReady && isBitmapReady) {
                updateCanvas()
            }
        }

        coronalViewModel.fileState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    binding.pbCoronal.visibility = View.VISIBLE
                }

                is ResultState.Success -> {
                    val binary = result.data
                    val dicomId = arguments?.getString("DICOM_ID")

                    if (!isInitialDisplayDone) {
                        isInitialDisplayDone = true
                        coronalViewModel.updateBitmapForY(currentSliceIndex, binary ?: return@observe, dicomId)
                        binding.sbCoronal.progress = currentSliceIndex
                        binding.tvSliceNumberCoronal.text = "Slice: $currentSliceIndex"
                    }

                    binding.pbCoronal.visibility = View.GONE
                }

                is ResultState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message ?: "❌ Failed to load file",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.pbCoronal.visibility = View.GONE
                }
            }
        }
    }

    private fun updateCanvas() {
        val oriBitmap = coronalViewModel.bitmap.value?.data ?: return
        val annotation = coronalViewModel.getAnnotationBitmap(currentSliceIndex)

        binding.pvCoronal.setBaseBitmap(oriBitmap)
        binding.pvCoronal.setEmptyCanvas(annotation)
    }

    fun savePen(onUploadFinished: (Boolean) -> Unit) {
        binding.pvCoronal.commitDrawingToBitmapDirect()
        val sliceIndex = currentSliceIndex

        val dicomId = arguments?.getString("DICOM_ID")
            ?: requireActivity().intent.getStringExtra("DICOM_ID")

        if (dicomId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "DICOM ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            onUploadFinished(false)
            return
        }

//        val annotationBitmap = coronalViewModel.getAnnotationBitmap(sliceIndex)
//        if (!isBitmapLikelyNotEmpty(annotationBitmap)) {
//            Toast.makeText(requireContext(), "Anotasi masih kosong, tidak disimpan", Toast.LENGTH_SHORT).show()
//            onUploadFinished(false)
//            return
//        }

        coronalViewModel.saveAnnotationForSliceAndUpload(requireContext(), dicomId, sliceIndex) { success ->
            val msg = if (success) "Anotasi slice $sliceIndex di-upload" else "Gagal upload anotasi"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            onUploadFinished(success)
        }
    }

//    fun isBitmapLikelyNotEmpty(bitmap: Bitmap): Boolean {
//        for (y in 0 until bitmap.height step 8) {
//            for (x in 0 until bitmap.width step 8) {
//                val pixel = bitmap.getPixel(x,y)
//                if (pixel != Color.TRANSPARENT) {
//                    return true
//                }
//            }
//        }
//        return false
//    }

    override fun setEditMode(enabled: EditMode) {
        binding.pvCoronal.setEditMode(enabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}