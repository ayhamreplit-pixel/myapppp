package com.example.model

/**
 * Strips quality tags like [HD], (HD), HD, FHD, 4K, UHD, SD, HEVC, 1080p, 720p, 50fps, 60fps,
 * server prefixes like AR:, VIP:, | AR | so channel titles remain clean and uncluttered.
 */
object ChannelTitleFormatter {

  fun formatTitle(title: String): String {
    if (title.isBlank()) return "قناة البث"
    var clean = title.trim()
    
    // 1. Remove prefixes like "AR:", "VIP:", "| AR |", "| BEIN |", "BEIN:", "OSN:"
    clean = clean.replace(Regex("^(?:\\|[A-Za-z0-9_ -]+\\||[A-Za-z0-9_-]+:)\\s*"), "")
    
    // 2. Remove bracketed quality tags: [HD], [1080p], (4K), (FHD), [UHD], [HEVC], [H.265], [50fps], etc.
    clean = clean.replace(Regex("(?i)\\[\\s*(?:FHD|UHD|4K|HD|SD|HEVC|H\\.?265|1080p|720p|50fps|60fps|MULTI|HQ)[^\\]]*\\]"), "")
    clean = clean.replace(Regex("(?i)\\(\\s*(?:FHD|UHD|4K|HD|SD|HEVC|H\\.?265|1080p|720p|50fps|60fps|MULTI|HQ)[^\\)]*\\)"), "")
    
    // 3. Remove standalone resolution markers in the text (like "1080p", "4K", "FHD", "720p")
    clean = clean.replace(Regex("(?i)\\b(?:FHD|UHD|4K|HEVC|H\\.?265|1080p|720p|50fps|60fps)\\b"), "")
    
    // 4. Remove isolated trailing "HD" or "SD"
    clean = clean.replace(Regex("(?i)\\s+(?:HD|SD)$"), "")
    
    // 5. Clean up redundant spaces and edge symbols
    clean = clean.replace(Regex("\\s+"), " ").trim(' ', '-', '|', ':', '_')
    
    return if (clean.isBlank()) title else clean
  }
}
