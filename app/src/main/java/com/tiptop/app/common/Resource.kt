package com.tiptop.app.common

data class Resource<out T>(val status: Status, val data: T?, val msg: String?) {
    companion object {
        fun <T> default(data: T?): Resource<T> {
            return Resource(Status.DEFAULT, data, null)
        }
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(data: T?, msg: String?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}

enum class Status { SUCCESS, ERROR, LOADING,DEFAULT }