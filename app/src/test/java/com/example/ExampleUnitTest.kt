package com.example

import com.example.data.ai.OfflineAssistantEngine
import com.example.data.security.HardwareSecurityManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hardwareBinding_signatureVerification() {
        val productKey = "ARC-PRO-2026-X89B-4412"
        val email = "rudraparekh2932@gmail.com"
        val hwid = "HWID-82F1-A490-CC12-58E3"
        val tier = "ENTERPRISE_360"

        val signature = HardwareSecurityManager.generateHardwareBindingSignature(
            productKey = productKey,
            email = email,
            hardwareId = hwid,
            tier = tier
        )

        assertTrue(signature.isNotBlank())

        val isValid = HardwareSecurityManager.verifyHardwareBinding(
            productKey = productKey,
            email = email,
            hardwareId = hwid,
            tier = tier,
            expectedSignature = signature
        )

        assertTrue(isValid)

        // Tampering test
        val isTamperedValid = HardwareSecurityManager.verifyHardwareBinding(
            productKey = productKey,
            email = "attacker@mail.com",
            hardwareId = hwid,
            tier = tier,
            expectedSignature = signature
        )
        assertFalse(isTamperedValid)
    }

    @Test
    fun offlineAssistant_excelFormulaGeneration() {
        val result = OfflineAssistantEngine.processQuery("How do I do an XLOOKUP in Excel?")
        assertNotNull(result.formula)
        assertTrue(result.formula!!.contains("XLOOKUP"))
        assertTrue(result.reply.contains("XLOOKUP"))
    }

    @Test
    fun cloudStorageInfo_accuracyAndFormatting() {
        val info = com.example.data.model.CloudSyncInfo(
            storageUsedMb = 24.6f,
            totalStorageMb = 500f * 1024f,
            storageDisplayLimit = "500 GB",
            mailStorageMb = 5.2f,
            docsStorageMb = 8.1f,
            sheetsStorageMb = 4.3f,
            slidesStorageMb = 3.0f,
            pdfsStorageMb = 2.5f,
            creativeStorageMb = 1.5f
        )

        assertEquals("24.6 MB", info.formattedUsed)
        assertTrue(info.formattedAvailable.contains("GB"))
        assertTrue(info.formattedPercentage.contains("%"))
        assertTrue(info.usedPercentage > 0f)
    }
}
