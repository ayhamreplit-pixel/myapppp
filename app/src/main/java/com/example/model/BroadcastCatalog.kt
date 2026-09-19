package com.example.model

object BroadcastCatalog {
  val placeholderStream = BroadcastStream(
    id = "initial_stream",
    title = "بث مباشر",
    subtitle = "مشغل البث الاحترافي",
    category = "Live",
    streamUrl = "",
    format = StreamFormat.HLS,
    isLive = true
  )
}
