package com.tiptop.app.common

import android.util.Log
import java.io.*
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encryptor {
    fun encryptToFile(
        kerStr: String,
        specStr: String,
        inputStream: InputStream,
        out: OutputStream,
        function: (Boolean) -> Unit
    ) {
        var success = false
        try {
            val iv = IvParameterSpec(specStr.toByteArray(StandardCharsets.UTF_8))
            val keySpec =
                SecretKeySpec(kerStr.toByteArray(StandardCharsets.UTF_8), Utils().getSecretKey())
            val c = Cipher.getInstance(Utils().getEncryptor())
            c.init(Cipher.ENCRYPT_MODE, keySpec, iv)
            val cipherOutputStream = CipherOutputStream(out, c)
            var count = 0
            val buffer = ByteArray(Utils().getBlockBuffer())
            while (inputStream.read(buffer).also { count = it } > 0) {
                cipherOutputStream.write(buffer, 0, count)
            }
            success = true
        } catch (_: Exception) {
            success = false
        } finally {
            out.close()
            function(success)
        }
    }

    fun getEncryptedBytes(
        keyStr: String,
        specStr: String,
        byteArray: ByteArray,
        function: (ByteArray?) -> Unit
    ) {
        Log.d("createRemoteFile","getEncryptedBytes")

        try {
            val iv = IvParameterSpec(specStr.toByteArray(StandardCharsets.UTF_8))
            val keySpec =
                SecretKeySpec(keyStr.toByteArray(StandardCharsets.UTF_8), Utils().getSecretKey())
            val cipher = Cipher.getInstance(Utils().getEncryptor())
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
            val encryptedData = cipher.doFinal(byteArray)
            Log.d("createRemoteFile","encryptedData $encryptedData")

            function(encryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("createRemoteFile","Exception : ${e.message}")

            function(null)
        }
    }

    fun getDecryptedBytes(
        keyStr: String,
        specStr: String,
        byteArray: ByteArray,
        function: (ByteArray?) -> Unit
    ) {
        try {
            val iv = IvParameterSpec(specStr.toByteArray(StandardCharsets.UTF_8))
            val keySpec =
                SecretKeySpec(keyStr.toByteArray(StandardCharsets.UTF_8), Utils().getSecretKey())
            val cipher = Cipher.getInstance(Utils().getEncryptor())
            cipher.init(
                Cipher.DECRYPT_MODE,
                keySpec,
                iv
            )
            val decryptedData = cipher.doFinal(byteArray)
            function(decryptedData)
        } catch (_: Exception) {
            function(null)
        }
    }
}