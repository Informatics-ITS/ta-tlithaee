package com.example.tugasakhir.presentation.dicom

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhir.ResultState
import com.example.tugasakhir.model.DicomData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DicomViewModel(
    private val firebaseRepository: DicomFirebaseRepository
) : ViewModel() {

    private val _dicomFiles = MutableLiveData<List<DicomData>>()
    val dicomFiles: LiveData<List<DicomData>> get() = _dicomFiles
    val uploadState = MutableLiveData<ResultState<DicomData>>()
    val deleteState = MutableLiveData<ResultState<Unit>>()

    fun uploadToCloud(uri: Uri, item: DicomData, onSuccess: (DicomData) -> Unit) {
        viewModelScope.launch {
            try {
                uploadState.value = ResultState.Loading()

                val url = firebaseRepository.uploadDicomFile(uri, item.filename)
                val fileWithUrl = item.copy(fileUrl = url)
                firebaseRepository.saveDicomToCloud(fileWithUrl)

                uploadState.value = ResultState.Success(fileWithUrl)
                onSuccess(fileWithUrl)
            } catch (e: Exception) {
                uploadState.value = ResultState.Error("Gagal upload: ${e.message}")
                Log.e("DicomViewModel", "Upload gagal: ${e.message}")
            }
        }
    }

    fun loadCloudFiles() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val dicomFiles = firebaseRepository.getDicomFilesFromCloud(userId)
                    Log.d("DicomViewModel", "DICOM files fetched: ${dicomFiles.size}")
                    _dicomFiles.value = dicomFiles
                } else {
                    Log.e("DicomViewModel", "User tidak terautentikasi")
                }
            } catch (e: Exception) {
                Log.e("DicomViewModel", "Gagal ambil dari cloud: ${e.message}")
            }
        }
    }

    fun deleteFromCloud(file: DicomData) {
        viewModelScope.launch {
            deleteState.value = ResultState.Loading()

            try {
                firebaseRepository.deleteDicomFile(file)
                firebaseRepository.deleteAnnotationsFolder(file.id)
                firebaseRepository.deleteAnnotationsFromFirestore(file.id)
                firebaseRepository.deleteAnnotationDocuments(file.id)
                firebaseRepository.deleteFromFirestore(file.id)
                loadCloudFiles()
                deleteState.value = ResultState.Success(Unit)
            } catch (e: Exception) {
                Log.e("DicomViewModel", "Gagal hapus file: ${e.message}")
                deleteState.value = ResultState.Error("Gagal hapus: ${e.message}")
            }
        }
    }

}