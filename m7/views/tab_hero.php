<!-- 3. TAB: HERO BANNERS & FEATURED -->
<div id="tab-hero" class="tab-content">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">بوسترات الهيرو الرئيسية (Hero Header Banners)</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">البانرات الكبرى الترويجية في أعلى واجهة التطبيق مع روابط البث وسيرفرات القمة</p>
        </div>
        <div>
            <button class="btn btn-blue" onclick="openHeroModal()">➕ إضافة بوستر هيرو جديد</button>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px;">
        <?php foreach ($currentHero as $hb): ?>
            <div class="card" style="padding: 0; overflow: hidden; margin-bottom: 0;">
                <div style="height: 140px; background-image: url('<?= htmlspecialchars($hb['bannerUrl'] ?? '') ?>'); background-size: cover; background-position: center; position: relative; padding: 12px; display: flex; flex-direction: column; justify-content: flex-end;">
                    <div style="position: absolute; inset: 0; background: linear-gradient(180deg, transparent 0%, rgba(7,9,14,0.95) 100%);"></div>
                    <div style="position: relative; z-index: 2;">
                        <span class="badge" style="margin-bottom: 4px;"><?= htmlspecialchars($hb['tournament'] ?? '') ?></span>
                        <h3 style="font-size: 16px; font-weight: 900; color: #fff;"><?= htmlspecialchars($hb['title'] ?? '') ?></h3>
                    </div>
                </div>
                <div style="padding: 14px; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-size: 12px; color: var(--text-muted);">المعلق: <?= htmlspecialchars($hb['commentator'] ?? 'عصام الشوالي') ?></span>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px;" onclick='openHeroModal(<?= json_encode($hb) ?>)'>✏️ تعديل</button>
                        <button class="btn btn-red" style="padding: 4px 8px; font-size: 11px;" onclick="deleteHeroBanner('<?= $hb['id'] ?>')">🗑️</button>
                    </div>
                </div>
            </div>
        <?php endforeach; ?>
    </div>
</div>

<!-- 3.2 TAB: FEATURED WEEKLY -->
<div id="tab-featured" class="tab-content">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">⭐ أفضل مباريات الأسبوع (Featured Weekly Matches)</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">أبرز قمم الدوريات العالمية مع روابط البث المباشر المسبق</p>
        </div>
        <div>
            <button class="btn btn-blue" onclick="openFeaturedModal()">➕ إضافة مباراة مميزة</button>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px;">
        <?php foreach ($currentFeatured as $fw): ?>
            <div class="card" style="margin-bottom: 0;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                    <span class="badge"><?= htmlspecialchars($fw['tournament'] ?? 'الدوري الممتاز') ?></span>
                    <span style="font-size: 12px; color: var(--gold); font-weight: 700;"><?= htmlspecialchars($fw['kickoffDate'] ?? 'السبت') ?> <?= htmlspecialchars($fw['kickoffTime'] ?? '22:00') ?></span>
                </div>
                <h3 style="font-size: 16px; font-weight: 800; margin-bottom: 8px;"><?= htmlspecialchars($fw['title'] ?? '') ?></h3>
                <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">القناة: <?= htmlspecialchars($fw['channelName'] ?? '') ?> • المعلق: <?= htmlspecialchars($fw['commentator'] ?? '') ?></p>
                <div style="display: flex; gap: 6px; justify-content: flex-end;">
                    <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px;" onclick='openFeaturedModal(<?= json_encode($fw) ?>)'>✏️ تعديل</button>
                    <button class="btn btn-red" style="padding: 4px 8px; font-size: 11px;" onclick="deleteFeaturedMatch('<?= $fw['id'] ?>')">🗑️ حذف</button>
                </div>
            </div>
        <?php endforeach; ?>
    </div>
</div>
