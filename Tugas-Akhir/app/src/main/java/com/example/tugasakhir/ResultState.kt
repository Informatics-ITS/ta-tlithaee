package com.example.tugasakhir

sealed class ResultState<Type> (
    val data: Type? = null,
    val message: String? = null
) {
    class Success<Type>(data: Type?): ResultState<Type>(data)
    class Error<Type>(message: String?, data: Type? = null): ResultState<Type>(data, message)
    class Loading<Type>: ResultState<Type>()
}