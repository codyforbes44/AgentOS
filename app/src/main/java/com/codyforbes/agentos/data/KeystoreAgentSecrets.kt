package com.codyforbes.agentos.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AES-GCM via the Android Keystore. Ciphertext lives in app-private files, not Room.
 */
class KeystoreAgentSecrets(
    context: Context,
) : AgentSecrets {
    private val directory = File(context.applicationContext.filesDir, DIRECTORY)
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    override suspend fun put(agentId: String, apiKey: String) {
        if (apiKey.isEmpty()) return
        withContext(Dispatchers.IO) {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey())
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))
            val payload = ByteArray(1 + iv.size + ciphertext.size)
            payload[0] = iv.size.toByte()
            System.arraycopy(iv, 0, payload, 1, iv.size)
            System.arraycopy(ciphertext, 0, payload, 1 + iv.size, ciphertext.size)
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(TAG, "Could not create agent secret directory")
                return@withContext
            }
            fileFor(agentId).writeBytes(payload)
        }
    }

    override suspend fun delete(agentId: String) {
        withContext(Dispatchers.IO) {
            val file = fileFor(agentId)
            if (file.exists() && !file.delete()) {
                Log.e(TAG, "Could not delete agent secret")
            }
        }
    }

    override suspend fun get(agentId: String): String? = withContext(Dispatchers.IO) {
        val file = fileFor(agentId)
        if (!file.exists()) return@withContext null
        try {
            val payload = file.readBytes()
            if (payload.size < 2) return@withContext null
            val ivLength = payload[0].toInt() and 0xFF
            if (ivLength <= 0 || payload.size <= 1 + ivLength) return@withContext null
            val iv = payload.copyOfRange(1, 1 + ivLength)
            val ciphertext = payload.copyOfRange(1 + ivLength, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                masterKey(),
                GCMParameterSpec(GCM_TAG_BITS, iv),
            )
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (_: Exception) {
            Log.e(TAG, "Could not read agent secret")
            null
        }
    }

    private fun fileFor(agentId: String): File {
        val safe = agentId.replace(UNSAFE, "_").ifBlank { "agent" }
        return File(directory, "$safe.bin")
    }

    private fun masterKey(): SecretKey {
        synchronized(keyStore) {
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
            generator.init(spec)
            return generator.generateKey()
        }
    }

    private companion object {
        const val TAG = "KeystoreAgentSecrets"
        const val DIRECTORY = "agent-secrets"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "agentos.agent-secrets"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        val UNSAFE = Regex("[^A-Za-z0-9._-]")
    }
}
