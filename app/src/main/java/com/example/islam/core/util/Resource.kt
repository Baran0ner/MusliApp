package com.example.islam.core.util

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

val <T> Resource<T>.isLoading get() = this is Resource.Loading
val <T> Resource<T>.isSuccess get() = this is Resource.Success
val <T> Resource<T>.isError   get() = this is Resource.Error
