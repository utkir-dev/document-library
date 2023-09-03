package com.tiptop.presentation.screens.sign_up

interface SignUpViewModel {
    fun signUp(email: String, password: String, telegramUser: String)
}