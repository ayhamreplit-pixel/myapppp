<!-- 7. TAB: VIDEO PLAYER -->
<div id="tab-player" class="tab-content">
    <div class="card">
        <h2 style="font-size: 18px; color: var(--gold); font-weight: 900; margin-bottom: 12px;">مشغل الفيديو المباشر (HLS & m3u8 Test Player)</h2>
        <div style="display: flex; gap: 10px; margin-bottom: 16px;">
            <input type="text" id="manual_stream_url" class="form-control" placeholder="أدخل رابط البث https://.../stream.m3u8" style="direction: ltr;">
            <button class="btn" onclick="playManualUrl()">تشغيل البث</button>
        </div>
        <div style="width: 100%; max-width: 900px; margin: 0 auto; background: #000; border-radius: 16px; overflow: hidden; aspect-ratio: 16/9; position: relative;">
            <video id="liveVideo" controls style="width: 100%; height: 100%;"></video>
        </div>
        <p id="player_info" style="margin-top: 10px; font-size: 13px; color: var(--gold); text-align: center;"></p>
    </div>
</div>
