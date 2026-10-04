<!-- 6. TAB: SECURITY & AES-256 ENCRYPTION -->
<div id="tab-security" class="tab-content">
    <div class="card" style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
            <div>
                <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">🔒 نظام تشفير وحماية بيانات البث (AES-256)</h2>
                <p style="font-size: 12.5px; color: var(--text-muted);">حماية روابط السيرفرات والبث المباشر ضد السرقة والتتبع، مع فك تشفير تلقائي فائق السرعة داخل التطبيق</p>
            </div>
            <div style="background: rgba(48,209,88,0.15); border: 1px solid var(--green); padding: 6px 14px; border-radius: 20px; font-size: 13px; color: var(--green); font-weight: 700;">
                🛡️ حالة التشفير: نشطة وآمنة (AES-256-CBC)
            </div>
        </div>

        <form onsubmit="saveSettingsForm(event)" style="background: rgba(255,255,255,0.02); padding: 18px; border-radius: 12px; border: 1px solid var(--card-border); margin-bottom: 24px;">
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 15px;">
                <div class="form-group">
                    <label>مفتاح التشفير الرئيسي (Master Secret Key)</label>
                    <input type="text" id="cfg_master_key" class="form-control" value="<?= htmlspecialchars($currentSettings['master_key'] ?? DEFAULT_MASTER_KEY) ?>" style="direction: ltr; font-family: monospace; font-weight: bold; color: var(--gold);" required>
                    <small style="color: var(--text-muted); font-size: 11px;">المفتاح الموحد المستخدم لتشفير وفك تشفير البث بين السيرفر وتطبيق الأندرويد.</small>
                </div>
                <div class="form-group">
                    <label>تشفير جميع روابط البث والسيرفرات (Auto Stream Encryption)</label>
                    <select id="cfg_encrypt_streams" class="form-control">
                        <option value="1" <?= (!isset($currentSettings['encrypt_streams']) || $currentSettings['encrypt_streams'] !== false) ? 'selected' : '' ?>>✅ مفعل (يتم تشفير جميع الروابط بهيئة enc:...)</option>
                        <option value="0" <?= (isset($currentSettings['encrypt_streams']) && $currentSettings['encrypt_streams'] === false) ? 'selected' : '' ?>>❌ معطل (إرسال روابط عادية)</option>
                    </select>
                    <small style="color: var(--text-muted); font-size: 11px;">يمنع المتطفلين من التقاط روابط m3u8 وسرقة سيرفرات البث.</small>
                </div>
            </div>
            <div style="margin-top: 15px;">
                <button type="submit" class="btn">💾 حفظ إعدادات الأمان والتشفير</button>
            </div>
        </form>

        <h3 style="font-size: 16px; color: var(--gold); margin-bottom: 12px;">🧪 أداة تجربة التشفير وفك التشفير المباشر (Crypto Live Tester)</h3>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
            <div style="background: #090c14; padding: 15px; border-radius: 10px; border: 1px solid #1a233a;">
                <h4 style="font-size: 14px; margin-bottom: 10px; color: var(--blue);">1. تشفير رابط مباشر (Encrypt to enc:...)</h4>
                <input type="text" id="test_plain_input" class="form-control" placeholder="https://domain.com/live.m3u8" style="direction: ltr; margin-bottom: 10px;">
                <button type="button" class="btn btn-blue" onclick="testEncryptUrl()">🔒 تشفير الرابط الآن</button>
                <div id="test_encrypt_result" style="margin-top: 10px; word-break: break-all; font-size: 12px; font-family: monospace; color: var(--green);"></div>
            </div>
            <div style="background: #090c14; padding: 15px; border-radius: 10px; border: 1px solid #1a233a;">
                <h4 style="font-size: 14px; margin-bottom: 10px; color: var(--gold);">2. فك تشفير نص مشفر (Decrypt enc:...)</h4>
                <input type="text" id="test_cipher_input" class="form-control" placeholder="enc:..." style="direction: ltr; margin-bottom: 10px;">
                <button type="button" class="btn" onclick="testDecryptCipher()">🔓 فك التشفير وتشغيل</button>
                <div id="test_decrypt_result" style="margin-top: 10px; word-break: break-all; font-size: 12px; font-family: monospace; color: var(--gold);"></div>
            </div>
        </div>
    </div>
</div>
