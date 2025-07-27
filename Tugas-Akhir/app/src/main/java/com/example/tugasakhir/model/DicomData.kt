package com.example.tugasakhir.model

data class DicomData(
    val id: String = "",
    val userId: String = "",
    val filename: String = "",
    val fileUrl: String = "",
    val width: Int = 0,
    val height: Int = 0,
//    val annotations: List<AnnotationData> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)