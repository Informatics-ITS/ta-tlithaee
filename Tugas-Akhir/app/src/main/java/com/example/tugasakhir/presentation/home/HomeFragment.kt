package com.example.tugasakhir.presentation.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tugasakhir.R
import com.example.tugasakhir.ResultState
import com.example.tugasakhir.ViewModelFactory
import com.example.tugasakhir.adapter.HomeAdapter
import com.example.tugasakhir.databinding.FragmentHomeBinding
import com.example.tugasakhir.model.DicomData
import com.example.tugasakhir.presentation.dicom.DicomActivity
import com.example.tugasakhir.presentation.dicom.DicomViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var adapter: HomeAdapter
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var homeViewModel: HomeViewModel
    private val dicomViewModel: DicomViewModel by viewModels {
        ViewModelFactory.getDefault(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = ViewModelFactory.getDefault(requireContext())
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupBinding()
        setupRecyclerView()
        updateUI()

        return binding.root
    }

    private fun setupBinding() {
        binding.ibHome.setOnClickListener {
            selectBinFile()
        }

        binding.btnUpload.setOnClickListener {
            uploadDicomFile()
        }
    }

    private fun setupRecyclerView() {
        adapter = HomeAdapter()
        adapter.onDeleteClicked = { file ->
            dicomViewModel.deleteFromCloud(file)
        }
        binding.rvDicom.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDicom.adapter = adapter

        dicomViewModel.dicomFiles.observe(viewLifecycleOwner) { fileList ->
//            Log.d("HomeFragment", "Diterima ${fileList.size} file dari Firestore")
            adapter.submitList(fileList)
        }

        dicomViewModel.loadCloudFiles()
    }

    private fun updateUI() {
        homeViewModel.fileState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    binding.pbHome.visibility = View.VISIBLE
                }
                is ResultState.Success -> {
                    binding.pbHome.visibility = View.GONE
                }
                is ResultState.Error -> {
                    binding.pbHome.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message ?: "Error selecting file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dicomViewModel.uploadState.observe(viewLifecycleOwner) { uploadResult ->
            when (uploadResult) {
                is ResultState.Loading -> {
                    binding.pbHome.visibility = View.VISIBLE
                }
                is ResultState.Success -> {
                    binding.pbHome.visibility = View.GONE
                    dicomViewModel.loadCloudFiles()
                    Toast.makeText(requireContext(), "Upload berhasil", Toast.LENGTH_SHORT).show()
                }
                is ResultState.Error -> {
                    binding.pbHome.visibility = View.GONE
                    Toast.makeText(requireContext(), uploadResult.message ?: "Upload gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dicomViewModel.deleteState.observe(viewLifecycleOwner) { deleteResult ->
            when (deleteResult) {
                is ResultState.Loading -> {
                    binding.pbHome.visibility = View.VISIBLE
                }
                is ResultState.Success -> {
                    binding.pbHome.visibility = View.GONE
                    Toast.makeText(requireContext(), "File berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                is ResultState.Error -> {
                    binding.pbHome.visibility = View.GONE
                    Toast.makeText(requireContext(), deleteResult.message ?: "Gagal hapus file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun selectBinFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Pilih .bin file"), FILE_PICKER_REQUEST_CODE)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown.bin"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                homeViewModel.processSelectedFile(uri)
            }
        }
    }

    private fun uploadDicomFile() {
        val result = homeViewModel.fileState.value
        if (result !is ResultState.Success) return

        val uri = result.data ?: return
        val filename = getFileNameFromUri(requireContext(), uri)

        val dicomFile = DicomData(
            id = UUID.randomUUID().toString(),
            filename = filename,
            fileUrl = "",
            width = 0,
            height = 0,
//            annotations = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        dicomViewModel.uploadToCloud(uri, dicomFile) { uploaded ->
            if (uploaded.id.isNotEmpty() && uploaded.fileUrl.isNotEmpty()) {
                val intent = Intent(requireContext(), DicomActivity::class.java).apply {
                    putExtra("FILE_URL", uploaded.fileUrl)
                    putExtra("FILENAME", uploaded.filename)
                    putExtra("DICOM_ID", uploaded.id)
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Upload gagal, DICOM_ID kosong", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.action_nav_home_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }
}