package com.zstronics.ceibro.base.encryption

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

class EncryptData {

    fun encryptToCbc256(data: ByteArray): String {
        try {
           // val secretKey: ByteArray = "C@|BR0@uthS@Cre+AUTHVYU*B&^%I*/+".toByteArray()
            val secretKey: ByteArray = "C++BR0@uthS@Cre+AUTHVYU*B++%I*%+".toByteArray()
          //  val secretIV: ByteArray = "C@|BR0@uthS@Cre+".toByteArray()
            val secretIV: ByteArray = "C++BR0@uthS@Cre+".toByteArray()

            // Create a SecretKey from the user-provided key
            val secretKeySpec: SecretKey = SecretKeySpec(secretKey, "AES")
            // Create an IvParameterSpec from the user-provided IV
            val ivParameterSpec = IvParameterSpec(secretIV)
            // Create Cipher instance
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            // Initialize the Cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            // Perform the encryption
            val encryptedData = cipher.doFinal(data)
            //Converting to HEX
            return encryptedData.joinToString("") { "%02x".format(it and 0xFF.toByte()) }
        } catch (e: Exception) {
            throw RuntimeException("Error encrypting data", e)
        }
    }

}

fun encryptDataToAesCbcInHex(data: ByteArray): String {
    try {
        return EncryptData().encryptToCbc256(data)
    } catch (e: Exception) {
        throw RuntimeException("Error encrypting data", e)
    }
}