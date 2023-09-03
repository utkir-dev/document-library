package com.tiptop.domain.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tiptop.app.common.ResponseResult
import com.tiptop.domain.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override val currentUser: FirebaseUser?
        get() = auth.currentUser
    override val authState: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    override suspend fun signIn(email: String, password: String): ResponseResult<String?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            ResponseResult.Success(authResult.user?.uid)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override suspend fun signUp(email: String, password: String): ResponseResult<String?> {
        val res = auth.fetchSignInMethodsForEmail(email).await()
        val isNewUser = res.signInMethods?.isEmpty() ?: false
        return try {
            if (isNewUser) {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                ResponseResult.Success(result?.user?.uid)
            } else {
                ResponseResult.Failure("Bu email allaqachon ro'yxatdan o'tgan")
            }
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }


    override suspend fun signOut() {
        auth.signOut()
    }
}
