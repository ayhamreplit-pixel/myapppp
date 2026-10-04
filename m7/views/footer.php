</div> <!-- /container -->

<!-- MODALS -->

<!-- 1. Server Modal -->
<div class="modal" id="serverModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;" id="serverModalTitle">إدارة سيرفرات البث</h3>
        <div id="existingServersList" style="margin-bottom: 16px;"></div>

        <form onsubmit="saveServerForm(event)">
            <input type="hidden" id="srv_match_id">
            <div class="form-group">
                <label>اسم السيرفر</label>
                <input type="text" id="srv_name" class="form-control" value="سيرفر 1 (FHD)" required>
            </div>
            <div class="form-group">
                <label>رابط البث المباشر (m3u8 أو mp4 أو enc:...)</label>
                <input type="text" id="srv_url" class="form-control" placeholder="https://domain.com/live/stream.m3u8" style="direction: ltr;" required>
            </div>
            <div class="form-group">
                <label>الجودة</label>
                <select id="srv_quality" class="form-control">
                    <option value="4K UHD">4K UHD</option>
                    <option value="1080p FHD" selected>1080p FHD</option>
                    <option value="720p HD">720p HD</option>
                    <option value="SD Low">SD Low</option>
                </select>
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 إضافة وحفظ السيرفر</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('serverModal')">إغلاق</button>
            </div>
        </form>
    </div>
</div>

<!-- 2. Edit Match Modal -->
<div class="modal" id="editMatchModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;">تعديل بيانات ونتيجة المباراة</h3>
        <form onsubmit="saveEditMatchForm(event)">
            <input type="hidden" id="edit_match_id">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>أهداف الفريق المضيف</label>
                    <input type="number" id="edit_home_scores" class="form-control">
                </div>
                <div class="form-group">
                    <label>أهداف الفريق الضيف</label>
                    <input type="number" id="edit_away_scores" class="form-control">
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>حالة المباراة</label>
                    <select id="edit_status" class="form-control">
                        <option value="1">لم تبدأ (قادمة)</option>
                        <option value="2">جارية الآن (مباشر)</option>
                        <option value="3">استراحة بين الشوطين</option>
                        <option value="4">انتهت</option>
                        <option value="5">مؤجلة</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>دقيقة المباراة</label>
                    <input type="text" id="edit_score_time" class="form-control" placeholder="'65">
                </div>
            </div>
            <div class="form-group">
                <label>القناة الناقلة</label>
                <input type="text" id="edit_channel_name" class="form-control">
            </div>
            <div class="form-group">
                <label>المعلق الرياضي</label>
                <input type="text" id="edit_commentator" class="form-control">
            </div>
            <div class="form-group">
                <label>الملعب</label>
                <input type="text" id="edit_stadium" class="form-control">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ التعديلات</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('editMatchModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- 3. Add Custom Match Modal -->
<div class="modal" id="customMatchModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;">➕ إضافة مباراة جديدة مخصصة</h3>
        <form onsubmit="saveCustomMatchForm(event)">
            <div class="form-group">
                <label>اسم البطولة / الدوري</label>
                <input type="text" id="cm_championship" class="form-control" value="دوري أبطال أوروبا" required>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>الفريق الأول (المضيف)</label>
                    <input type="text" id="cm_home_team" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>شعار الفريق الأول (رابط أو ارفع)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="cm_home_image" class="form-control" placeholder="https://...">
                        <label class="btn btn-outline" style="padding: 6px 10px; cursor: pointer; white-space: nowrap;">
                            📁
                            <input type="file" accept="image/*" style="display:none;" onchange="uploadImageFile(this, 'cm_home_image')">
                        </label>
                    </div>
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>الفريق الثاني (الضيف)</label>
                    <input type="text" id="cm_away_team" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>شعار الفريق الثاني (رابط أو ارفع)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="cm_away_image" class="form-control" placeholder="https://...">
                        <label class="btn btn-outline" style="padding: 6px 10px; cursor: pointer; white-space: nowrap;">
                            📁
                            <input type="file" accept="image/*" style="display:none;" onchange="uploadImageFile(this, 'cm_away_image')">
                        </label>
                    </div>
                </div>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>تاريخ المباراة</label>
                    <input type="date" id="cm_date" class="form-control" value="<?= $targetDate ?>" required>
                </div>
                <div class="form-group">
                    <label>وقت المباراة</label>
                    <input type="time" id="cm_time" class="form-control" value="22:00" required>
                </div>
            </div>
            <div class="form-group">
                <label>رابط البث المباشر (اختياري)</label>
                <input type="text" id="cm_stream_url" class="form-control" placeholder="https://.../stream.m3u8" style="direction: ltr;">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 إضافة المباراة</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('customMatchModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- 4. Add Channel Modal -->
<div class="modal" id="channelModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;">إضافة / تعديل قناة تلفزيونية</h3>
        <form onsubmit="saveChannelForm(event)">
            <input type="hidden" id="ch_stream_id">
            <div class="form-group">
                <label>اسم القناة</label>
                <input type="text" id="ch_name" class="form-control" placeholder="beIN SPORTS 1 HD" required>
            </div>
            <div class="form-group">
                <label>رابط البث (m3u8 أو enc:...)</label>
                <input type="text" id="ch_play_url" class="form-control" placeholder="https://.../index.m3u8" style="direction: ltr;" required>
            </div>
            <div class="form-group">
                <label>شعار القناة (أدخل رابط أو ارفع من جهازك مباشرة)</label>
                <div style="display: flex; gap: 8px;">
                    <input type="text" id="ch_icon_url" class="form-control" placeholder="https://... أو اختر ملف من جهازك">
                    <label class="btn btn-blue" style="margin: 0; white-space: nowrap; cursor: pointer;">
                        📁 ارفع من جهازك
                        <input type="file" accept="image/*" style="display: none;" onchange="uploadImageFile(this, 'ch_icon_url')">
                    </label>
                </div>
            </div>
            <div class="form-group">
                <label>تصنيف القناة</label>
                <input type="text" id="ch_category_id" class="form-control" placeholder="مثال: قنوات beIN SPORTS أو SSC أو رياضة عربية" value="قنوات beIN SPORTS">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ القناة</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('channelModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- 5. Add Hero Banner Modal -->
<div class="modal" id="heroModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;">إضافة / تعديل بوستر الهيرو والقمة</h3>
        <form onsubmit="saveHeroForm(event)">
            <input type="hidden" id="hero_id">
            <div class="form-group">
                <label>عنوان البوستر الرئيسي</label>
                <input type="text" id="hero_title" class="form-control" placeholder="ريال مدريد ضد مانشستر سيتي" required>
            </div>
            <div class="form-group">
                <label>اسم البطولة</label>
                <input type="text" id="hero_tournament" class="form-control" value="دوري أبطال أوروبا">
            </div>
            <div class="form-group">
                <label>رابط صورة البوستر العريضة (أو ارفع من جهازك)</label>
                <div style="display: flex; gap: 8px;">
                    <input type="text" id="hero_banner_url" class="form-control" placeholder="https://..." required>
                    <label class="btn btn-blue" style="margin: 0; white-space: nowrap; cursor: pointer;">
                        📁 ارفع صورة
                        <input type="file" accept="image/*" style="display: none;" onchange="uploadImageFile(this, 'hero_banner_url')">
                    </label>
                </div>
            </div>
            <div class="form-group">
                <label>رابط بث مباشر للبوستر (m3u8 أو enc:...)</label>
                <input type="text" id="hero_stream_url" class="form-control" placeholder="https://..." style="direction: ltr;">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ البوستر</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('heroModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- 6. Add Featured Match Modal -->
<div class="modal" id="featuredModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;">إضافة / تعديل مباراة الأسبوع المميزة</h3>
        <form onsubmit="saveFeaturedForm(event)">
            <input type="hidden" id="feat_id">
            <div class="form-group">
                <label>عنوان القمة</label>
                <input type="text" id="feat_title" class="form-control" placeholder="ليفربول ضد مانشستر يونايتد" required>
            </div>
            <div class="form-group">
                <label>البطولة</label>
                <input type="text" id="feat_tournament" class="form-control" value="الدوري الإنجليزي الممتاز">
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>يوم المباراة</label>
                    <input type="text" id="feat_date" class="form-control" value="السبت">
                </div>
                <div class="form-group">
                    <label>توقيت المباراة</label>
                    <input type="text" id="feat_time" class="form-control" value="22:00">
                </div>
            </div>
            <div class="form-group">
                <label>رابط البث المباشر (m3u8)</label>
                <input type="text" id="feat_stream_url" class="form-control" placeholder="https://..." style="direction: ltr;">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ المباراة المميزة</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('featuredModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- 7. Add Slider Modal -->
<div class="modal" id="sliderModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;">إضافة بوستر لسلايدر التطبيق</h3>
        <form onsubmit="saveSliderForm(event)">
            <div class="form-group">
                <label>عنوان البوستر</label>
                <input type="text" id="sl_title" class="form-control" placeholder="قمة الدوري الإنجليزي" required>
            </div>
            <div class="form-group">
                <label>صورة البوستر (رابط أو ارفع من جهازك)</label>
                <div style="display: flex; gap: 8px;">
                    <input type="text" id="sl_image" class="form-control" placeholder="https://..." required>
                    <label class="btn btn-blue" style="margin: 0; white-space: nowrap; cursor: pointer;">
                        📁 ارفع
                        <input type="file" accept="image/*" style="display: none;" onchange="uploadImageFile(this, 'sl_image')">
                    </label>
                </div>
            </div>
            <div class="form-group">
                <label>الرابط التوجيهي (اختياري)</label>
                <input type="text" id="sl_link" class="form-control">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ البوستر</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('sliderModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- JAVASCRIPT LOGIC -->
<script>
let hlsInstance = null;
const API_URL = 'api.php';

function openModal(id) { document.getElementById(id).classList.add('show'); }
function closeModal(id) { document.getElementById(id).classList.remove('show'); }

function switchMainTab(tabKey, btn) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.main-tab-btn').forEach(el => el.classList.remove('active'));
    const target = document.getElementById('tab-' + tabKey);
    if (target) target.classList.add('active');
    if (btn) {
        btn.classList.add('active');
    } else {
        const activeBtn = document.querySelector(`.main-tab-btn[onclick*="'${tabKey}'"]`);
        if (activeBtn) activeBtn.classList.add('active');
    }
}

function filterMatchesTable() {
    const q = document.getElementById('matchSearchInput').value.toLowerCase();
    document.querySelectorAll('.match-row').forEach(row => {
        const text = row.getAttribute('data-search') ? row.getAttribute('data-search').toLowerCase() : '';
        row.style.display = text.includes(q) ? 'grid' : 'none';
    });
}

// 📁 Image File Upload Handler
async function uploadImageFile(fileInput, targetInputId) {
    if (!fileInput.files || !fileInput.files[0]) return;
    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('image', file);
    formData.append('action', 'upload');

    const targetInput = document.getElementById(targetInputId);
    if (targetInput) targetInput.placeholder = 'جاري رفع الصورة إلى السيرفر... ⏳';

    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            body: formData
        });
        const data = await res.json();
        if (data.status && data.url) {
            if (targetInput) targetInput.value = data.url;
            alert(data.message);
        } else {
            alert(data.message || 'فشل رفع الصورة');
        }
    } catch (e) {
        alert('حدث خطأ أثناء الاتصال بالسيرفر لرفع الصورة');
    }
}

// 👥 Live Viewers & Devices Management
async function loadLiveSessions() {
    try {
        const res = await fetch(API_URL + '?action=sessions');
        const data = await res.json();
        if (data.status) {
            const badge = document.getElementById('liveViewersCountBadge');
            if (badge) badge.innerText = `🟢 ${data.count} جهاز متصل حالياً`;
            const headerBtn = document.querySelector(`.main-tab-btn[onclick*="'devices'"]`);
            if (headerBtn) headerBtn.innerText = `👥 المتصلون والأجهزة (${data.count})`;

            const grid = document.getElementById('liveViewersGrid');
            if (grid) {
                if (data.sessions.length === 0) {
                    grid.innerHTML = '<div style="grid-column: 1 / -1; padding: 30px; text-align: center; color: var(--text-muted); background: rgba(0,0,0,0.2); border-radius: 10px;">لا يوجد مستخدمون يشاهدون حالياً (يتم رصد المتصلين في آخر 60 ثانية تلقائياً)</div>';
                } else {
                    let html = '';
                    data.sessions.forEach(s => {
                        html += `<div style="background: rgba(0,0,0,0.4); border: 1px solid rgba(48,209,88,0.2); border-radius: 12px; padding: 14px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                                <span style="font-weight: 800; font-size: 14px; color: #fff;">📱 ${s.device_name || 'Android'}</span>
                                <span class="badge" style="background: rgba(10,132,255,0.15); color: var(--blue); font-size: 10px;">${s.profile_name || 'User'}</span>
                            </div>
                            <div style="font-size: 12px; color: var(--gold); margin-bottom: 8px; font-weight: 700;">
                                ▶️ يشاهد الآن: ${s.active_stream || 'التطبيق'}
                            </div>
                            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-muted);">
                                <span>IP: ${s.ip || '127.0.0.1'}</span>
                                <span>آخر نبضة: ${s.last_ping_formatted || ''}</span>
                            </div>
                        </div>`;
                    });
                    grid.innerHTML = html;
                }
            }
        }
    } catch (e) {}
}
setInterval(loadLiveSessions, 5000);

async function toggleDeviceBan(deviceId, ban) {
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'toggle_device_ban', device_id: deviceId, ban: ban })
    });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function approveDevice(deviceId) {
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'approve_device', device_id: deviceId })
    });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteDevice(deviceId) {
    if (!confirm('هل تريد حذف هذا الجهاز من السجل؟')) return;
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'delete_device', device_id: deviceId })
    });
    location.reload();
}

async function saveAccessPolicy(policy) {
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'save_access_policy', access_policy: policy })
    });
    const data = await res.json();
    alert(data.message);
}

async function toggleMatchStream(matchId, enable) {
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'toggle_stream', match_id: matchId, stream_active: enable })
    });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

function openServersModal(matchId, title, servers) {
    document.getElementById('srv_match_id').value = matchId;
    document.getElementById('serverModalTitle').innerText = 'سيرفرات بث: ' + title;
    const container = document.getElementById('existingServersList');
    container.innerHTML = '';
    if (servers && servers.length > 0) {
        let html = '<div style="background:rgba(0,0,0,0.3); padding:10px; border-radius:8px; margin-bottom:12px;"><h4 style="font-size:13px; color:var(--gold); margin-bottom:8px;">السيرفرات المضافة حالياً:</h4>';
        servers.forEach(s => {
            html += `<div style="display:flex; justify-content:space-between; align-items:center; padding:6px 0; border-bottom:1px solid rgba(255,255,255,0.05);">
                <div><b>${s.name}</b> <span class="badge">${s.quality || 'FHD'}</span></div>
                <div style="display:flex; gap:6px;">
                    <button type="button" class="btn btn-outline" style="padding:3px 8px; font-size:11px;" onclick="playInTestPlayer('${s.url}', '${s.name}')">▶️ تجربة</button>
                    <button type="button" class="btn btn-red" style="padding:3px 8px; font-size:11px;" onclick="deleteServer('${matchId}', '${s.id}')">🗑️ حذف</button>
                </div>
            </div>`;
        });
        html += '</div>';
        container.innerHTML = html;
    }
    openModal('serverModal');
}

async function saveServerForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_server',
        match_id: document.getElementById('srv_match_id').value,
        server_name: document.getElementById('srv_name').value,
        stream_url: document.getElementById('srv_url').value,
        quality: document.getElementById('srv_quality').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteServer(matchId, srvId) {
    if (!confirm('هل أنت متأكد من حذف هذا السيرفر؟')) return;
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_server', match_id: matchId, server_id: srvId }) });
    const data = await res.json();
    if (data.status) location.reload();
}

function openEditMatchModal(matchId, matchData) {
    document.getElementById('edit_match_id').value = matchId;
    document.getElementById('edit_home_scores').value = matchData.home_scores !== undefined ? matchData.home_scores : '';
    document.getElementById('edit_away_scores').value = matchData.away_scores !== undefined ? matchData.away_scores : '';
    document.getElementById('edit_status').value = matchData.status !== undefined ? matchData.status : '1';
    document.getElementById('edit_score_time').value = matchData.score_time || '';
    document.getElementById('edit_channel_name').value = matchData.channel_name || '';
    document.getElementById('edit_commentator').value = matchData.commentator || '';
    document.getElementById('edit_stadium').value = matchData.stadium || '';
    openModal('editMatchModal');
}

async function saveEditMatchForm(e) {
    e.preventDefault();
    const payload = {
        action: 'edit_match',
        match_id: document.getElementById('edit_match_id').value,
        home_scores: document.getElementById('edit_home_scores').value,
        away_scores: document.getElementById('edit_away_scores').value,
        status: document.getElementById('edit_status').value,
        score_time: document.getElementById('edit_score_time').value,
        channel_name: document.getElementById('edit_channel_name').value,
        commentator: document.getElementById('edit_commentator').value,
        stadium: document.getElementById('edit_stadium').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

function openCustomMatchModal() { openModal('customMatchModal'); }
async function saveCustomMatchForm(e) {
    e.preventDefault();
    const payload = {
        action: 'add_custom_match',
        championship: document.getElementById('cm_championship').value,
        home_team: document.getElementById('cm_home_team').value,
        home_team_image: document.getElementById('cm_home_image').value,
        away_team: document.getElementById('cm_away_team').value,
        away_team_image: document.getElementById('cm_away_image').value,
        match_date: document.getElementById('cm_date').value,
        match_time: document.getElementById('cm_time').value,
        stream_url: document.getElementById('cm_stream_url').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

function openAddChannelModal() {
    document.getElementById('ch_stream_id').value = '';
    document.getElementById('ch_name').value = '';
    document.getElementById('ch_play_url').value = '';
    document.getElementById('ch_icon_url').value = '';
    openModal('channelModal');
}

async function saveChannelForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_channel',
        streamId: document.getElementById('ch_stream_id').value,
        name: document.getElementById('ch_name').value,
        playUrl: document.getElementById('ch_play_url').value,
        iconUrl: document.getElementById('ch_icon_url').value,
        categoryId: document.getElementById('ch_category_id').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteChannel(streamId) {
    if (!confirm('هل تريد حذف هذه القناة نهائياً؟')) return;
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_channel', streamId: streamId }) });
    location.reload();
}

function openHeroModal(data = null) {
    if (data) {
        document.getElementById('hero_id').value = data.id || '';
        document.getElementById('hero_title').value = data.title || '';
        document.getElementById('hero_tournament').value = data.tournament || '';
        document.getElementById('hero_banner_url').value = data.bannerUrl || '';
        document.getElementById('hero_stream_url').value = (data.servers && data.servers[0]) ? data.servers[0].url : (data.streamUrl || '');
    } else {
        document.getElementById('hero_id').value = 'hero_' + Date.now();
        document.getElementById('hero_title').value = '';
        document.getElementById('hero_tournament').value = 'دوري أبطال أوروبا';
        document.getElementById('hero_banner_url').value = '';
        document.getElementById('hero_stream_url').value = '';
    }
    openModal('heroModal');
}

async function saveHeroForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_hero_banner',
        id: document.getElementById('hero_id').value,
        title: document.getElementById('hero_title').value,
        tournament: document.getElementById('hero_tournament').value,
        bannerUrl: document.getElementById('hero_banner_url').value,
        streamUrl: document.getElementById('hero_stream_url').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteHeroBanner(id) {
    if (!confirm('حذف هذا البوستر نهائياً؟')) return;
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_hero_banner', id: id }) });
    location.reload();
}

function openFeaturedModal(data = null) {
    if (data) {
        document.getElementById('feat_id').value = data.id || '';
        document.getElementById('feat_title').value = data.title || '';
        document.getElementById('feat_tournament').value = data.tournament || '';
        document.getElementById('feat_date').value = data.kickoffDate || 'السبت';
        document.getElementById('feat_time').value = data.kickoffTime || '22:00';
        document.getElementById('feat_stream_url').value = (data.servers && data.servers[0]) ? data.servers[0].url : (data.streamUrl || '');
    } else {
        document.getElementById('feat_id').value = 'feat_' + Date.now();
        document.getElementById('feat_title').value = '';
        document.getElementById('feat_tournament').value = 'الدوري الإنجليزي الممتاز';
        document.getElementById('feat_date').value = 'السبت';
        document.getElementById('feat_time').value = '22:00';
        document.getElementById('feat_stream_url').value = '';
    }
    openModal('featuredModal');
}

async function saveFeaturedForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_featured_weekly',
        id: document.getElementById('feat_id').value,
        title: document.getElementById('feat_title').value,
        tournament: document.getElementById('feat_tournament').value,
        kickoffDate: document.getElementById('feat_date').value,
        kickoffTime: document.getElementById('feat_time').value,
        streamUrl: document.getElementById('feat_stream_url').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteFeaturedMatch(id) {
    if (!confirm('حذف هذه المباراة من مباريات الأسبوع؟')) return;
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_featured_weekly', id: id }) });
    location.reload();
}

function openAddSliderModal() { openModal('sliderModal'); }
async function saveSliderForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_slider',
        title: document.getElementById('sl_title').value,
        image: document.getElementById('sl_image').value,
        link: document.getElementById('sl_link').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteSlider(id) {
    if (!confirm('حذف هذا البوستر؟')) return;
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_slider', id: id }) });
    location.reload();
}

async function saveAnnouncement(e) {
    e.preventDefault();
    const payload = {
        action: 'save_announcement',
        title: document.getElementById('notif_title').value,
        message: document.getElementById('notif_message').value,
        target_type: document.getElementById('notif_target_type').value,
        target_id: document.getElementById('notif_target_id').value
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
}

async function clearAnnouncement() {
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'clear_announcement' }) });
    const data = await res.json();
    alert(data.message);
    location.reload();
}

async function saveSettingsForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_settings',
        master_key: document.getElementById('cfg_master_key').value.trim(),
        encrypt_streams: document.getElementById('cfg_encrypt_streams').value === '1'
    };
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function testEncryptUrl() {
    const rawUrl = document.getElementById('test_plain_input').value.trim();
    if (!rawUrl) { alert('يرجى إدخال رابط'); return; }
    const key = document.getElementById('cfg_master_key').value.trim();
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'test_encrypt', url: rawUrl, secret_key: key }) });
    const data = await res.json();
    if (data.status) {
        document.getElementById('test_encrypt_result').innerHTML = `<b>المشفر:</b><br>${data.encrypted}<br><button class="btn btn-outline" style="margin-top:6px; padding:3px 8px; font-size:11px;" onclick="navigator.clipboard.writeText('${data.encrypted}'); alert('تم النسخ');">📋 نسخ</button>`;
        document.getElementById('test_cipher_input').value = data.encrypted;
    }
}

async function testDecryptCipher() {
    const cipher = document.getElementById('test_cipher_input').value.trim();
    if (!cipher) { alert('يرجى إدخال نص مشفر enc:...'); return; }
    const key = document.getElementById('cfg_master_key').value.trim();
    const res = await fetch(API_URL, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'test_decrypt', cipher: cipher, secret_key: key }) });
    const data = await res.json();
    if (data.status) {
        document.getElementById('test_decrypt_result').innerHTML = `<b>الرابط الأصلي:</b><br>${data.decrypted}<br><button class="btn btn-blue" style="margin-top:6px; padding:3px 8px; font-size:11px;" onclick="playInTestPlayer('${data.decrypted}', 'تجربة المفكوك')">▶️ تشغيل</button>`;
    }
}

function playInTestPlayer(url, title) {
    switchMainTab('player');
    document.getElementById('manual_stream_url').value = url;
    document.getElementById('player_info').innerText = 'تشغيل: ' + (title || url);
    const video = document.getElementById('liveVideo');
    if (Hls.isSupported() && url.includes('.m3u8')) {
        if (hlsInstance) hlsInstance.destroy();
        hlsInstance = new Hls();
        hlsInstance.loadSource(url);
        hlsInstance.attachMedia(video);
        hlsInstance.on(Hls.Events.MANIFEST_PARSED, function () { video.play(); });
    } else {
        video.src = url;
        video.play();
    }
}

function playManualUrl() {
    const url = document.getElementById('manual_stream_url').value.trim();
    if (url) playInTestPlayer(url, 'بث يدوي');
}
</script>

</body>
</html>
