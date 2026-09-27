package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IptvDao {

  @Query("SELECT * FROM channels WHERE serverKey = :key ORDER BY name ASC")
  fun getChannelsByKeyFlow(key: String): Flow<List<ChannelEntity>>

  @Query("SELECT * FROM channels WHERE serverKey = :key ORDER BY name ASC")
  suspend fun getChannelsByKey(key: String): List<ChannelEntity>

  @Query("SELECT * FROM categories WHERE serverKey = :key ORDER BY categoryName ASC")
  fun getCategoriesByKeyFlow(key: String): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories WHERE serverKey = :key ORDER BY categoryName ASC")
  suspend fun getCategoriesByKey(key: String): List<CategoryEntity>

  @Query("SELECT * FROM channels WHERE isFavorite = 1")
  fun getFavoritesFlow(): Flow<List<ChannelEntity>>

  @Query("SELECT * FROM channels WHERE isFavorite = 1")
  suspend fun getFavoriteChannels(): List<ChannelEntity>

  @Query("UPDATE channels SET isFavorite = :isFav WHERE streamId = :streamId")
  suspend fun setFavorite(streamId: String, isFav: Boolean)

  @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' LIMIT 100")
  suspend fun searchChannels(query: String): List<ChannelEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChannels(channels: List<ChannelEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryEntity>)

  @Query("DELETE FROM channels WHERE serverKey = :key")
  suspend fun clearChannelsByKey(key: String)

  @Query("DELETE FROM categories WHERE serverKey = :key")
  suspend fun clearCategoriesByKey(key: String)

  @Query("SELECT * FROM playlists ORDER BY lastUpdated DESC")
  fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

  @Query("SELECT * FROM playlists ORDER BY lastUpdated DESC")
  suspend fun getAllPlaylists(): List<PlaylistEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlaylist(playlist: PlaylistEntity)

  @Query("DELETE FROM playlists WHERE id = :id")
  suspend fun deletePlaylist(id: String)

  @Query("SELECT COUNT(*) FROM channels")
  suspend fun getTotalChannelCount(): Int
}
