package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductKeyDao {
    @Query("SELECT * FROM product_keys ORDER BY keyIndex ASC")
    fun getAllKeys(): Flow<List<ProductKeyEntity>>

    @Query("SELECT * FROM product_keys WHERE productKey = :key LIMIT 1")
    suspend fun getKeyByCode(key: String): ProductKeyEntity?

    @Query("SELECT * FROM product_keys WHERE isActivated = 1 LIMIT 1")
    suspend fun getActivatedKey(): ProductKeyEntity?

    @Query("SELECT COUNT(*) FROM product_keys")
    suspend fun getKeyCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(keys: List<ProductKeyEntity>)

    @Update
    suspend fun updateKey(key: ProductKeyEntity)

    @Query("UPDATE product_keys SET isActivated = 0")
    suspend fun deactivateAllKeys()

    @Query("UPDATE product_keys SET isActivated = 1, activatedOwner = :owner, activatedDate = :date WHERE productKey = :key")
    suspend fun activateKey(key: String, owner: String, date: Long)
}
