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
