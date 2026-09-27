package com.example.model

object BroadcastCatalog {

  val placeholderStream = BroadcastStream(
    id = "demo_bein_1",
    title = "beIN SPORTS 1 HD",
    subtitle = "دوري أبطال أوروبا • بث مباشر 4K",
    category = "TOD Sports Live",
    streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    format = StreamFormat.HLS,
    isLive = true,
    logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png"
  )

  val curatedDemoStreams = listOf(
    BroadcastStream(
      id = "demo_bein_1",
      title = "beIN SPORTS 1 HD",
      subtitle = "دوري أبطال أوروبا • تعليق عربي 1 و 2",
      category = "TOD Sports Live",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      format = StreamFormat.HLS,
      isLive = true,
      logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png"
    ),
    BroadcastStream(
      id = "demo_bein_4k",
      title = "beIN SPORTS 4K Ultra",
      subtitle = "قمة الجولة • دقة 4K 60FPS فائقة الوضوح",
      category = "TOD Sports Live",
      streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
      format = StreamFormat.HLS,
      isLive = true,
      logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png"
    ),
    BroadcastStream(
      id = "demo_sports_news",
      title = "beIN SPORTS الإخبارية",
      subtitle = "حصاد اليوم الرياضي والنتائج المباشرة",
      category = "TOD Sports Live",
      streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
      format = StreamFormat.HLS,
      isLive = true
    ),
    BroadcastStream(
      id = "demo_alkass_extra",
      title = "قناة الكأس EXTRA HD",
      subtitle = "البطولات الآسيوية والخليجية المباشرة",
      category = "TOD Sports Live",
      streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
      format = StreamFormat.HLS,
      isLive = true
    ),
    BroadcastStream(
      id = "demo_tod_cinema_4k",
      title = "TOD Cinema 4K",
      subtitle = "عالم السينما والأفلام الحصرية HDR",
      category = "TOD Movies & Series",
      streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
      format = StreamFormat.PROGRESSIVE,
      isLive = false
    ),
    BroadcastStream(
      id = "demo_national_geographic",
      title = "ناشيونال جيوغرافيك 4K",
      subtitle = "عالم الطبيعة والوثائقيات بدقة مذهلة",
      category = "TOD Documentaries",
      streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
      format = StreamFormat.PROGRESSIVE,
      isLive = false
    )
  )

  fun getInitialDemoChannels(): List<XtreamChannel> {
    return curatedDemoStreams.map {
      XtreamChannel(
        streamId = it.id,
        name = it.title,
        iconUrl = it.logoUrl,
        categoryId = it.category,
        playUrl = it.streamUrl
      )
    }
  }

  fun getInitialDemoCategories(): List<XtreamCategory> {
    val channels = getInitialDemoChannels()
    val grouped = channels.groupBy { it.categoryId ?: "TOD Sports Live" }
    val list = mutableListOf<XtreamCategory>()
    list.add(XtreamCategory("ALL", "جميع القنوات", channels.size))
    grouped.forEach { (cat, chs) ->
      list.add(XtreamCategory(cat, cat, chs.size))
    }
    return list
  }
}
