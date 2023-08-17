package com.feyzaeda.casestudy.util

sealed class Results<out R> {
    data class Loading<out T>(val data: T) : Results<T>()
    data class Success<out T>(val data: T) : Results<T>()
    data class Error(val error: String) : Results<Nothing>()
}