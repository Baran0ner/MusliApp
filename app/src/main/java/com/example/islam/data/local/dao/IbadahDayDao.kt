package com.example.islam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.islam.data.local.entity.IbadahDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IbadahDayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: IbadahDayEntity)

    @Query("SELECT * FROM ibadah_day_status WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): IbadahDayEntity?

    @Query("SELECT * FROM ibadah_day_status WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetween(startDate: String, endDate: String): Flow<List<IbadahDayEntity>>
}
