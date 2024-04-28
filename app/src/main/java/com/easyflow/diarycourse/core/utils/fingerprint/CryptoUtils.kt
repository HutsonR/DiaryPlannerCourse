package com.easyflow.diarycourse.core.utils.fingerprint

import android.annotation.SuppressLint
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PublicKey
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

/*
*  ORIGINAL https://habr.com/ru/companies/e-legion/articles/317706/
* */
class CryptoUtils {

    private var sKeyStore: KeyStore = KeyStore.getInstance(KEY_STORE)
    private var sKeyPairGenerator: KeyPairGenerator? = null
    private var sCipher: Cipher? = null

    private fun prepare(): Boolean {
        return getKeyStore() && getCipher() && getKey()
    }

    // Кейстор хранит только криптографические ключи
    private fun getKeyStore(): Boolean {
        try {
            sKeyStore = KeyStore.getInstance(KEY_STORE)
            sKeyStore.load(null)
            return true
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("GetInstance")
    private fun getCipher(): Boolean {
        try {
            sCipher = Cipher.getInstance(TRANSFORMATION)
            return true
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
        return false
    }

    private fun getKey(): Boolean {
        try {
            return sKeyStore.containsAlias(KEY_ALIAS) || generateNewKey()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
        return false
    }

    private fun generateNewKey(): Boolean {
        if (getKeyPairGenerator()) {
            try {
                sKeyPairGenerator?.let { keyPair ->
                    keyPair.initialize(
                        KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .setUserAuthenticationRequired(true)
                            .build()
                    )
                    keyPair.generateKeyPair()
                    return true
                }
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun getKeyPairGenerator(): Boolean {
        try {
            sKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE)
            return true
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }
        return false
    }

    // Cipher
    private fun initCipher(mode: Int): Boolean {
        try {
            sKeyStore.load(null)
            when (mode) {
                Cipher.ENCRYPT_MODE -> initEncodeCipher(mode)
                Cipher.DECRYPT_MODE -> initDecodeCipher(mode)
                else -> return false //this cipher is only for encode\decode
            }
            return true
        } catch (e: Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException -> {
                    deleteInvalidKey()
                    return false
                }
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException -> throw RuntimeException("Failed to init Cipher", e)
                else -> throw e
            }
        }
    }

    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableKeyException::class,
        InvalidKeyException::class
    )
    private fun initDecodeCipher(mode: Int) {
        sCipher?.init(mode, sKeyStore.getKey(KEY_ALIAS, null))
    }

    @Throws(
        KeyStoreException::class,
        InvalidKeySpecException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun initEncodeCipher(mode: Int) {
        val key: PublicKey = sKeyStore.getCertificate(KEY_ALIAS).publicKey

        // workaround for using public key
        // from https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html
        val unrestricted = KeyFactory.getInstance(key.algorithm).generatePublic(X509EncodedKeySpec(key.encoded))
        // from https://code.google.com/p/android/issues/detail?id=197719
        val spec = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
        sCipher?.init(mode, unrestricted, spec)
    }

    fun deleteInvalidKey() {
        if (getKeyStore()) {
            try {
                sKeyStore.deleteEntry(KEY_ALIAS)
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            }
        }
    }

    @Nullable
    fun getCryptoObject(): FingerprintManagerCompat.CryptoObject? {
        return if (prepare() && initCipher(Cipher.DECRYPT_MODE)) {
            sCipher?.let { FingerprintManagerCompat.CryptoObject(it) }
        } else null
    }


    fun encode(inputBytes: ByteArray): String? {
        try {
            if (prepare() && initCipher(Cipher.ENCRYPT_MODE)) {
                sCipher?.let { cipher ->
                    Log.d("debugTag", "encode bytes before ${inputBytes.joinToString { it.toString() }}")
                    val bytes: ByteArray = cipher.doFinal(inputBytes)
                    Log.d("debugTag", "encode bytes $${bytes.joinToString { it.toString() }}")
                    return Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }
        } catch (exception: IllegalBlockSizeException) {
            exception.printStackTrace()
        } catch (exception: BadPaddingException) {
            exception.printStackTrace()
        }
        return null
    }


    fun decode(encodedString: String?, cipher: Cipher): String? {
        try {
            val bytes = Base64.decode(encodedString, Base64.NO_WRAP)
            Log.d("debugTag", "decode bytes ${bytes.joinToString { it.toString() }}")
            return String(cipher.doFinal(bytes))
        } catch (exception: IllegalBlockSizeException) {
            exception.printStackTrace()
        } catch (exception: BadPaddingException) {
            exception.printStackTrace()
        }
        return null
    }

    companion object {
        private const val KEY_ALIAS = "key_for_pin"
        private const val KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    }
}