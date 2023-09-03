package com.tiptop.domain

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tiptop.app.common.ResponseResult

interface AuthRepository {
    val currentUser: FirebaseUser?
    val authState: FirebaseAuth?
    suspend fun signIn(email: String, password: String): ResponseResult<String?>
    suspend fun signUp(email: String, password: String): ResponseResult<String?>
    suspend fun signOut()
}


