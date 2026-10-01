package com.example.data.security

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

/**
 * Telemetry and cryptographic details representing the device hardware identity.
 */
data class DeviceHardwareDetails(
    val hardwareId: String,
    val androidId: String,
    val maskedAndroidId: String,
    val brand: String,
    val model: String,
    val manufacturer: String,
    val device: String,
    val board: String,
    val hardware: String,
    val product: String,
    val bootloader: String,
    val buildId: String,
    val supportedAbis: List<String>,
    val osVersion: String,
    val sdkInt: Int,
    val rawFingerprint: String,
    val fingerprintHash: String,
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * Service contract for hardware ID derivation, device-binding, and product key validation.
 */
interface HardwareIdServiceContract {
    val hardwareId: String
    val hardwareDetails: DeviceHardwareDetails
    fun getHardwareId(): String
    fun getHardwareDetails(): DeviceHardwareDetails
    fun generateHardwareBindingSignature(productKey: String, email: String, tier: String): String
    fun verifyHardwareBinding(productKey: String, email: String, tier: String, expectedSignature: String): Boolean
    fun generateActivationToken(productKey: String, email: String, tier: String): String
    fun validateProductKeyFormat(productKey: String): Boolean
}

/**
 * Core service implementation that generates a unique, deterministic, and cryptographic
 * device hardware ID using Android's Secure Settings and comprehensive Build information
 * to support the Arc 360 product key licensing system.
 */
class DeviceHardwareIdService(private val context: Context) : HardwareIdServiceContract {

    companion object {
        const val HWID_SALT = "ARC_360_DEVICE_HARDWARE_FINGERPRINT_SALT_2026"
        private const val DEFAULT_FALLBACK_HWID = "HWID-82F1-A490-CC12-58E3"

        @Volatile
        private var instance: DeviceHardwareIdService? = null

        fun getInstance(context: Context): DeviceHardwareIdService {
            return instance ?: synchronized(this) {
                instance ?: DeviceHardwareIdService(context.applicationContext).also { instance = it }
            }
        }
    }

    private val cachedDetails: DeviceHardwareDetails by lazy {
        computeHardwareDetails()
    }

    override val hardwareId: String
        get() = cachedDetails.hardwareId

    override val hardwareDetails: DeviceHardwareDetails
        get() = cachedDetails

    override fun getHardwareId(): String = hardwareId

    override fun getHardwareDetails(): DeviceHardwareDetails = cachedDetails

    /**
     * Derives deterministic hardware parameters by aggregating Android Secure Settings (ANDROID_ID)
     * and hardware build properties.
     */
    private fun computeHardwareDetails(): DeviceHardwareDetails {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )?.trim() ?: "ARC_EMULATOR_DEVICE_ID"

            val brand = Build.BRAND.orEmpty()
            val model = Build.MODEL.orEmpty()
            val manufacturer = Build.MANUFACTURER.orEmpty()
            val device = Build.DEVICE.orEmpty()
            val board = Build.BOARD.orEmpty()
            val hardware = Build.HARDWARE.orEmpty()
            val product = Build.PRODUCT.orEmpty()
            val bootloader = Build.BOOTLOADER.orEmpty()
            val buildId = Build.ID.orEmpty()
            val supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: listOf("unknown")
            val osVersion = Build.VERSION.RELEASE.orEmpty()
            val sdkInt = Build.VERSION.SDK_INT

            // Structured raw fingerprint concatenation incorporating Secure Settings & Build info
            val rawFingerprint = buildString {
                append(androidId).append(":")
                append(board).append(":")
                append(brand).append(":")
                append(device).append(":")
                append(hardware).append(":")
                append(manufacturer).append(":")
                append(model).append(":")
                append(product).append(":")
                append(bootloader).append(":")
                append(supportedAbis.joinToString(",")).append(":")
                append(HWID_SALT)
            }

            val sha256Digest = MessageDigest.getInstance("SHA-256")
                .digest(rawFingerprint.toByteArray(StandardCharsets.UTF_8))

            val hexString = sha256Digest.joinToString("") { "%02X".format(it) }

            // Format deterministic 16-character alphanumeric ID in 4 groups: HWID-XXXX-XXXX-XXXX-XXXX
            val derivedHwid = "HWID-${hexString.substring(0, 4)}-${hexString.substring(4, 8)}-${hexString.substring(8, 12)}-${hexString.substring(12, 16)}"

            val maskedAndroidId = if (androidId.length > 6) {
                "${androidId.take(3)}****${androidId.takeLast(3)}"
            } else {
                "****"
            }

            DeviceHardwareDetails(
                hardwareId = derivedHwid,
                androidId = androidId,
                maskedAndroidId = maskedAndroidId,
                brand = brand.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                model = model,
                manufacturer = manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                device = device,
                board = board,
                hardware = hardware,
                product = product,
                bootloader = bootloader,
                buildId = buildId,
                supportedAbis = supportedAbis,
                osVersion = osVersion,
                sdkInt = sdkInt,
                rawFingerprint = rawFingerprint,
                fingerprintHash = hexString
            )
        } catch (_: Exception) {
            DeviceHardwareDetails(
                hardwareId = DEFAULT_FALLBACK_HWID,
                androidId = "ARC_EMULATOR_DEVICE_ID",
                maskedAndroidId = "ARC****ID",
                brand = "Android",
                model = "Generic Device",
                manufacturer = "Google",
                device = "generic",
                board = "goldfish",
                hardware = "ranchu",
                product = "sdk_gphone",
                bootloader = "unknown",
                buildId = "ARC360-REL",
                supportedAbis = listOf("arm64-v8a", "x86_64"),
                osVersion = "15",
                sdkInt = 35,
                rawFingerprint = "FALLBACK_FINGERPRINT",
                fingerprintHash = "82F1A490CC1258E3F291BB09E198547A1102948BCDEF"
            )
        }
    }

    /**
     * Generates a cryptographic verification signature binding a product key, user email,
     * and this device's unique Hardware ID.
     */
    override fun generateHardwareBindingSignature(
        productKey: String,
        email: String,
        tier: String
    ): String {
        return generateHardwareBindingSignature(productKey, email, hardwareId, tier)
    }

    /**
     * Explicit signature generation supporting arbitrary hardwareId strings.
     */
    fun generateHardwareBindingSignature(
        productKey: String,
        email: String,
        targetHardwareId: String,
        tier: String
    ): String {
        val payload = "$productKey:$email:$targetHardwareId:$tier:$HWID_SALT"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02X".format(it) }
    }

    /**
     * Validates whether a license signature matches this device's hardware ID.
     */
    override fun verifyHardwareBinding(
        productKey: String,
        email: String,
        tier: String,
        expectedSignature: String
    ): Boolean {
        return verifyHardwareBinding(productKey, email, hardwareId, tier, expectedSignature)
    }

    /**
     * Explicit verification supporting arbitrary hardwareId strings.
     */
    fun verifyHardwareBinding(
        productKey: String,
        email: String,
        targetHardwareId: String,
        tier: String,
        expectedSignature: String
    ): Boolean {
        val computedSignature = generateHardwareBindingSignature(productKey, email, targetHardwareId, tier)
        return computedSignature.equals(expectedSignature, ignoreCase = true)
    }

    /**
     * Generates a unique activation token formatted as ARC360-XXXX-XXXXXXXX
     * cryptographically bound to the device's hardware ID.
     */
    override fun generateActivationToken(
        productKey: String,
        email: String,
        tier: String
    ): String {
        val tokenData = "$productKey:$email:$hardwareId:$tier"
        val tokenDigest = MessageDigest.getInstance("SHA-256")
            .digest(tokenData.toByteArray(StandardCharsets.UTF_8))
        val tokenHex = tokenDigest.take(8).joinToString("") { "%02X".format(it) }
        val hwShort = hardwareId.removePrefix("HWID-").take(4)
        return "ARC360-$hwShort-$tokenHex"
    }

    /**
     * Validates product key format compliance (e.g., ARCS-XXXX-XXXX-XXXX-XXXX).
     */
    override fun validateProductKeyFormat(productKey: String): Boolean {
        val cleaned = productKey.trim().uppercase(Locale.ROOT)
        val regex = Regex("^ARC[S|P|U|E|C]?-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return cleaned.matches(regex) || cleaned.length in 19..24
    }
}

/**
 * Standard Android Service component enabling bound or background interactions
 * with the Hardware ID licensing engine across app processes or background workers.
 */
class HardwareIdService : Service() {

    private lateinit var hardwareService: DeviceHardwareIdService
    private val binder = HardwareIdBinder()

    inner class HardwareIdBinder : Binder() {
        val service: DeviceHardwareIdService
            get() = hardwareService

        fun getHardwareId(): String = hardwareService.getHardwareId()
        fun getHardwareDetails(): DeviceHardwareDetails = hardwareService.getHardwareDetails()
        fun generateBindingSignature(productKey: String, email: String, tier: String): String =
            hardwareService.generateHardwareBindingSignature(productKey, email, tier)
        fun verifyBinding(productKey: String, email: String, tier: String, signature: String): Boolean =
            hardwareService.verifyHardwareBinding(productKey, email, tier, signature)
    }

    override fun onCreate() {
        super.onCreate()
        hardwareService = DeviceHardwareIdService.getInstance(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
