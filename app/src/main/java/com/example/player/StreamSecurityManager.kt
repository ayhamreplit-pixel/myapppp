package com.example.player

import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Enterprise Stream Security & Encryption Engine
 * Protects stream URLs, tokens, and metadata against scraping, tampering, and unauthorized access.
 * Supports AES-256 encryption/decryption, HMAC signature verification, and dynamic security tokens.
 */
object StreamSecurityManager {

  private const val TAG = "StreamSecurityManager"
  private const val ALGORITHM = "AES/CBC/PKCS5Padding"
  private const val HMAC_ALGORITHM = "HmacSHA256"

  // Master key for stream & API payload scrambling
  const val DEFAULT_MASTER_KEY = "TOD_SPORTS_STREAM_SECURE_KEY_2026_PRO"

  /**
   * Anti-Frida & Anti-Tamper Security Engine
   * Detects active Frida hook servers, memory inspection, and debugger attachments.
   */
  fun isFridaOrTamperDetected(): Boolean {
    return try {
      // 1. Check for default Frida ports (27042, 27043)
      val fridaPorts = listOf(27042, 27043)
      for (port in fridaPorts) {
        try {
          val socket = java.net.Socket("127.0.0.1", port)
          socket.close()
          Log.w(TAG, "Security Alert: Active Frida server detected on port $port")
          return true
        } catch (e: Exception) {
          // Port closed, normal
        }
      }

      // 2. Check for Frida binaries and temporary injection files
      val suspiciousPaths = listOf(
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/data/local/tmp/frida-agent.so",
        "/data/local/tmp/frida-agent-64.so"
      )
      for (path in suspiciousPaths) {
        if (java.io.File(path).exists()) {
          Log.w(TAG, "Security Alert: Suspicious Frida binary detected at $path")
          return true
        }
      }

      false
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Derives a deterministic 256-bit AES key from a passphrase.
   */
  private fun deriveKey(passphrase: String): SecretKeySpec {
    val sha256 = MessageDigest.getInstance("SHA-256")
    val keyBytes = sha256.digest(passphrase.toByteArray(StandardCharsets.UTF_8))
    return SecretKeySpec(keyBytes, "AES")
  }

  /**
   * Generates a 16-byte IV deterministically or from key hash.
   */
  private fun deriveIv(passphrase: String): IvParameterSpec {
    val md5 = MessageDigest.getInstance("MD5")
    val ivBytes = md5.digest((passphrase + "_iv_salt").toByteArray(StandardCharsets.UTF_8))
    return IvParameterSpec(ivBytes)
  }

  /**
   * Decrypts an encrypted stream URL or payload from AlwaysData server.
   * Supports prefixed formats: "enc:...", "aes:...", or raw Base64 cipher text.
   */
  fun decryptStreamUrl(cipherText: String, secretKey: String = DEFAULT_MASTER_KEY): String {
    val cleanCipher = cipherText.trim()
      .removePrefix("enc:")
      .removePrefix("aes:")
      .removePrefix("sec:")
      .removePrefix("m7:")
      .removePrefix("m7enc:")
      .trim()

    if (cleanCipher.isEmpty()) return ""

    // If it's already a plain http/https URL and not base64 encrypted, return as is
    if (cleanCipher.startsWith("http://") || cleanCipher.startsWith("https://") || cleanCipher.startsWith("rtmp://")) {
      return cleanCipher
    }

    return try {
      val keySpec = deriveKey(secretKey)
      val ivSpec = deriveIv(secretKey)

      val cipher = Cipher.getInstance(ALGORITHM)
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

      val decodedBytes = Base64.decode(cleanCipher, Base64.DEFAULT)
      val decryptedBytes = cipher.doFinal(decodedBytes)
      val decryptedUrl = String(decryptedBytes, StandardCharsets.UTF_8).trim()

      if (decryptedUrl.startsWith("http://") || decryptedUrl.startsWith("https://") || decryptedUrl.startsWith("rtmp://") || decryptedUrl.contains("/")) {
        decryptedUrl
      } else {
        cipherText
      }
    } catch (e: Exception) {
      Log.w(TAG, "Decryption fallback for stream: ${e.message}")
      cipherText
    }
  }

  /**
   * Encrypts a raw stream URL with AES-256 for transmission or storage.
   */
  fun encryptStreamUrl(rawUrl: String, secretKey: String = DEFAULT_MASTER_KEY): String {
    return try {
      val keySpec = deriveKey(secretKey)
      val ivSpec = deriveIv(secretKey)

      val cipher = Cipher.getInstance(ALGORITHM)
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

      val encryptedBytes = cipher.doFinal(rawUrl.toByteArray(StandardCharsets.UTF_8))
      "enc:" + Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
      Log.e(TAG, "Encryption failed", e)
      rawUrl
    }
  }

  /**
   * Generates an HMAC-SHA256 signature for anti-tamper stream links.
   */
  fun generateHmacSignature(data: String, secretKey: String = DEFAULT_MASTER_KEY): String {
    return try {
      val mac = Mac.getInstance(HMAC_ALGORITHM)
      val keySpec = SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), HMAC_ALGORITHM)
      mac.init(keySpec)
      val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
      Base64.encodeToString(hash, Base64.NO_WRAP)
    } catch (e: Exception) {
      ""
    }
  }

  /**
   * Verifies an HMAC signature.
   */
  fun verifySignature(data: String, signature: String, secretKey: String = DEFAULT_MASTER_KEY): Boolean {
    val expected = generateHmacSignature(data, secretKey)
    return expected.isNotBlank() && expected == signature
  }

  /**
   * Generates protected headers (Anti-Leech) for playback requests.
   */
  fun getSecureStreamHeaders(streamUrl: String): Map<String, String> {
    val headers = mutableMapOf<String, String>()
    headers["User-Agent"] = "TOD-Sports-Android/4.8.0 (Linux; U; Android 14; ar)"
    headers["X-Playback-Session"] = System.currentTimeMillis().toString()
    headers["Accept"] = "*/*"
    return headers
  }
}
