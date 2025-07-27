package com.example.tugasakhir.presentation.home

import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tugasakhir.ResultState

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _fileState = MutableLiveData<ResultState<Uri>>()
    val fileState: LiveData<ResultState<Uri>> get() = _fileState

    fun processSelectedFile(uri: Uri) {
        val fileName = getFileNameFromUri(uri)
        if (fileName.endsWith(".bin", ignoreCase = true)) {
            _fileState.value = ResultState.Success(uri)
        } else {
            _fileState.value = ResultState.Error("Hanya boleh .bin file!")
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result = "default.bin"
        repository.context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        }
        return result
    }
}



