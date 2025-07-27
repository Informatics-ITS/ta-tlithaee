package com.example.tugasakhir.presentation.dicom.type.coronal

import android.graphics.Bitmap
import android.graphics.Color
import com.example.tugasakhir.ResultState
import java.io.File
import java.io.IOException

class CoronalRepository {
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

    fun processBitmap(y: Int, binaryData: ByteArray): ResultState<Bitmap> {
        return try {
            val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
            for (z in 0 until SIZE) {
                for (x in 0 until SIZE){
                    try {
                        val indeks = y * SIZE + z * SIZE * SIZE + x

                        if (binaryData.size > indeks) {
                            val color = binaryData[indeks].toInt() and 0xff
                            bitmap.setPixel(x, z, Color.rgb(color, color, color))
                        }
                    } catch (e: Exception){
                        println("Caught an Exception: ${e.message}")
                    }

                }
            }
            ResultState.Success(bitmap)
        } catch (e: Exception) {
            ResultState.Error("Gagal memproses gambar: ${e.message}")
        }
    }
}