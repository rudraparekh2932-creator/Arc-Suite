package com.example.data.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

/**
 * Secure Hardware Security Manager that delegates to DeviceHardwareIdService
 * to provide unique hardware ID generation, build telemetry, and cryptographic verification
 * to support the Arc 360 product key licensing system.
 */
object HardwareSecurityManager {

    fun getHardwareId(context: Context): String {
        return DeviceHardwareIdService.getInstance(context).getHardwareId()
    }

    fun getHardwareDetails(context: Context): DeviceHardwareDetails {
        return DeviceHardwareIdService.getInstance(context).getHardwareDetails()
    }

    /**
     * Generates a cryptographic verification signature binding a product key, user email,
     * and the device's unique Hardware ID.
     */
    fun generateHardwareBindingSignature(
        productKey: String,
        email: String,
        hardwareId: String,
        tier: String
    ): String {
        return DeviceHardwareIdService.getInstance(null ?: return DeviceHardwareIdService.HWID_SALT.let {
            val payload = "$productKey:$email:$hardwareId:$tier:${DeviceHardwareIdService.HWID_SALT}"
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(payload.toByteArray(StandardCharsets.UTF_8))
            digest.joinToString("") { "%02X".format(it) }
        })
    }

    /**
     * Validates whether a stored license signature matches the current device's hardware ID.
     */
    fun verifyHardwareBinding(
        productKey: String,
        email: String,
        hardwareId: String,
        tier: String,
        expectedSignature: String
    ): Boolean {
        val payload = "$productKey:$email:$hardwareId:$tier:${DeviceHardwareIdService.HWID_SALT}"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
        val computed = digest.joinToString("") { "%02X".format(it) }
        return computed.equals(expectedSignature, ignoreCase = true)
    }
}
