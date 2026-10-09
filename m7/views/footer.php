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
    <div class="modal-content" style="max-width: 650px;">
        <h3 style="color: var(--gold); margin-bottom: 12px;">تعديل بيانات ونتيجة وشعارات المباراة</h3>
        <form onsubmit="saveEditMatchForm(event)">
            <input type="hidden" id="edit_match_id">

            <div class="form-group">
                <label>اسم البطولة / الدوري</label>
                <input type="text" id="edit_championship" class="form-control">
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>الفريق الأول (المضيف)</label>
                    <input type="text" id="edit_home_team" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>شعار الفريق الأول (رابط أو اختر من المكتبة)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="edit_home_image" class="form-control" placeholder="https://...">
                        <button type="button" class="btn btn-outline" style="padding: 6px 10px; white-space: nowrap;" title="اختر من مكتبة الشعارات" onclick="openLogoPickerFor('edit_home_image', 'edit_home_team')">🛡️ اختر</button>
                    </div>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>الفريق الثاني (الضيف)</label>
                    <input type="text" id="edit_away_team" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>شعار الفريق الثاني (رابط أو اختر من المكتبة)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="edit_away_image" class="form-control" placeholder="https://...">
                        <button type="button" class="btn btn-outline" style="padding: 6px 10px; white-space: nowrap;" title="اختر من مكتبة الشعارات" onclick="openLogoPickerFor('edit_away_image', 'edit_away_team')">🛡️ اختر</button>
                    </div>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>تاريخ المباراة</label>
                    <input type="date" id="edit_match_date" class="form-control">
                </div>
                <div class="form-group">
                    <label>وقت المباراة</label>
                    <input type="time" id="edit_match_time" class="form-control">
                </div>
            </div>

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
                    <label>شعار الفريق الأول (رابط أو ارفع أو اختر من المكتبة)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="cm_home_image" class="form-control" placeholder="https://...">
                        <button type="button" class="btn btn-outline" style="padding: 6px 10px; white-space: nowrap;" title="اختر من مكتبة الشعارات" onclick="openLogoPickerFor('cm_home_image', 'cm_home_team')">🛡️ اختر</button>
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
                    <label>شعار الفريق الثاني (رابط أو ارفع أو اختر من المكتبة)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="cm_away_image" class="form-control" placeholder="https://...">
                        <button type="button" class="btn btn-outline" style="padding: 6px 10px; white-space: nowrap;" title="اختر من مكتبة الشعارات" onclick="openLogoPickerFor('cm_away_image', 'cm_away_team')">🛡️ اختر</button>
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
                <label>شعار القناة (أدخل رابط أو ارفع من جهازك أو اختر من المكتبة)</label>
                <div style="display: flex; gap: 8px;">
                    <input type="text" id="ch_icon_url" class="form-control" placeholder="https://... أو اختر من المكتبة">
                    <button type="button" class="btn btn-outline" style="white-space: nowrap;" onclick="openLogoPickerFor('ch_icon_url', 'ch_name')">🛡️ اختر شعار</button>
                    <label class="btn btn-blue" style="margin: 0; white-space: nowrap; cursor: pointer;">
                        📁 ارفع
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
    <div class="modal-content" style="max-width: 680px;">
        <h3 style="color: var(--gold); margin-bottom: 12px;">إضافة / تعديل بوستر الهيرو والقمة مع الشعارات</h3>
        <form onsubmit="saveHeroForm(event)">
            <input type="hidden" id="hero_id">
            <div class="form-group">
                <label>عنوان البوستر الرئيسي</label>
                <input type="text" id="hero_title" class="form-control" placeholder="ريال مدريد ضد مانشستر سيتي" required>
            </div>
            <div class="form-group">
                <label>اسم البطولة / الدوري</label>
                <input type="text" id="hero_tournament" class="form-control" value="دوري أبطال أوروبا">
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>الفريق الأول (المضيف)</label>
                    <input type="text" id="hero_home_team" class="form-control" placeholder="ريال مدريد">
                </div>
                <div class="form-group">
                    <label>شعار الفريق الأول (رابط أو من المكتبة)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="hero_home_logo" class="form-control" placeholder="https://...">
                        <button type="button" class="btn btn-outline" style="padding: 6px 10px; white-space: nowrap;" title="اختر من مكتبة الشعارات" onclick="openLogoPickerFor('hero_home_logo', 'hero_home_team')">🛡️ اختر</button>
                    </div>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                    <label>الفريق الثاني (الضيف)</label>
                    <input type="text" id="hero_away_team" class="form-control" placeholder="مانشستر سيتي">
                </div>
                <div class="form-group">
                    <label>شعار الفريق الثاني (رابط أو من المكتبة)</label>
                    <div style="display: flex; gap: 6px;">
                        <input type="url" id="hero_away_logo" class="form-control" placeholder="https://...">
                        <button type="button" class="btn btn-outline" style="padding: 6px 10px; white-space: nowrap;" title="اختر من مكتبة الشعارات" onclick="openLogoPickerFor('hero_away_logo', 'hero_away_team')">🛡️ اختر</button>
                    </div>
                </div>
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
    document.getElementById('edit_championship').value = (matchData.championship && matchData.championship.title) ? matchData.championship.title : (matchData.championship || '');
    document.getElementById('edit_home_team').value = (matchData.home_team && matchData.home_team.title) ? matchData.home_team.title : (matchData.home_team || '');
    document.getElementById('edit_home_image').value = (matchData.home_team && matchData.home_team.image) ? matchData.home_team.image : '';
    document.getElementById('edit_away_team').value = (matchData.away_team && matchData.away_team.title) ? matchData.away_team.title : (matchData.away_team || '');
    document.getElementById('edit_away_image').value = (matchData.away_team && matchData.away_team.image) ? matchData.away_team.image : '';
    document.getElementById('edit_match_date').value = matchData.match_date || '';
    document.getElementById('edit_match_time').value = matchData.match_time ? matchData.match_time.substring(0, 5) : '';
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
        championship: document.getElementById('edit_championship').value,
        home_team: document.getElementById('edit_home_team').value,
        home_team_image: document.getElementById('edit_home_image').value,
        away_team: document.getElementById('edit_away_team').value,
        away_team_image: document.getElementById('edit_away_image').value,
        match_date: document.getElementById('edit_match_date').value,
        match_time: document.getElementById('edit_match_time').value,
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

async function quickScorePrompt(matchId, currentH, currentA) {
    const h = prompt('أهداف الفريق المضيف:', currentH === '-' ? '0' : currentH);
    if (h === null) return;
    const a = prompt('أهداف الفريق الضيف:', currentA === '-' ? '0' : currentA);
    if (a === null) return;
    const min = prompt('دقيقة المباراة الحالية (مثال: 55 أو الشوط الأول):', 'مباشر');
    if (min === null) return;

    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            action: 'quick_score',
            match_id: matchId,
            home_scores: parseInt(h) || 0,
            away_scores: parseInt(a) || 0,
            status: 2,
            score_time: min
        })
    });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function cloneMatch(matchId) {
    if (!confirm('هل تريد نسخ هذه المباراة لإنشاء مباراة جديدة مشابهة؟')) return;
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'clone_match', match_id: matchId })
    });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

async function deleteMatch(matchId) {
    if (!confirm('هل أنت متأكد من حذف هذه المباراة نهائياً من السيرفر؟')) return;
    const res = await fetch(API_URL, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'delete_match', match_id: matchId })
    });
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
    document.getElementById('ch_category_id').value = 'قنوات beIN SPORTS';
    openModal('channelModal');
}

function openEditChannelModal(ch) {
    document.getElementById('ch_stream_id').value = ch.streamId || '';
    document.getElementById('ch_name').value = ch.name || '';
    document.getElementById('ch_play_url').value = ch.playUrl || '';
    document.getElementById('ch_icon_url').value = ch.iconUrl || '';
    document.getElementById('ch_category_id').value = ch.categoryId || 'قنوات beIN SPORTS';
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
        document.getElementById('hero_home_team').value = (data.homeTeam && typeof data.homeTeam === 'object') ? (data.homeTeam.name || '') : (data.homeTeam || '');
        document.getElementById('hero_home_logo').value = (data.homeTeam && typeof data.homeTeam === 'object') ? (data.homeTeam.logoUrl || '') : (data.homeLogo || '');
        document.getElementById('hero_away_team').value = (data.awayTeam && typeof data.awayTeam === 'object') ? (data.awayTeam.name || '') : (data.awayTeam || '');
        document.getElementById('hero_away_logo').value = (data.awayTeam && typeof data.awayTeam === 'object') ? (data.awayTeam.logoUrl || '') : (data.awayLogo || '');
        document.getElementById('hero_banner_url').value = data.bannerUrl || '';
        document.getElementById('hero_stream_url').value = (data.servers && data.servers[0]) ? data.servers[0].url : (data.streamUrl || '');
    } else {
        document.getElementById('hero_id').value = 'hero_' + Date.now();
        document.getElementById('hero_title').value = '';
        document.getElementById('hero_tournament').value = 'دوري أبطال أوروبا';
        document.getElementById('hero_home_team').value = '';
        document.getElementById('hero_home_logo').value = '';
        document.getElementById('hero_away_team').value = '';
        document.getElementById('hero_away_logo').value = '';
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
        homeTeam: document.getElementById('hero_home_team').value,
        homeLogo: document.getElementById('hero_home_logo').value,
        awayTeam: document.getElementById('hero_away_team').value,
        awayLogo: document.getElementById('hero_away_logo').value,
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

async function playInTestPlayer(url, title) {
    switchMainTab('player');
    const badge = document.getElementById('player_status_badge');
    if (badge) {
        badge.innerText = 'جاري التحميل...';
        badge.style.color = 'var(--gold)';
    }

    // Auto decrypt if encrypted
    let playUrl = url;
    if (url.startsWith('enc:') || url.startsWith('aes:') || url.startsWith('m7:')) {
        try {
            const masterKey = document.getElementById('cfg_master_key') ? document.getElementById('cfg_master_key').value.trim() : 'DEFAULT_MASTER_KEY';
            const res = await fetch(API_URL, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ action: 'test_decrypt', cipher: url, secret_key: masterKey })
            });
            const d = await res.json();
            if (d.status && d.decrypted) {
                playUrl = d.decrypted;
            }
        } catch (e) {
            console.error('Decryption error:', e);
        }
    }

    document.getElementById('manual_stream_url').value = playUrl;
    document.getElementById('player_info').innerText = 'تشغيل: ' + (title || playUrl);
    const video = document.getElementById('liveVideo');
    if (Hls.isSupported() && playUrl.includes('.m3u8')) {
        if (hlsInstance) hlsInstance.destroy();
        hlsInstance = new Hls();
        hlsInstance.loadSource(playUrl);
        hlsInstance.attachMedia(video);
        hlsInstance.on(Hls.Events.MANIFEST_PARSED, function () {
            video.play();
            if (badge) { badge.innerText = '🟢 البث يعمل بنجاح'; badge.style.color = 'var(--green)'; }
        });
        hlsInstance.on(Hls.Events.ERROR, function(e, data) {
            if (badge) { badge.innerText = '⚠️ تعذر تحميل البث'; badge.style.color = 'var(--red)'; }
        });
    } else {
        video.src = playUrl;
        video.play().then(() => {
            if (badge) { badge.innerText = '🟢 البث يعمل بنجاح'; badge.style.color = 'var(--green)'; }
        }).catch(() => {
            if (badge) { badge.innerText = '⚠️ انقر لتشغيل الفيديو'; badge.style.color = 'var(--gold)'; }
        });
    }
}

function playManualUrl(customUrl, label) {
    const inputUrl = customUrl || document.getElementById('manual_stream_url').value.trim();
    if (inputUrl) {
        if (!customUrl) document.getElementById('manual_stream_url').value = inputUrl;
        playInTestPlayer(inputUrl, label || 'بث يدوي');
    }
}

// -------------------------------------------------------------
// FOOTBALL API EXPLORER JAVASCRIPT FUNCTIONS
// -------------------------------------------------------------
async function loadTodayApiMatches() {
    const container = document.getElementById('apiMatchesList');
    container.innerHTML = '<div style="padding:20px;"><div style="display:inline-block; width:24px; height:24px; border:3px solid var(--gold); border-radius:50%; border-top-color:transparent; animation:spin 1s linear infinite;"></div><p style="margin-top:10px;">جاري جلب جدول مباريات اليوم من الـ API العالمي...</p></div>';
    try {
        const res = await fetch(API_URL + '?action=api_matches');
        const data = await res.json();
        renderApiMatches(data.events || []);
    } catch (e) {
        container.innerHTML = '<p style="color:var(--red);">حدث خطأ أثناء الاتصال بالـ API: ' + e.message + '</p>';
    }
}

async function searchApiMatches() {
    const q = document.getElementById('apiSearchQuery').value.trim();
    const container = document.getElementById('apiMatchesList');
    container.innerHTML = '<div style="padding:20px;"><div style="display:inline-block; width:24px; height:24px; border:3px solid var(--blue); border-radius:50%; border-top-color:transparent; animation:spin 1s linear infinite;"></div><p style="margin-top:10px;">جاري البحث عن: ' + (q || 'جميع المباريات') + '...</p></div>';
    try {
        const res = await fetch(API_URL + '?action=api_matches&q=' + encodeURIComponent(q));
        const data = await res.json();
        renderApiMatches(data.events || []);
    } catch (e) {
        container.innerHTML = '<p style="color:var(--red);">فشل البحث: ' + e.message + '</p>';
    }
}

function renderApiMatches(events) {
    const container = document.getElementById('apiMatchesList');
    document.getElementById('apiResultsCount').innerText = 'المباريات المتاحة في الـ API: ' + events.length;
    if (!events || events.length === 0) {
        container.innerHTML = '<div style="padding:30px;"><p>لم يتم العثور على أي مباريات مطابقة للبحث في الـ API.</p></div>';
        return;
    }

    let html = '';
    events.forEach(ev => {
        const hTeam = ev.home_team.name || 'فريق مضيف';
        const aTeam = ev.away_team.name || 'فريق ضيف';
        const hLogo = ev.home_team.logo || '';
        const aLogo = ev.away_team.logo || '';
        const league = ev.league || 'دوري';
        const time = ev.time || '20:00';
        const hScore = ev.home_score !== null ? ev.home_score : '-';
        const aScore = ev.away_score !== null ? ev.away_score : '-';
        const isLive = ev.is_live;

        html += `<div class="match-row" style="background: rgba(255,255,255,0.015); border-radius: 8px; margin: 4px 8px;">
            <div>
                <span style="font-size: 11px; color: var(--text-muted); display: block;">${league}</span>
                <span style="font-weight: 700; font-size: 13px; color: var(--gold);">${time}</span>
                ${isLive ? '<span class="badge" style="background:rgba(255,69,58,0.2); color:var(--red); border-color:var(--red); font-size:10px; display:inline-block; margin-top:2px;">مباشر ' + (ev.clock || '') + '</span>' : ''}
            </div>

            <div style="display: flex; align-items: center; justify-content: flex-end; gap: 8px;">
                <span style="font-weight: 700; font-size: 13.5px;">${hTeam}</span>
                ${hLogo ? `<img src="${hLogo}" style="width: 24px; height: 24px; object-fit: contain;">` : ''}
            </div>

            <div style="text-align: center;">
                <span style="background: rgba(255,255,255,0.08); padding: 4px 10px; border-radius: 6px; font-weight: 900; font-size: 14px;">${hScore} - ${aScore}</span>
            </div>

            <div style="display: flex; align-items: center; justify-content: flex-start; gap: 8px;">
                ${aLogo ? `<img src="${aLogo}" style="width: 24px; height: 24px; object-fit: contain;">` : ''}
                <span style="font-weight: 700; font-size: 13.5px;">${aTeam}</span>
            </div>

            <div style="display: flex; gap: 6px; justify-content: flex-end; align-items: center;">
                <button class="btn btn-blue" style="padding: 5px 10px; font-size: 11px;" onclick="viewApiMatchDetails('${ev.api_id}', '${encodeURIComponent(JSON.stringify(ev))}')">
                    📊 التشكيلة والإحصائيات
                </button>
                <button class="btn btn-green" style="padding: 5px 8px; font-size: 11px;" onclick="importApiMatchDirectly('${encodeURIComponent(JSON.stringify(ev))}')">
                    ➕ إضافة
                </button>
            </div>
        </div>`;
    });

    container.innerHTML = html;
}

async function viewApiMatchDetails(eventId, rawEvJson) {
    const card = document.getElementById('apiDetailsCard');
    const content = document.getElementById('apiDetailsContent');
    card.style.display = 'block';
    content.innerHTML = '<div style="padding:30px; text-align:center;"><div style="display:inline-block; width:24px; height:24px; border:3px solid var(--gold); border-radius:50%; border-top-color:transparent; animation:spin 1s linear infinite;"></div><p style="margin-top:10px;">جاري جلب التشكيلة الرسمية والإحصائيات من الـ API العالمي...</p></div>';

    const evData = JSON.parse(decodeURIComponent(rawEvJson));
    document.getElementById('apiDetailsTitle').innerText = '📊 تفاصيل ' + evData.home_team.name + ' ضد ' + evData.away_team.name;
    document.getElementById('apiAddAsCustomMatchBtn').onclick = () => importApiMatchDirectly(rawEvJson);

    try {
        const res = await fetch(API_URL + '?action=match_details&event_id=' + eventId);
        const data = await res.json();
        renderApiMatchFullDetails(data, evData);
    } catch (e) {
        content.innerHTML = '<p style="color:var(--red);">فشل جلب تفاصيل المباراة: ' + e.message + '</p>';
    }
}

function renderApiMatchFullDetails(data, ev) {
    const content = document.getElementById('apiDetailsContent');
    const stats = data.statistics || [];
    const lineups = data.lineups || { home: { starters: [] }, away: { starters: [] } };

    let html = `
    <!-- Top Scoreboard -->
    <div style="background: rgba(0,0,0,0.4); border-radius: 12px; padding: 16px; margin-bottom: 20px; text-align: center;">
        <div style="display: flex; justify-content: space-around; align-items: center;">
            <div style="text-align: center;">
                <img src="${ev.home_team.logo}" style="width: 48px; height: 48px; object-fit: contain; margin-bottom: 6px;">
                <h4 style="font-size: 15px; font-weight: 700;">${ev.home_team.name}</h4>
                <span style="font-size: 11px; color: var(--gold);">خطة: ${lineups.home.formation || '4-3-3'}</span>
            </div>
            <div>
                <span style="font-size: 28px; font-weight: 900; color: var(--gold);">${ev.home_score !== null ? ev.home_score : '-'} : ${ev.away_score !== null ? ev.away_score : '-'}</span>
                <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">${ev.date} • ${ev.time}</div>
            </div>
            <div style="text-align: center;">
                <img src="${ev.away_team.logo}" style="width: 48px; height: 48px; object-fit: contain; margin-bottom: 6px;">
                <h4 style="font-size: 15px; font-weight: 700;">${ev.away_team.name}</h4>
                <span style="font-size: 11px; color: var(--gold);">خطة: ${lineups.away.formation || '4-3-3'}</span>
            </div>
        </div>
    </div>

    <!-- Tabs inside Details: Stats & Lineups -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
        <!-- Column 1: Match Statistics -->
        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--card-border); border-radius: 12px; padding: 16px;">
            <h4 style="color: var(--gold); font-size: 14px; margin-bottom: 14px; display: flex; align-items: center; gap: 6px;">
                📈 إحصائيات المباراة المباشرة
            </h4>
            <div style="display: flex; flex-direction: column; gap: 12px;">
    `;

    if (stats.length === 0) {
        html += '<p style="color:var(--text-muted); font-size:12px;">الإحصائيات غير متوفرة بعد لهذه المباراة أو لم تبدأ بعد.</p>';
    } else {
        stats.forEach(st => {
            html += `
            <div>
                <div style="display: flex; justify-content: space-between; font-size: 12.5px; margin-bottom: 4px;">
                    <span style="font-weight: 700;">${st.home}</span>
                    <span style="color: var(--text-muted); font-size: 11.5px;">${st.label}</span>
                    <span style="font-weight: 700;">${st.away}</span>
                </div>
                <div style="height: 6px; background: rgba(255,255,255,0.1); border-radius: 3px; display: flex; overflow: hidden;">
                    <div style="width: 50%; background: var(--blue);"></div>
                    <div style="width: 50%; background: var(--gold);"></div>
                </div>
            </div>`;
        });
    }

    html += `
            </div>
        </div>

        <!-- Column 2: Official Lineups & Starters -->
        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--card-border); border-radius: 12px; padding: 16px;">
            <h4 style="color: var(--gold); font-size: 14px; margin-bottom: 14px; display: flex; align-items: center; gap: 6px;">
                👕 التشكيلة الأساسية (Lineups)
            </h4>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                <div>
                    <h5 style="font-size: 12px; color: var(--blue); margin-bottom: 8px;">${ev.home_team.name}</h5>
                    <ul style="list-style: none; padding: 0; font-size: 11.5px;">
    `;

    (lineups.home.starters || []).forEach(p => {
        html += `<li style="padding: 3px 0; border-bottom: 1px solid rgba(255,255,255,0.05); display: flex; justify-content: space-between;">
            <span>${p.jersey ? '<b>' + p.jersey + '.</b> ' : ''}${p.name}</span>
            <span style="color: var(--text-muted);">${p.position}</span>
        </li>`;
    });

    html += `
                    </ul>
                </div>
                <div>
                    <h5 style="font-size: 12px; color: var(--gold); margin-bottom: 8px;">${ev.away_team.name}</h5>
                    <ul style="list-style: none; padding: 0; font-size: 11.5px;">
    `;

    (lineups.away.starters || []).forEach(p => {
        html += `<li style="padding: 3px 0; border-bottom: 1px solid rgba(255,255,255,0.05); display: flex; justify-content: space-between;">
            <span>${p.jersey ? '<b>' + p.jersey + '.</b> ' : ''}${p.name}</span>
            <span style="color: var(--text-muted);">${p.position}</span>
        </li>`;
    });

    html += `
                    </ul>
                </div>
            </div>
        </div>
    </div>
    `;

    content.innerHTML = html;
}

function importApiMatchDirectly(rawEvJson) {
    const ev = JSON.parse(decodeURIComponent(rawEvJson));
    document.getElementById('cm_championship').value = ev.league || 'دوري';
    document.getElementById('cm_home_team').value = ev.home_team.name;
    document.getElementById('cm_home_image').value = ev.home_team.logo || '';
    document.getElementById('cm_away_team').value = ev.away_team.name;
    document.getElementById('cm_away_image').value = ev.away_team.logo || '';
    document.getElementById('cm_date').value = ev.date || '<?= $targetDate ?>';
    document.getElementById('cm_time').value = ev.time || '21:00';
    document.getElementById('cm_stream_url').value = '';
    openModal('customMatchModal');
}

// -------------------------------------------------------------
// LOGOS LIBRARY JAVASCRIPT CONTROLLER
// -------------------------------------------------------------
let allLoadedLogos = [];
let currentLogosCategory = 'all';
let activeLogoTargetInputId = null;
let activeNameTargetInputId = null;

async function fetchLogos(category = 'all', query = '') {
    try {
        const url = `${API_URL}?action=logos&cat=${encodeURIComponent(category)}&q=${encodeURIComponent(query)}`;
        const res = await fetch(url);
        const data = await res.json();
        return data.logos || [];
    } catch (e) {
        return [];
    }
}

async function renderLogosLibrary() {
    const grid = document.getElementById('logosGrid');
    if (!grid) return;
    grid.innerHTML = '<div style="padding:20px; text-align:center;"><div style="display:inline-block; width:24px; height:24px; border:3px solid var(--gold); border-radius:50%; border-top-color:transparent; animation:spin 1s linear infinite;"></div><p style="margin-top:8px;">جاري تحميل الشعارات عالية الدقة...</p></div>';
    
    const query = document.getElementById('logoSearchInput') ? document.getElementById('logoSearchInput').value.trim() : '';
    allLoadedLogos = await fetchLogos(currentLogosCategory, query);

    if (allLoadedLogos.length === 0) {
        grid.innerHTML = '<p style="padding:20px; text-align:center; color:var(--text-muted); grid-column:1/-1;">لم يتم العثور على أي شعارات مطابقة.</p>';
        return;
    }

    let html = '';
    allLoadedLogos.forEach(item => {
        html += `
        <div class="card" style="margin-bottom:0; padding:12px; display:flex; flex-direction:column; align-items:center; justify-content:space-between; text-align:center; background:rgba(255,255,255,0.02); transition:transform 0.2s;" onmouseover="this.style.transform='translateY(-3px)'" onmouseout="this.style.transform='none'">
            <div style="width:60px; height:60px; display:flex; align-items:center; justify-content:center; margin-bottom:10px;">
                <img src="${item.logo}" alt="${item.name}" style="max-width:100%; max-height:100%; object-fit:contain;">
            </div>
            <div style="font-size:13px; font-weight:700; margin-bottom:4px; max-width:100%; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${item.name}</div>
            <div style="font-size:11px; color:var(--text-muted); margin-bottom:10px;">${item.en_name || ''}</div>
            <div style="display:flex; gap:6px; width:100%;">
                <button type="button" class="btn btn-outline" style="flex:1; padding:4px 6px; font-size:11px;" onclick="copyLogoUrl('${item.logo}')">📋 نسخ الرابط</button>
                <button type="button" class="btn btn-blue" style="padding:4px 8px; font-size:11px;" onclick="quickUseLogoInMatch('${item.name}', '${item.logo}')">➕ استخدم</button>
            </div>
        </div>
        `;
    });
    grid.innerHTML = html;
}

function selectLogoCategory(cat) {
    currentLogosCategory = cat;
    renderLogosLibrary();
}

function filterLogosLibrary() {
    renderLogosLibrary();
}

function copyLogoUrl(url) {
    navigator.clipboard.writeText(url);
    alert('تم نسخ رابط الشعار بدقة عالية: ' + url);
}

function quickUseLogoInMatch(name, url) {
    openCustomMatchModal();
    const h = document.getElementById('cm_home_team');
    const hImg = document.getElementById('cm_home_image');
    if (!h.value) {
        h.value = name;
        hImg.value = url;
    } else {
        document.getElementById('cm_away_team').value = name;
        document.getElementById('cm_away_image').value = url;
    }
}

async function openLogoPickerFor(targetImageInputId, targetNameInputId = null) {
    activeLogoTargetInputId = targetImageInputId;
    activeNameTargetInputId = targetNameInputId;
    openModal('logoPickerModal');
    renderPickerLogos();
}

async function renderPickerLogos() {
    const grid = document.getElementById('pickerGrid');
    const query = document.getElementById('pickerSearchInput') ? document.getElementById('pickerSearchInput').value.trim() : '';
    const logos = await fetchLogos('all', query);

    if (logos.length === 0) {
        grid.innerHTML = '<p style="padding:20px; text-align:center; color:var(--text-muted); grid-column:1/-1;">لا توجد نتائج مطابقة.</p>';
        return;
    }

    let html = '';
    logos.forEach(item => {
        html += `
        <div style="cursor:pointer; padding:10px; background:rgba(255,255,255,0.03); border:1px solid var(--card-border); border-radius:10px; text-align:center; transition:border-color 0.2s;" onmouseover="this.style.borderColor='var(--gold)'" onmouseout="this.style.borderColor='var(--card-border)'" onclick="selectPickerLogo('${item.name}', '${item.logo}')">
            <img src="${item.logo}" alt="${item.name}" style="width:40px; height:40px; object-fit:contain; margin-bottom:6px;">
            <div style="font-size:11px; font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${item.name}</div>
        </div>
        `;
    });
    grid.innerHTML = html;
}

function filterPickerLogos() {
    renderPickerLogos();
}

function selectPickerLogo(name, logoUrl) {
    if (activeLogoTargetInputId) {
        const imgInput = document.getElementById(activeLogoTargetInputId);
        if (imgInput) imgInput.value = logoUrl;
    }
    if (activeNameTargetInputId) {
        const nameInput = document.getElementById(activeNameTargetInputId);
        if (nameInput && (!nameInput.value || nameInput.value.trim() === '')) {
            nameInput.value = name;
        }
    }
    closeModal('logoPickerModal');
}

// Initial load of logos when page is ready
document.addEventListener('DOMContentLoaded', () => {
    renderLogosLibrary();
});
</script>

<style>
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>

</body>
</html>
