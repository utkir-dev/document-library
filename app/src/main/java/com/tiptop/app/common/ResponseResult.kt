package com.tiptop.app.common

sealed class ResponseResult<out T> {
    object Loading : ResponseResult<Nothing>()

    data class Success<out T>(
        val data: T
    ) : ResponseResult<T>()

    data class Failure(
        val errorMessage: String?
    ) : ResponseResult<Nothing>()

}