package com.example.tugasakhir.presentation.dicom

import android.net.Uri
import android.util.Log
import com.example.tugasakhir.model.DicomData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class DicomFirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val dicomCollection = firestore.collection("dicomFiles")

    suspend fun uploadDicomFile(uri: Uri, fileName: String): String {
        val ref = storage.reference.child("dicom/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun saveDicomToCloud(file: DicomData) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val data = file.copy(userId = userId, createdAt = System.currentTimeMillis())
            dicomCollection.document(file.id).set(data).await()
        } else {
            Log.e("DicomRepo", "User tidak terautentikasi, tidak dapat menyimpan data!")
        }
    }

    suspend fun getDicomFilesFromCloud(userId: String): List<DicomData> {
        val dokumen = dicomCollection.whereEqualTo("userId", userId).get().await()
//        Log.d("DicomFirebaseRepo", "✅ Dokumen Firestore: ${snapshot.size()}")
//        snapshot.documents.forEach {
////            Log.d("DicomFirebaseRepo", "📄 Dokumen ID: ${it.id}")
//        }
        return dokumen.documents.mapNotNull {
            it.toObject(DicomData::class.java)?.copy(id = it.id)
        }
    }


    suspend fun deleteDicomFile(file: DicomData) {
        try {
            val filename = file.filename
            storage.reference.child("dicom/$filename").delete().await()
        } catch (e: Exception) {
//            Log.w("DicomRepo", "⚠️ File sudah tidak ada di Storage: ${e.message}")
        }

        firestore.collection("dicomFiles").document(file.id).delete().await()
    }

    suspend fun deleteAnnotationsFolder(dicomId: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("annotations/$dicomId")

        try {
            val orientationFolders = storageRef.listAll().await()

            for (folder in orientationFolders.prefixes) {
                val items = folder.listAll().await()

                for (item in items.items) {
                    item.delete().await()
                }

                try {
                    folder.delete().await()
                } catch (e: Exception) {
                    Log.w("DicomRepo", "Gagal hapus folder orientasi: ${e.message}")
                }
            }

            try {
                storageRef.delete().await()
            } catch (e: Exception) {
                Log.w("DicomRepo", "Gagal hapus folder dicomId: ${e.message}")
            }

        } catch (e: Exception) {
            if (e.message?.contains("Object does not exist") == true) {
                Log.w("DicomRepo", "Folder anotasi tidak ditemukan, lanjutkan.")
            } else {
                throw e
            }
        }
    }


    suspend fun deleteAnnotationsFromFirestore(dicomId: String) {
        val orientations = listOf("axial", "sagittal", "coronal")
        val firestore = FirebaseFirestore.getInstance()

        for (orientation in orientations) {
            val slicesRef = firestore
                .collection("dicomFiles")
                .document(dicomId)
                .collection("annotations")
                .document(orientation)
                .collection("slices")

            try {
                val snapshots = slicesRef.get().await()
                for (doc in snapshots.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w("DicomRepo", "Gagal hapus anotasi $orientation: ${e.message}")
            }
        }
    }

    suspend fun deleteAnnotationDocuments(dicomId: String) {
        val orientations = listOf("axial", "sagittal", "coronal")
        val firestore = FirebaseFirestore.getInstance()
        val annotationsRef = firestore.collection("dicomFiles").document(dicomId).collection("annotations")

        for (orientation in orientations) {
            val orientationDocRef = annotationsRef.document(orientation)

            val slicesRef = orientationDocRef.collection("slices")
            try {
                val slices = slicesRef.get().await()
                for (slice in slices.documents) {
                    slice.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w("DicomRepo", "Gagal hapus slices di $orientation: ${e.message}")
            }

            try {
                orientationDocRef.delete().await()
            } catch (e: Exception) {
                Log.w("DicomRepo", "Gagal hapus dokumen $orientation: ${e.message}")
            }
        }
    }

    suspend fun deleteFromFirestore(id: String) {
        firestore.collection("dicomFiles").document(id).delete().await()
    }

}