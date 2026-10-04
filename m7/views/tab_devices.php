<!-- TAB: LIVE VIEWERS & DEVICE SESSIONS -->
<div id="tab-devices" class="tab-content">
    <!-- Active Viewers Live Banner -->
    <div class="card" style="border-color: rgba(48,209,88,0.3); background: linear-gradient(180deg, #121c1f 0%, #0d141a 100%);">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span style="display: inline-block; width: 14px; height: 14px; background: var(--green); border-radius: 50%; box-shadow: 0 0 12px var(--green); animation: pulse 1.5s infinite;"></span>
                <h2 style="font-size: 19px; color: #fff; font-weight: 900;">من يشاهد الآن؟ (المشاهدون والأجهزة النشطة)</h2>
            </div>
            <div style="display: flex; gap: 10px; align-items: center;">
                <span class="badge" style="background: rgba(48,209,88,0.15); color: var(--green); border-color: var(--green); font-size: 13px;" id="liveViewersCountBadge">
                    🟢 <?= count($activeSessions) ?> جهاز متصل حالياً
                </span>
                <button class="btn btn-outline" onclick="loadLiveSessions()" style="padding: 5px 12px; font-size: 12px;">🔄 تحديث فوري</button>
            </div>
        </div>

        <div id="liveViewersGrid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 14px;">
            <?php if (empty($activeSessions)): ?>
                <div style="grid-column: 1 / -1; padding: 30px; text-align: center; color: var(--text-muted); background: rgba(0,0,0,0.2); border-radius: 10px;">
                    لا يوجد مستخدمون يشاهدون حالياً (يتم رصد المتصلين في آخر 60 ثانية تلقائياً)
                </div>
            <?php else: ?>
                <?php foreach ($activeSessions as $s): ?>
                    <div style="background: rgba(0,0,0,0.4); border: 1px solid rgba(48,209,88,0.2); border-radius: 12px; padding: 14px;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                            <span style="font-weight: 800; font-size: 14px; color: #fff;">📱 <?= htmlspecialchars($s['device_name'] ?? 'جهاز أندرويد') ?></span>
                            <span class="badge" style="background: rgba(10,132,255,0.15); color: var(--blue); font-size: 10px;"><?= htmlspecialchars($s['profile_name'] ?? 'مستخدم') ?></span>
                        </div>
                        <div style="font-size: 12px; color: var(--gold); margin-bottom: 8px; font-weight: 700;">
                            ▶️ يشاهد الآن: <?= htmlspecialchars($s['active_stream'] ?? 'التطبيق') ?>
                        </div>
                        <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-muted);">
                            <span>IP: <?= htmlspecialchars($s['ip'] ?? '127.0.0.1') ?></span>
                            <span>آخر نبضة: <?= htmlspecialchars($s['last_ping_formatted'] ?? '') ?></span>
                        </div>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </div>

    <!-- Registered Devices Table & Policy -->
    <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
            <div>
                <h3 style="font-size: 17px; color: var(--gold); font-weight: 900;">سجل الأجهزة وإدارة الصلاحيات والحظر</h3>
                <p style="font-size: 12.5px; color: var(--text-muted);">التحكم الصارم في الأجهزة المسموح لها بالاتصال، وحظر الأجهزة غير المرغوبة</p>
            </div>
            <div style="display: flex; gap: 8px;">
                <select id="cfg_access_policy" onchange="saveAccessPolicy(this.value)" class="form-control" style="width: auto;">
                    <option value="open" <?= (($currentSettings['access_policy'] ?? 'open') === 'open') ? 'selected' : '' ?>>🔓 وصول مفتوح لجميع أجهزة التطبيق</option>
                    <option value="whitelist" <?= (($currentSettings['access_policy'] ?? '') === 'whitelist') ? 'selected' : '' ?>>🔒 يتطلب موافقة الأدمن (Whitelist)</option>
                </select>
            </div>
        </div>

        <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; font-size: 13px; text-align: right;">
                <thead>
                    <tr style="border-bottom: 1px solid var(--card-border); color: var(--text-muted);">
                        <th style="padding: 10px;">الجهاز</th>
                        <th style="padding: 10px;">معرف الجهاز (Device ID)</th>
                        <th style="padding: 10px;">إصدار النظام / التطبيق</th>
                        <th style="padding: 10px;">IP / آخر نشاط</th>
                        <th style="padding: 10px;">الحالة</th>
                        <th style="padding: 10px; text-align: left;">إجراءات</th>
                    </tr>
                </thead>
                <tbody id="registeredDevicesTbody">
                    <?php if (empty($registeredDevices)): ?>
                        <tr>
                            <td colspan="6" style="padding: 24px; text-align: center; color: var(--text-muted);">لا توجد أجهزة مسجلة بعد</td>
                        </tr>
                    <?php else: ?>
                        <?php foreach ($registeredDevices as $dev): 
                            $isBanned = !empty($dev['banned']);
                            $isApproved = !empty($dev['approved']);
                            $dId = htmlspecialchars($dev['device_id'] ?? '');
                        ?>
                        <tr style="border-bottom: 1px solid rgba(255,255,255,0.04);">
                            <td style="padding: 10px; font-weight: 700; color: #fff;">
                                📱 <?= htmlspecialchars($dev['device_name'] ?? 'Android') ?>
                            </td>
                            <td style="padding: 10px; font-family: monospace; font-size: 11px; color: var(--text-muted);">
                                <?= $dId ?>
                            </td>
                            <td style="padding: 10px; font-size: 12px;">
                                Android <?= htmlspecialchars($dev['android_version'] ?? '14') ?> • v<?= htmlspecialchars($dev['app_version'] ?? '3.5.0') ?>
                            </td>
                            <td style="padding: 10px; font-size: 11.5px; color: var(--text-muted);">
                                <?= htmlspecialchars($dev['ip'] ?? '') ?><br>
                                <span style="font-size: 10.5px;"><?= htmlspecialchars($dev['last_active'] ?? '') ?></span>
                            </td>
                            <td style="padding: 10px;">
                                <?php if ($isBanned): ?>
                                    <span class="badge" style="background: rgba(255,69,58,0.2); color: var(--red); border-color: var(--red);">🚫 محظور</span>
                                <?php elseif ($isApproved): ?>
                                    <span class="badge" style="background: rgba(48,209,88,0.15); color: var(--green); border-color: var(--green);">✅ معتمد ومفعل</span>
                                <?php else: ?>
                                    <span class="badge" style="background: rgba(255,184,0,0.15); color: var(--gold); border-color: var(--gold);">⏳ قيد الانتظار</span>
                                <?php endif; ?>
                            </td>
                            <td style="padding: 10px; text-align: left;">
                                <div style="display: inline-flex; gap: 6px;">
                                    <?php if (!$isApproved): ?>
                                        <button class="btn btn-green" style="padding: 4px 8px; font-size: 11px;" onclick="approveDevice('<?= $dId ?>')">اعتماد</button>
                                    <?php endif; ?>
                                    <button class="btn <?= $isBanned ? 'btn-green' : 'btn-red' ?>" style="padding: 4px 8px; font-size: 11px;" onclick="toggleDeviceBan('<?= $dId ?>', <?= $isBanned ? 'false' : 'true' ?>)">
                                        <?= $isBanned ? 'إلغاء الحظر' : 'حظر الجهاز' ?>
                                    </button>
                                    <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px;" onclick="deleteDevice('<?= $dId ?>')">🗑️</button>
                                </div>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
