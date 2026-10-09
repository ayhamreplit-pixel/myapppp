<!-- 7. TAB: VIDEO PLAYER -->
<div id="tab-player" class="tab-content">
    <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 16px;">
            <div>
                <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">مشغل الفيديو المباشر وفحص السيرفرات (HLS / m3u8 Test Engine)</h2>
                <p style="font-size: 12.5px; color: var(--text-muted);">فحص فوري لسيرفرات البث، التحقق من جودة الصوت والصورة، وتجربة الروابط المشفرة (enc:...) قبل نشرها للمستخدمين</p>
            </div>
            <div style="display: flex; gap: 8px;">
                <button class="btn btn-outline" onclick="playManualUrl('https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8', 'سيرفر تجريبي 1 (Mux HLS)')">سيرفر تجريبي 1</button>
                <button class="btn btn-outline" onclick="playManualUrl('https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8', 'سيرفر تجريبي 2 (Akamai Live)')">سيرفر تجريبي 2</button>
            </div>
        </div>

        <div style="display: flex; gap: 10px; margin-bottom: 18px;">
            <input type="text" id="manual_stream_url" class="form-control" placeholder="أدخل رابط البث https://.../stream.m3u8 أو الرابط المشفر enc:..." style="direction: ltr; font-family: monospace;">
            <button class="btn btn-blue" onclick="playManualUrl()">▶️ تشغيل واختبار البث</button>
        </div>

        <div style="width: 100%; max-width: 960px; margin: 0 auto; background: #000; border: 1px solid var(--card-border); border-radius: 16px; overflow: hidden; aspect-ratio: 16/9; position: relative; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
            <video id="liveVideo" controls style="width: 100%; height: 100%;"></video>
        </div>

        <div style="margin-top: 14px; display: flex; justify-content: center; align-items: center; gap: 10px;">
            <span class="badge" style="background: rgba(48,209,88,0.15); color: var(--green); border-color: rgba(48,209,88,0.3); font-size: 12px;" id="player_status_badge">جاهز للاختبار</span>
            <p id="player_info" style="font-size: 13px; color: var(--gold); font-weight: 700;"></p>
        </div>
    </div>
</div>
