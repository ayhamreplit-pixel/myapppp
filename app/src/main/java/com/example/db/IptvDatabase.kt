package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [ChannelEntity::class, CategoryEntity::class, PlaylistEntity::class],
  version = 1,
  exportSchema = false
)
abstract class IptvDatabase : RoomDatabase() {

  abstract fun iptvDao(): IptvDao

  companion object {
    @Volatile
    private var INSTANCE: IptvDatabase? = null

    fun getDatabase(context: Context): IptvDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          IptvDatabase::class.java,
          "iptv_master_database.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
