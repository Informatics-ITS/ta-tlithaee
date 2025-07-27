package com.example.tugasakhir.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class AnnotationData(
    var id: String = "",
    var dicomId: String = "",
    var sliceIndex: Int = 0,
    var view: String = "",
    var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
    var createdAt: Long = 0L
)
