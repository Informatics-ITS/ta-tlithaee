package com.example.tugasakhir.presentation.dicom.type.sagital

import android.graphics.Bitmap
import android.graphics.Color
import com.example.tugasakhir.ResultState
import java.io.File
import java.io.IOException

class SagitalRepository {
    private val SIZE = 256
    fun readBinFile(filePath: String): ResultState<ByteArray> {
        return try {
            val data = File(filePath).readBytes()
            ResultState.Success(data)
        } catch (e: IOException) {
            e.printStackTrace()
            ResultState.Error("Gagal membaca file: ${e.message}")
        }
    }

    fun processBitmap(x: Int, binaryData: ByteArray): ResultState<Bitmap> {
        return try {
            val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
            for(z in 0 until SIZE){
                for(y in 0 until SIZE){
                    val indeks = y * SIZE + z * SIZE * SIZE + x
                    if (binaryData.size > indeks) {
                        val color = binaryData[indeks].toInt() and 0xff
                        bitmap.setPixel(y, z, Color.rgb(color, color, color))
                    }
                }
            }
            ResultState.Success(bitmap)
        } catch (e: Exception) {
            ResultState.Error("Gagal memproses gambar: ${e.message}")
        }
    }
}