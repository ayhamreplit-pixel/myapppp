<!-- 4. TAB: SLIDER -->
<div id="tab-slider" class="tab-content">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">إدارة سلايدر التطبيق الترويجي</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">إضافة صور وبوسترات السلايدر المتحرك في واجهة التطبيق</p>
        </div>
        <div>
            <button class="btn btn-blue" onclick="openAddSliderModal()">➕ إضافة بوستر جديد</button>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;">
        <?php foreach ($currentSlider as $sl): ?>
            <div class="card" style="padding: 0; overflow: hidden; margin-bottom: 0;">
                <img src="<?= htmlspecialchars($sl['image'] ?? '') ?>" style="width: 100%; height: 140px; object-fit: cover;">
                <div style="padding: 12px; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-size: 13px; font-weight: 700;"><?= htmlspecialchars($sl['title'] ?? 'بوستر') ?></span>
                    <button class="btn btn-red" style="padding: 4px 8px; font-size: 11px;" onclick="deleteSlider('<?= $sl['id'] ?>')">🗑️</button>
                </div>
            </div>
        <?php endforeach; ?>
    </div>
</div>

<!-- 5. TAB: NOTIFICATIONS -->
<div id="tab-notifications" class="tab-content">
    <div class="card" style="max-width: 700px; margin: 0 auto;">
        <h2 style="font-size: 18px; color: var(--gold); font-weight: 900; margin-bottom: 12px;">إرسال تنبيه أو إشعار مباشر لجميع المستخدمين</h2>
        <p style="font-size: 12.5px; color: var(--text-muted); margin-bottom: 20px;">يظهر هذا التنبيه كشريط عاجل أو نافذة منبثقة داخل التطبيق فورياً</p>

        <form onsubmit="saveAnnouncement(event)">
            <div class="form-group">
                <label>عنوان الإشعار</label>
                <input type="text" id="notif_title" class="form-control" value="<?= htmlspecialchars($currentAnnounce['title'] ?? '') ?>" placeholder="مثال: قمة الليلة بدأت الآن!" required>
            </div>
            <div class="form-group">
                <label>نص الرسالة</label>
                <textarea id="notif_message" class="form-control" rows="3" placeholder="اكتب تفاصيل التنبيه هنا..." required><?= htmlspecialchars($currentAnnounce['message'] ?? '') ?></textarea>
            </div>
            <div class="form-group">
                <label>توجيه المستخدم عند الضغط</label>
                <select id="notif_target_type" class="form-control">
                    <option value="none">بدون توجيه</option>
                    <option value="match" <?= (($currentAnnounce['target_type'] ?? '') === 'match') ? 'selected' : '' ?>>مباراة معينة</option>
                    <option value="channel" <?= (($currentAnnounce['target_type'] ?? '') === 'channel') ? 'selected' : '' ?>>قناة تلفزيونية</option>
                </select>
            </div>
            <div class="form-group">
                <label>معرف الهدف (ID المباراة أو القناة - اختياري)</label>
                <input type="text" id="notif_target_id" class="form-control" value="<?= htmlspecialchars($currentAnnounce['target_id'] ?? '') ?>" placeholder="id...">
            </div>
            <div style="display: flex; gap: 10px; margin-top: 20px;">
                <button type="submit" class="btn btn-green">📢 نشر وتفعيل التنبيه الآن</button>
                <button type="button" class="btn btn-red" onclick="clearAnnouncement()">🚫 إيقاف التنبيه</button>
            </div>
        </form>
    </div>
</div>
