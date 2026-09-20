package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class TodApplication : Application(), ImageLoaderFactory {
  override fun newImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
      .memoryCache {
        MemoryCache.Builder(this)
          .maxSizePercent(0.20)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(cacheDir.resolve("channel_icons_cache"))
          .maxSizeBytes(40L * 1024 * 1024)
          .build()
      }
      .respectCacheHeaders(false)
      .crossfade(false)
      .build()
  }
}
