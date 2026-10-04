<!-- 1. TAB: MATCHES & STREAMS -->
<div id="tab-matches" class="tab-content active">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">جدول المباريات وسيرفرات المشاهدة</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">تحكم كامل في تفعيل البث، النتائج، المعلقين، وإضافة سيرفرات 4K و FHD لكل مباراة</p>
        </div>
        <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <a href="?date=yesterday" class="btn btn-outline <?= ($dateParam === 'yesterday') ? 'style=\"border-color:var(--gold);\"' : '' ?>">الأمس</a>
            <a href="?date=<?= $todayDate ?>" class="btn btn-outline <?= ($targetDate === $todayDate && $dateParam !== 'yesterday' && $dateParam !== 'tomorrow') ? 'style=\"border-color:var(--gold);\"' : '' ?>">اليوم</a>
            <a href="?date=tomorrow" class="btn btn-outline <?= ($dateParam === 'tomorrow') ? 'style=\"border-color:var(--gold);\"' : '' ?>">الغد</a>
            <input type="date" value="<?= $targetDate ?>" onchange="location.href='?date='+this.value" class="form-control" style="width: auto; padding: 6px 10px;">
            <button class="btn btn-blue" onclick="openCustomMatchModal()">➕ إضافة مباراة مخصصة</button>
        </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
        <div style="padding: 12px 16px; background: rgba(255,255,255,0.02); border-bottom: 1px solid var(--card-border); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 13px; font-weight: 700; color: var(--gold);">عدد المباريات المتوفرة: <?= count($enrichedMatches) ?></span>
            <input type="text" id="matchSearchInput" onkeyup="filterMatchesTable()" placeholder="🔍 بحث سريع عن فريق أو بطولة..." class="form-control" style="max-width: 280px; padding: 6px 12px;">
        </div>

        <div id="matchesListContainer">
            <?php if (empty($enrichedMatches)): ?>
                <div style="padding: 40px; text-align: center; color: var(--text-muted);">
                    <p style="font-size: 15px;">لا توجد مباريات مسجلة لهذا التاريخ (<?= $targetDate ?>).</p>
                    <button class="btn btn-blue" style="margin-top: 10px;" onclick="openCustomMatchModal()">➕ إضافة مباراة الآن</button>
                </div>
            <?php else: ?>
                <?php foreach ($enrichedMatches as $m): 
                    $mId = strval($m['match_id'] ?? '');
                    $hTeam = $m['home_team']['title'] ?? 'فريق 1';
                    $hLogo = $m['home_team']['image'] ?? '';
                    $aTeam = $m['away_team']['title'] ?? 'فريق 2';
                    $aLogo = $m['away_team']['image'] ?? '';
                    $comp = $m['championship']['title'] ?? 'البطولة';
                    $time = substr($m['match_time'] ?? '20:00', 0, 5);
                    $hScore = isset($m['home_scores']) ? $m['home_scores'] : '-';
                    $aScore = isset($m['away_scores']) ? $m['away_scores'] : '-';
                    $isLive = in_array(intval($m['status'] ?? 0), [2, 3]) || !empty($m['live']);
                    $isStreamActive = !empty($m['is_stream_active']);
                    $serversCount = count($m['servers'] ?? []);
                ?>
                <div class="match-row" data-search="<?= htmlspecialchars($hTeam . ' ' . $aTeam . ' ' . $comp) ?>">
                    <div>
                        <span style="font-size: 11px; color: var(--text-muted); display: block;"><?= htmlspecialchars($comp) ?></span>
                        <span style="font-weight: 700; font-size: 13px; color: var(--gold);"><?= $time ?></span>
                        <?php if ($isLive): ?>
                            <span class="badge" style="background: rgba(255,69,58,0.2); color: var(--red); border-color: var(--red); font-size: 10px; display: inline-block; margin-top: 2px;">مباشر <?= htmlspecialchars($m['score_time'] ?? '') ?></span>
                        <?php endif; ?>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: flex-end; gap: 8px;">
                        <span style="font-weight: 700; font-size: 13.5px;"><?= htmlspecialchars($hTeam) ?></span>
                        <?php if ($hLogo): ?><img src="<?= htmlspecialchars($hLogo) ?>" style="width: 24px; height: 24px; object-fit: contain;"><?php endif; ?>
                    </div>

                    <div style="text-align: center;">
                        <span style="background: rgba(255,255,255,0.08); padding: 4px 10px; border-radius: 6px; font-weight: 900; font-size: 14px;"><?= $hScore ?> - <?= $aScore ?></span>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: flex-start; gap: 8px;">
                        <?php if ($aLogo): ?><img src="<?= htmlspecialchars($aLogo) ?>" style="width: 24px; height: 24px; object-fit: contain;"><?php endif; ?>
                        <span style="font-weight: 700; font-size: 13.5px;"><?= htmlspecialchars($aTeam) ?></span>
                    </div>

                    <div style="display: flex; gap: 6px; justify-content: flex-end; align-items: center;">
                        <button class="btn <?= $isStreamActive ? 'btn-green' : 'btn-outline' ?>" style="padding: 5px 10px; font-size: 11px;" onclick="toggleMatchStream('<?= $mId ?>', <?= $isStreamActive ? 'false' : 'true' ?>)">
                            <?= $isStreamActive ? '⚡ البث شغال' : 'تفعيل البث' ?>
                        </button>
                        <button class="btn btn-blue" style="padding: 5px 10px; font-size: 11px;" onclick='openServersModal("<?= $mId ?>", "<?= htmlspecialchars($hTeam . ' ضد ' . $aTeam, ENT_QUOTES) ?>", <?= json_encode($m['servers'] ?? []) ?>)'>
                            📡 السيرفرات (<?= $serversCount ?>)
                        </button>
                        <button class="btn btn-outline" style="padding: 5px 8px; font-size: 11px;" onclick='openEditMatchModal("<?= $mId ?>", <?= json_encode($m) ?>)'>✏️</button>
                    </div>
                </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </div>
</div>
