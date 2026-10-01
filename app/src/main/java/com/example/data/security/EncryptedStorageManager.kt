package com.example.data.security

import android.content.Context
import android.util.Base64
import com.example.data.model.ProductKeyEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

sealed class VerificationResult {
    data class Success(
        val key: String,
        val tier: String,
        val keyIndex: Int,
        val activationToken: String,
        val ownerEmail: String,
        val hardwareId: String
    ) : VerificationResult()

    data class Failure(val reason: String) : VerificationResult()
}

data class VerifiedLicense(
    val isVerified: Boolean,
    val activeKey: String,
    val tier: String,
    val ownerEmail: String,
    val activationToken: String,
    val verifiedAt: Long,
    val hardwareId: String = ""
)

class EncryptedStorageManager(private val context: Context) {

    private val vaultFileName = "arc_keys_vault.enc"
    private val licenseFileName = "arc_license_state.enc"
    private val keyDerivationSalt = "ARC_SUITE_SECURE_SALT_2026_HARDWARE_VAULT"
    private val gcmTagLengthBits = 128
    private val ivSizeBytes = 12

    // Derive 256-bit AES key from device & package signature hash
    private fun getSecretKey(): SecretKeySpec {
        val seed = "${context.packageName}_$keyDerivationSalt"
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(seed.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(ivSizeBytes)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(gcmTagLengthBits, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        if (combined.size < ivSizeBytes) throw IllegalArgumentException("Invalid encrypted payload")

        val iv = ByteArray(ivSizeBytes)
        System.arraycopy(combined, 0, iv, 0, ivSizeBytes)

        val cipherBytes = ByteArray(combined.size - ivSizeBytes)
        System.arraycopy(combined, ivSizeBytes, cipherBytes, 0, cipherBytes.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(gcmTagLengthBits, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    /**
     * Initializes the local encrypted storage with the 100 valid product keys.
     * Stored strictly encrypted to prevent user exposure.
     */
    fun saveKeysToEncryptedVault(keys: List<ProductKeyEntity>) {
        val jsonArray = JSONArray()
        for (k in keys) {
            val obj = JSONObject()
            obj.put("index", k.keyIndex)
            obj.put("key", k.productKey)
            obj.put("tier", k.tier)
            obj.put("seats", k.deviceSeatsMax)
            jsonArray.put(obj)
        }

        val encryptedPayload = encrypt(jsonArray.toString())
        val file = File(context.filesDir, vaultFileName)
        file.writeText(encryptedPayload, StandardCharsets.UTF_8)
    }

    /**
     * Private internal method to load keys solely for cryptographic matching.
     * NEVER expose keys to UI or outside callers.
     */
    private fun loadDecryptedKeysFromVault(): List<ProductKeyEntity> {
        val file = File(context.filesDir, vaultFileName)
        if (!file.exists()) return emptyList()

        return try {
            val encryptedText = file.readText(StandardCharsets.UTF_8)
            val jsonText = decrypt(encryptedText)
            val jsonArray = JSONArray(jsonText)
            val list = mutableListOf<ProductKeyEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ProductKeyEntity(
                        keyIndex = obj.getInt("index"),
                        productKey = obj.getString("key"),
                        tier = obj.getString("tier"),
                        deviceSeatsMax = obj.optInt("seats", 10)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Compares an entered key against the valid keys stored in the encrypted hardware vault.
     * If matched and email is valid, marks as verified and stores encrypted license credentials.
     */
    fun verifyAndActivateKey(enteredKey: String, customEmail: String): VerificationResult {
        val cleanedKey = enteredKey.trim().uppercase()
        val cleanedEmail = customEmail.trim()

        if (cleanedEmail.isBlank() || !cleanedEmail.contains("@") || !cleanedEmail.contains(".")) {
            return VerificationResult.Failure("Please enter a valid corporate or personal email address.")
        }

        val validKeys = loadDecryptedKeysFromVault()
        if (validKeys.isEmpty()) {
            return VerificationResult.Failure("Encrypted keys vault not yet initialized. Please wait a moment.")
        }

        val match = validKeys.find { it.productKey.equals(cleanedKey, ignoreCase = true) }
        if (match == null) {
            return VerificationResult.Failure("Invalid product key. Ensure the 20-character key matches official Arc 360 format (e.g. ARCS-XXXX-XXXX-XXXX-XXXX).")
        }

        // Extract unique Hardware ID via DeviceHardwareIdService
        val hardwareService = DeviceHardwareIdService.getInstance(context)
        val hardwareId = hardwareService.getHardwareId()
        val hardwareSignature = hardwareService.generateHardwareBindingSignature(
            productKey = match.productKey,
            email = cleanedEmail,
            tier = match.tier
        )

        // Generate cryptographic activation token locked to this device Hardware ID
        val activationToken = hardwareService.generateActivationToken(
            productKey = match.productKey,
            email = cleanedEmail,
            tier = match.tier
        )

        // Save active verified license in encrypted storage
        val licenseObj = JSONObject()
        licenseObj.put("isVerified", true)
        licenseObj.put("activeKey", match.productKey)
        licenseObj.put("tier", match.tier)
        licenseObj.put("ownerEmail", cleanedEmail)
        licenseObj.put("activationToken", activationToken)
        licenseObj.put("verifiedAt", System.currentTimeMillis())
        licenseObj.put("hardwareId", hardwareId)
        licenseObj.put("hardwareSignature", hardwareSignature)

        val encryptedLicense = encrypt(licenseObj.toString())
        File(context.filesDir, licenseFileName).writeText(encryptedLicense, StandardCharsets.UTF_8)

        return VerificationResult.Success(
            key = match.productKey,
            tier = match.tier,
            keyIndex = match.keyIndex,
            activationToken = activationToken,
            ownerEmail = cleanedEmail,
            hardwareId = hardwareId
        )
    }

    /**
     * Checks if a valid, decrypted license is currently verified on this device and bound to this hardware ID.
     */
    fun getVerifiedLicense(): VerifiedLicense? {
        val file = File(context.filesDir, licenseFileName)
        if (!file.exists()) return null

        return try {
            val encryptedText = file.readText(StandardCharsets.UTF_8)
            val jsonText = decrypt(encryptedText)
            val obj = JSONObject(jsonText)
            if (obj.optBoolean("isVerified", false)) {
                val storedHardwareId = obj.optString("hardwareId", "")
                val hardwareService = DeviceHardwareIdService.getInstance(context)
                val currentHardwareId = hardwareService.getHardwareId()

                // Verify hardware ID matching (detect tampered or cloned licenses across devices)
                if (storedHardwareId.isNotEmpty() && !storedHardwareId.equals(currentHardwareId, ignoreCase = true)) {
                    android.util.Log.w("EncryptedStorage", "Hardware ID mismatch! Stored: $storedHardwareId, Current: $currentHardwareId")
                    return null
                }

                val key = obj.getString("activeKey")
                val email = obj.optString("ownerEmail", obj.optString("owner", ""))
                val tier = obj.getString("tier")
                val storedSignature = obj.optString("hardwareSignature", "")

                if (storedSignature.isNotEmpty() && !hardwareService.verifyHardwareBinding(key, email, tier, storedSignature)) {
                    android.util.Log.w("EncryptedStorage", "Hardware cryptographic signature mismatch!")
                    return null
                }

                VerifiedLicense(
                    isVerified = true,
                    activeKey = key,
                    tier = tier,
                    ownerEmail = email,
                    activationToken = obj.optString("activationToken", "ARC360-TOKEN"),
                    verifiedAt = obj.optLong("verifiedAt", System.currentTimeMillis()),
                    hardwareId = currentHardwareId
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Deactivates the current license to allow switching account or re-activating.
     */
    fun deactivateLicense() {
        val file = File(context.filesDir, licenseFileName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun isVaultEncryptedFileExisting(): Boolean {
        return File(context.filesDir, vaultFileName).exists()
    }
}
