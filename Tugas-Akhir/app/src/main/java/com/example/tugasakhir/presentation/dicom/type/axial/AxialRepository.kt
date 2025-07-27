package com.example.tugasakhir.presentation.dicom.type.axial

import android.graphics.Bitmap
import android.graphics.Color
import com.example.tugasakhir.ResultState
import java.io.File
import java.io.IOException
import kotlin.math.floor

class AxialRepository {
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

    fun processBitmap(z: Int, binaryData: ByteArray): ResultState<Bitmap> {
        return try {
            val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
            for (i in SIZE * SIZE * z until SIZE * SIZE * (z + 1)) {
                val y = floor(((i - SIZE * SIZE * z) / SIZE).toDouble()).toInt()
                val x = ((i - SIZE * z) % SIZE)
                //println("indeks ke $i = ${x}:${y}")
                if (binaryData.size > i) {
                    val color = binaryData[i].toInt() and 0xff
                    bitmap.setPixel(x, y, Color.rgb(color, color, color))
                }
            }
            ResultState.Success(bitmap)
        } catch (e: Exception) {
            ResultState.Error("Gagal memproses gambar: ${e.message}")
        }
    }
}