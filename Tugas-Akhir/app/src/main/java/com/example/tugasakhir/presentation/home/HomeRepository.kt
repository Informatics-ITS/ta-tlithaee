package com.example.tugasakhir.presentation.home

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.tugasakhir.ResultState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class HomeRepository(val context: Context) {

    fun saveFile(uri: Uri, fileName: String): ResultState<String> {
        return try {
            val folder = File(context.filesDir, "dicom")
            if (!folder.exists()) folder.mkdirs()

            val file = File(folder, fileName)
            val inputStream = context.contentResolver.openInputStream(uri)

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            ResultState.Success(file.absolutePath)
        } catch (e: IOException) {
            ResultState.Error("Gagal simpan file: ${e.message}")
        }
    }


    fun getFileName(uri: Uri): String {
        var result = "default.bin"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
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


