package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_keys")
data class ProductKeyEntity(
    @PrimaryKey
    val keyIndex: Int,
    val productKey: String,
    val tier: String,
    val isActivated: Boolean = false,
    val activatedOwner: String = "",
    val activatedDate: Long = 0L,
    val deviceSeatsMax: Int = 10,
    val deviceSeatsUsed: Int = 0
) {
    val tierNumber: Int
        get() = when {
            tier.contains("Enterprise", ignoreCase = true) || tier.contains("8TB", ignoreCase = true) || tier.contains("8 TB", ignoreCase = true) -> 5
            tier.contains("Business", ignoreCase = true) || tier.contains("4TB", ignoreCase = true) || tier.contains("4 TB", ignoreCase = true) -> 4
            tier.contains("Pro Creative", ignoreCase = true) || tier.contains("Creative", ignoreCase = true) || tier.contains("2TB", ignoreCase = true) || tier.contains("2 TB", ignoreCase = true) -> 3
            tier.contains("Ultimate", ignoreCase = true) || tier.contains("1TB", ignoreCase = true) || tier.contains("1 TB", ignoreCase = true) -> 2
            else -> 1 // Arc 360 Personal
        }

    val storageQuotaGb: Int
        get() = when (tierNumber) {
            5 -> 8192
            4 -> 4096
            3 -> 2048
            2 -> 1024
            else -> 500
        }

    val storageQuotaDisplay: String
        get() = when (tierNumber) {
            5 -> "8 TB"
            4 -> "4 TB"
            3 -> "2 TB"
            2 -> "1 TB"
            else -> "500 GB"
        }
}

data class LicenseState(
    val isLicensed: Boolean = false,
    val activeKey: String = "",
    val tier: String = "",
    val ownerName: String = "",
    val ownerEmail: String = "",
    val activationDate: Long = 0L,
    val expiryDate: String = "Perpetual Arc 360 License",
    val deviceName: String = "Google Android Device",
    val seatsUsed: Int = 1,
    val seatsMax: Int = 1,
    val storageQuotaGb: Int = 500
) {
    val tierNumber: Int
        get() = when {
            tier.contains("Enterprise", ignoreCase = true) || tier.contains("8TB", ignoreCase = true) || tier.contains("8 TB", ignoreCase = true) -> 5
            tier.contains("Business", ignoreCase = true) || tier.contains("4TB", ignoreCase = true) || tier.contains("4 TB", ignoreCase = true) -> 4
            tier.contains("Pro Creative", ignoreCase = true) || tier.contains("Creative", ignoreCase = true) || tier.contains("2TB", ignoreCase = true) || tier.contains("2 TB", ignoreCase = true) -> 3
            tier.contains("Ultimate", ignoreCase = true) || tier.contains("1TB", ignoreCase = true) || tier.contains("1 TB", ignoreCase = true) -> 2
            else -> 1
        }

    val storageDisplay: String
        get() = when (tierNumber) {
            5 -> "8 TB"
            4 -> "4 TB"
            3 -> "2 TB"
            2 -> "1 TB"
            else -> "500 GB"
        }

    val storageQuotaTb: Float
        get() = storageQuotaGb / 1024f
}

data class TierStorageSpec(
    val tierIndex: Int,
    val tierName: String,
    val shortName: String,
    val storageDisplay: String,
    val storageTb: Float,
    val storageGb: Int,
    val seatsDescription: String,
    val primaryColorHex: Long,
    val badgeLabel: String,
    val features: List<String>
)

val ArcTierSpecs = listOf(
    TierStorageSpec(
        tierIndex = 1,
        tierName = "Arc 360 Personal",
        shortName = "Personal (500 GB)",
        storageDisplay = "500 GB",
        storageTb = 0.5f,
        storageGb = 500,
        seatsDescription = "1 Individual Seat",
        primaryColorHex = 0xFF0284C7,
        badgeLabel = "500 GB Cloud",
        features = listOf(
            "500 GB Cloud Storage Quota",
            "1 Individual Personal Device Seat",
            "Arc 360 Mail (Outlook Alternative)",
            "Docs, Sheets, Slides & PDF Editor"
        )
    ),
    TierStorageSpec(
        tierIndex = 2,
        tierName = "Arc 360 Ultimate Studio",
        shortName = "Ultimate Studio (1 TB)",
        storageDisplay = "1 TB",
        storageTb = 1.0f,
        storageGb = 1024,
        seatsDescription = "5 High-Performance Seats",
        primaryColorHex = 0xFFD97706,
        badgeLabel = "1 TB Cloud",
        features = listOf(
            "1 TB Cloud Storage Quota",
            "5 Studio Power-User Seats",
            "Uncompressed 4K Video Caching",
            "Priority Differential Cloud Pipeline"
        )
    ),
    TierStorageSpec(
        tierIndex = 3,
        tierName = "Arc 360 Pro Creative Cloud",
        shortName = "Pro Creative (2 TB)",
        storageDisplay = "2 TB",
        storageTb = 2.0f,
        storageGb = 2048,
        seatsDescription = "10 Team Seats",
        primaryColorHex = 0xFF7C3AED,
        badgeLabel = "2 TB Cloud",
        features = listOf(
            "2 TB Cloud Storage Quota",
            "10 Verified Team Seats",
            "RAW Creative Asset Sync & Cache",
            "Photoshop & Premiere Cloud Render"
        )
    ),
    TierStorageSpec(
        tierIndex = 4,
        tierName = "Arc 360 Business Suite",
        shortName = "Business (4 TB)",
        storageDisplay = "4 TB",
        storageTb = 4.0f,
        storageGb = 4096,
        seatsDescription = "25 Corporate Seats",
        primaryColorHex = 0xFF059669,
        badgeLabel = "4 TB Cloud",
        features = listOf(
            "4 TB High-Capacity Cloud Vault",
            "25 Enterprise Workstation Seats",
            "Automated Revision History & Vault",
            "Full Arc 360 Mail Server Sync"
        )
    ),
    TierStorageSpec(
        tierIndex = 5,
        tierName = "Arc 360 Enterprise Ultra",
        shortName = "Enterprise Ultra (8 TB)",
        storageDisplay = "8 TB",
        storageTb = 8.0f,
        storageGb = 8192,
        seatsDescription = "Unlimited Multi-Seats",
        primaryColorHex = 0xFF2563EB,
        badgeLabel = "8 TB Cloud",
        features = listOf(
            "8 TB Maximum Cloud Quota",
            "Unlimited Team Device Seats",
            "Sub-50ms Delta Sync & Collab Hub",
            "Full Office + Creative Suite Access"
        )
    )
)

fun getTierSpecForTierString(tier: String): TierStorageSpec {
    return when {
        tier.contains("Enterprise", ignoreCase = true) || tier.contains("8TB", ignoreCase = true) || tier.contains("8 TB", ignoreCase = true) -> ArcTierSpecs[4]
        tier.contains("Business", ignoreCase = true) || tier.contains("4TB", ignoreCase = true) || tier.contains("4 TB", ignoreCase = true) -> ArcTierSpecs[3]
        tier.contains("Pro Creative", ignoreCase = true) || tier.contains("Creative", ignoreCase = true) || tier.contains("2TB", ignoreCase = true) || tier.contains("2 TB", ignoreCase = true) -> ArcTierSpecs[2]
        tier.contains("Ultimate", ignoreCase = true) || tier.contains("1TB", ignoreCase = true) || tier.contains("1 TB", ignoreCase = true) -> ArcTierSpecs[1]
        else -> ArcTierSpecs[0] // Arc 360 Personal
    }
}
