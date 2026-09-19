package com.example.model

object BroadcastCatalog {
  val defaultStreams = listOf(
    // Live Sports Broadcasts (HLS)
    BroadcastStream(
      id = "tod_sports_1",
      title = "Real Madrid vs Manchester City",
      subtitle = "UEFA Champions League - Quarter Final Live",
      category = "Live Sports",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      format = StreamFormat.HLS,
      isLive = true,
      channelNumber = "TOD 1 HD",
      tournamentOrLeague = "UEFA Champions League",
      score = "2 - 1",
      matchTime = "67'",
      resolutionLabel = "1080p 60FPS",
      description = "Live premium broadcast in pristine 1080p 60fps with multi-audio commentary, instant DVR seek, and real-time match event markers.",
      moments = listOf(
        MatchMoment(14 * 60, "14'", "⚽ GOAL! Bellingham header into top right corner", MatchEventType.GOAL),
        MatchMoment(31 * 60, "31'", "🟨 Yellow Card for tactical foul", MatchEventType.YELLOW_CARD),
        MatchMoment(45 * 60, "45'", "⏱️ Half Time (1 - 0)", MatchEventType.HALF_TIME),
        MatchMoment(53 * 60, "53'", "⚽ GOAL! De Bruyne stunning equalizer", MatchEventType.GOAL),
        MatchMoment(64 * 60, "64'", "⚽ GOAL! Vinicius Jr counter attack finish", MatchEventType.GOAL)
      )
    ),

    BroadcastStream(
      id = "tod_sports_2",
      title = "Liverpool vs Arsenal - Premier League",
      subtitle = "Super Sunday Live Match Broadcast",
      category = "Live Sports",
      streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
      format = StreamFormat.HLS,
      isLive = true,
      channelNumber = "TOD 2 HD",
      tournamentOrLeague = "Premier League",
      score = "1 - 1",
      matchTime = "82'",
      resolutionLabel = "1080p HDR",
      description = "High intensity Anfield clash broadcasted live with dual commentary channels and adaptive multi-bitrate HLS streaming.",
      moments = listOf(
        MatchMoment(22 * 60, "22'", "⚽ GOAL! Salah clinical left-foot strike", MatchEventType.GOAL),
        MatchMoment(45 * 60, "45'", "⏱️ Halftime whistle", MatchEventType.HALF_TIME),
        MatchMoment(70 * 60, "70'", "⚽ GOAL! Saka equalizer after defensive scramble", MatchEventType.GOAL),
        MatchMoment(76 * 60, "76'", "🟥 Red Card after second bookable offense", MatchEventType.RED_CARD)
      )
    ),

    // Multi-Audio & Subtitles HLS Stream
    BroadcastStream(
      id = "apple_bipbop_hls",
      title = "BipBop Multichannel HLS Master",
      subtitle = "Multi-Audio (Eng/Fra/Spa) & Closed Captions",
      category = "HLS Broadcast",
      streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
      format = StreamFormat.HLS,
      isLive = false,
      channelNumber = "TEST HLS",
      tournamentOrLeague = "Broadcast Lab",
      resolutionLabel = "Multi-Bitrate",
      description = "Apple reference HLS stream featuring multiple alternative audio language tracks, embedded subtitles, and 16:9 adaptive bitrates."
    ),

    // Advanced HEVC HLS Stream
    BroadcastStream(
      id = "apple_hevc_hls",
      title = "Advanced HEVC HLS Master",
      subtitle = "Full High-Dynamic Multi-Bitrate Feed",
      category = "HLS Broadcast",
      streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8",
      format = StreamFormat.HLS,
      isLive = false,
      channelNumber = "CINEMA 1",
      tournamentOrLeague = "Apple Reference",
      resolutionLabel = "1080p FHD",
      description = "Adaptive HTTP Live Streaming master feed with high quality multi-rate encoding ladder and surround sound."
    ),

    // DASH Multi-Bitrate Streams (.mpd)
    BroadcastStream(
      id = "envivio_dash",
      title = "Envivio Multi-Bitrate DASH",
      subtitle = "Dynamic Adaptive Streaming over HTTP (MPD)",
      category = "DASH Broadcast",
      streamUrl = "https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd",
      format = StreamFormat.DASH,
      isLive = false,
      channelNumber = "DASH 4K",
      tournamentOrLeague = "Ultra HD",
      resolutionLabel = "HD DASH",
      description = "Industry standard MPEG-DASH broadcast feed with segmented MPD manifest, multiple audio representations, and seamless adaptation."
    ),

    BroadcastStream(
      id = "bbb_dash",
      title = "Big Buck Bunny 60FPS DASH",
      subtitle = "High Frame Rate Multi-Quality DASH",
      category = "DASH Broadcast",
      streamUrl = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd",
      format = StreamFormat.DASH,
      isLive = false,
      channelNumber = "DASH 60",
      tournamentOrLeague = "Akamai Testnet",
      resolutionLabel = "1080p 60fps",
      description = "Akamai CDN MPEG-DASH adaptive stream testing buffer longevity, quick switching, and segment preloading."
    ),

    // VOD & Progressive MP4
    BroadcastStream(
      id = "oceans_highlight_mp4",
      title = "Oceans Cinematic Showcase",
      subtitle = "Deep Sea Documentary Highlights (MP4)",
      category = "Highlights & VOD",
      streamUrl = "https://vjs.zencdn.net/v/oceans.mp4",
      format = StreamFormat.PROGRESSIVE,
      isLive = false,
      channelNumber = "TOD REEL",
      tournamentOrLeague = "Studio Special",
      resolutionLabel = "1080p MP4",
      description = "Crystal clear progressive MP4 video testing instantaneous seeking, hardware decoder acceleration, and aspect ratio controls."
    ),

    BroadcastStream(
      id = "flower_nature_mp4",
      title = "Nature & Motion Showcase",
      subtitle = "High Bitrate Visual Showcase (MP4)",
      category = "Highlights & VOD",
      streamUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
      format = StreamFormat.PROGRESSIVE,
      isLive = false,
      channelNumber = "TOD THEATRE",
      tournamentOrLeague = "TOD Originals",
      resolutionLabel = "1080p Cinema",
      description = "Visual showcase testing audio boost, picture-in-picture mode, aspect ratio zooming, and speed variations."
    )
  )
}
