package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.XtreamCategory
import com.example.model.XtreamChannel

@Entity(tableName = "channels")
data class ChannelEntity(
  @PrimaryKey val streamId: String,
  val name: String,
  val iconUrl: String? = null,
  val categoryId: String? = null,
  val categoryName: String? = null,
  val playUrl: String,
  val streamType: String = "live",
  val isFavorite: Boolean = false,
  val lastWatchedTimestamp: Long = 0L,
  val serverKey: String = "default"
) {
  fun toXtreamChannel(): XtreamChannel {
    return XtreamChannel(
      streamId = streamId,
      name = name,
      iconUrl = iconUrl,
      categoryId = categoryId,
      playUrl = playUrl
    )
  }
}

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val categoryId: String,
  val categoryName: String,
  val channelCount: Int = 0,
  val serverKey: String = "default"
) {
  fun toXtreamCategory(): XtreamCategory {
    return XtreamCategory(
      categoryId = categoryId,
      categoryName = categoryName,
      channelCount = channelCount
    )
  }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
  @PrimaryKey val id: String,
  val playlistName: String,
  val serverUrl: String = "",
  val username: String = "",
  val password: String = "",
  val isM3u: Boolean = false,
  val m3uUrl: String = "",
  val lastUpdated: Long = System.currentTimeMillis(),
  val channelCount: Int = 0,
  val isActive: Boolean = false
)
