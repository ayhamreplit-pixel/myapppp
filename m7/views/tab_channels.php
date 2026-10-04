<!-- 2. TAB: LIVE CHANNELS -->
<div id="tab-channels" class="tab-content">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">إدارة القنوات التلفزيونية الرياضية المباشرة</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">إضافة وتعديل قنوات beIN SPORTS و SSC وقنوات البث المباشر (HLS / m3u8)</p>
        </div>
        <div>
            <button class="btn btn-blue" onclick="openAddChannelModal()">➕ إضافة قناة جديدة</button>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;">
        <?php foreach ($currentChannels as $ch): ?>
            <div class="card" style="margin-bottom: 0; display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
                        <?php if (!empty($ch['iconUrl'])): ?>
                            <img src="<?= htmlspecialchars($ch['iconUrl']) ?>" style="width: 44px; height: 44px; object-fit: contain; border-radius: 8px; background: rgba(255,255,255,0.05); padding: 4px;">
                        <?php else: ?>
                            <div style="width: 44px; height: 44px; border-radius: 8px; background: rgba(255,184,0,0.1); color: var(--gold); display: flex; align-items: center; justify-content: center; font-size: 20px;">📺</div>
                        <?php endif; ?>
                        <div>
                            <h3 style="font-size: 15px; font-weight: 700;"><?= htmlspecialchars($ch['name']) ?></h3>
                            <span style="font-size: 11px; color: var(--text-muted);"><?= htmlspecialchars($ch['categoryId'] ?? 'عام') ?> • <?= htmlspecialchars($ch['quality'] ?? '1080p') ?></span>
                        </div>
                    </div>
                    <div style="background: rgba(0,0,0,0.3); padding: 8px; border-radius: 6px; font-size: 11px; color: #aaa; word-break: break-all; direction: ltr; margin-bottom: 12px;">
                        <?= htmlspecialchars(substr($ch['playUrl'] ?? '', 0, 45)) ?>...
                    </div>
                </div>
                <div style="display: flex; gap: 8px; justify-content: flex-end;">
                    <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px;" onclick="playInTestPlayer('<?= addslashes($ch['playUrl'] ?? '') ?>', '<?= addslashes($ch['name'] ?? '') ?>')">▶️ تجربة</button>
                    <button class="btn btn-red" style="padding: 4px 8px; font-size: 11px;" onclick="deleteChannel('<?= $ch['streamId'] ?>')">🗑️ حذف</button>
                </div>
            </div>
        <?php endforeach; ?>
    </div>
</div>
