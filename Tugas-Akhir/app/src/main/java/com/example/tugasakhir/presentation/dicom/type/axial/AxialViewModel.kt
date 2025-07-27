package com.example.tugasakhir.presentation.dicom.type.axial

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhir.ResultState
import com.example.tugasakhir.model.AnnotationData
import com.example.tugasakhir.presentation.edit.OrientationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class AxialViewModel(
    private val repository: AxialRepository,
    private val orientationType: OrientationType
) : ViewModel() {
    private val SIZE = 256
    var imageWidth = 0
    var imageHeight = 0

    private val _fileState = MutableLiveData<ResultState<ByteArray>>()
    val fileState: LiveData<ResultState<ByteArray>> get() = _fileState

    private val _bitmap = MutableLiveData<ResultState<Bitmap>>()
    val bitmap: LiveData<ResultState<Bitmap>> get() = _bitmap

    private val _annotationsLoaded = MutableLiveData(false)
    val annotationsLoaded: LiveData<Boolean> get() = _annotationsLoaded

    private val annotationBitmaps = Array<Bitmap?>(SIZE) { null }
    private val annotationCanvases = Array<Canvas?>(SIZE) { null }

    fun getAnnotationBitmap(sliceIndex: Int): Bitmap {
        val existing = annotationBitmaps.getOrNull(sliceIndex)
        if (existing != null) return existing

        val width = imageWidth.takeIf { it > 0 } ?: 256
        val height = imageHeight.takeIf { it > 0 } ?: 256
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.TRANSPARENT)

        annotationBitmaps[sliceIndex] = bmp
        annotationCanvases[sliceIndex] = Canvas(bmp)

        return bmp
    }

//    fun getAnnotationCanvas(sliceIndex: Int): Canvas? {
//        return annotationCanvases.getOrNull(sliceIndex)
//    }

    fun saveAnnotationForSliceAndUpload(context: Context, dicomId: String, sliceIndex: Int, onComplete: (Boolean) -> Unit) {
        val bmp = annotationBitmaps[sliceIndex] ?: return onComplete(false)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempFile = File.createTempFile("slice_$sliceIndex", ".bin", context.cacheDir)
                FileOutputStream(tempFile).use { fos ->
                    for (y in 0 until bmp.height) {
                        for (x in 0 until bmp.width) {
                            val pixel = bmp.getPixel(x, y)
                            fos.write((pixel shr 24) and 0xFF) // A
                            fos.write((pixel shr 16) and 0xFF) // R
                            fos.write((pixel shr 8) and 0xFF)  // G
                            fos.write(pixel and 0xFF)          // B
                        }
                    }
                }

                val storageRef = FirebaseStorage.getInstance().reference
                val orientation = orientationType.name.lowercase()
                val fileRef = storageRef.child("annotations/$dicomId/$orientation/slice_$sliceIndex.bin")
                fileRef.putFile(tempFile.toUri()).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()

                val annotation = AnnotationData(
                    id = UUID.randomUUID().toString(),
                    dicomId = dicomId,
                    sliceIndex = sliceIndex,
                    view = orientationType.name,
                    url = downloadUrl,
                    width = imageWidth,
                    height = imageHeight,
                    createdAt = System.currentTimeMillis()
                )

                Firebase.firestore
                    .collection("dicomFiles")
                    .document(dicomId)
                    .collection("annotations")
                    .document(orientation)
                    .collection("slices")
                    .document("slice_$sliceIndex")
                    .set(annotation)
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.Main) {
                            onComplete(true)
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.Main) {
                            onComplete(false)
                        }
                    }

            } catch (e: Exception) {
                Log.e("AxialViewModel", "Gagal upload anotasi: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun loadFile(
        fileUrl: String,
        context: Context,
        isFromFirebase: Boolean = true,
        dicomId: String? = null,
        onAnnotationLoaded: ((Int) -> Unit)? = null
    ) {
        _fileState.value = ResultState.Loading()

        viewModelScope.launch {
            try {
                val file = if (isFromFirebase) {
                    downloadFileFromFirebaseUrl(context, fileUrl)
                } else {
                    File(fileUrl)
                }

                val result = repository.readBinFile(file.absolutePath)
                _fileState.value = result

                dicomId?.let {
                    loadAnnotationsFromFirestore(it, onAnnotationLoaded)
                }

            } catch (e: Exception) {
                _fileState.value = ResultState.Error("Gagal membaca file: ${e.message}")
            }
        }
    }

    fun loadAnnotationsFromFirestore(
        dicomId: String,
        onAnnotationLoaded: ((Int) -> Unit)? = null
    ) {
        val orientation = orientationType.name.lowercase()

        FirebaseFirestore.getInstance()
            .collection("dicomFiles")
            .document(dicomId)
            .collection("annotations")
            .document(orientation)
            .collection("slices")
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    val annotation = doc.toObject(AnnotationData::class.java) ?: continue
                    val index = annotation.sliceIndex
                    val url = annotation.url
                    val width = annotation.width
                    val height = annotation.height

                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val bmp = downloadAnnotationBitmap(url, width, height)
                            annotationBitmaps[index] = bmp
                            annotationCanvases[index] = Canvas(bmp)

                            withContext(Dispatchers.Main) {
                                onAnnotationLoaded?.invoke(index)
                            }
                        } catch (e: Exception) {
                            Log.e("AxialViewModel", "Gagal memuat anotasi slice $index: ${e.message}")
                        }
                    }
                }

                _annotationsLoaded.value = true
            }
            .addOnFailureListener {
                Log.e("AxialViewModel", "Gagal mengambil anotasi ($orientation): ${it.message}")
            }
    }


    private fun downloadAnnotationBitmap(url: String, width: Int, height: Int): Bitmap {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()

        val stream = BufferedInputStream(connection.inputStream)
        val bytes = stream.readBytes()
        stream.close()

//        val expectedSize = width * height * 4
//        if (bytes.size < expectedSize) {
//            Log.w("AxialViewModel", "Ukuran data anotasi kurang: hanya ${bytes.size} byte, seharusnya $expectedSize")
//        }

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        var i = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (i + 3 >= bytes.size) {
                    bmp.setPixel(x, y, Color.TRANSPARENT)
                } else {
                    val a = bytes[i].toInt() and 0xFF
                    val r = bytes[i + 1].toInt() and 0xFF
                    val g = bytes[i + 2].toInt() and 0xFF
                    val b = bytes[i + 3].toInt() and 0xFF
                    bmp.setPixel(x, y, Color.argb(a, r, g, b))
                }
                i += 4
            }
        }

        return bmp
    }

    suspend fun downloadFileFromFirebaseUrl(context: Context, url: String): File = withContext(Dispatchers.IO) {
        val decodedName = url.substringAfter("dicom%2F")
            .substringBefore("?")
            .replace("%2F", "/")
        val fileName = decodedName.ifBlank { "dicom_downloaded.bin" }

        val folder = File(context.cacheDir, "dicom")
        if (!folder.exists()) folder.mkdirs()

        val localFile = File(folder, fileName)
        if (localFile.exists()) return@withContext localFile

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }

            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(localFile)
            input.use { it.copyTo(output) }

            return@withContext localFile
        } catch (e: Exception) {
            Log.e("AxialViewModel", "Gagal unduh file: ${e.message}")
            throw IOException("Gagal download dari URL")
        }
    }

    fun updateBitmapForZ(z: Int, binaryData: ByteArray, dicomId: String? = null) {
        val result = repository.processBitmap(z, binaryData)

        if (result is ResultState.Success) {
            val bitmap = result.data ?: return
            imageWidth = bitmap.width
            imageHeight = bitmap.height

            if (!dicomId.isNullOrEmpty()) {
                FirebaseFirestore.getInstance()
                    .collection("dicomFiles")
                    .document(dicomId)
                    .update(mapOf("width" to imageWidth, "height" to imageHeight))
            }
        }

        _bitmap.value = result
    }
}
