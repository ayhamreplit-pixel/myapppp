<!-- 1. TAB: MATCHES & STREAMS -->
<div id="tab-matches" class="tab-content active">
    <!-- Top Summary Dashboard Cards -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 20px;">
        <div class="card" style="margin-bottom: 0; padding: 18px; border-left: 4px solid var(--gold); display: flex; align-items: center; justify-content: space-between;">
            <div>
                <span style="font-size: 12px; color: var(--text-muted); font-weight: 700; display: block;">إجمالي مباريات اليوم</span>
                <span style="font-size: 26px; font-weight: 900; color: #fff;"><?= count($enrichedMatches) ?></span>
            </div>
            <div style="font-size: 28px; opacity: 0.8;">⚽</div>
        </div>
        <div class="card" style="margin-bottom: 0; padding: 18px; border-left: 4px solid var(--red); display: flex; align-items: center; justify-content: space-between;">
            <div>
                <span style="font-size: 12px; color: var(--text-muted); font-weight: 700; display: block;">مباريات جارية الآن (Live)</span>
                <?php 
                    $liveMatchesCount = count(array_filter($enrichedMatches, function($m) {
                        return in_array(intval($m['status'] ?? 0), [2, 3]) || !empty($m['live']);
                    }));
                ?>
                <span style="font-size: 26px; font-weight: 900; color: var(--red);"><?= $liveMatchesCount ?></span>
            </div>
            <div style="font-size: 28px; opacity: 0.8;">🔴</div>
        </div>
        <div class="card" style="margin-bottom: 0; padding: 18px; border-left: 4px solid var(--green); display: flex; align-items: center; justify-content: space-between;">
            <div>
                <span style="font-size: 12px; color: var(--text-muted); font-weight: 700; display: block;">سيرفرات بث نشطة ⚡</span>
                <?php 
                    $activeStreamsCount = count(array_filter($enrichedMatches, function($m) {
                        return !empty($m['is_stream_active']);
                    }));
                ?>
                <span style="font-size: 26px; font-weight: 900; color: var(--green);"><?= $activeStreamsCount ?></span>
            </div>
            <div style="font-size: 28px; opacity: 0.8;">📡</div>
        </div>
        <div class="card" style="margin-bottom: 0; padding: 18px; border-left: 4px solid var(--blue); display: flex; align-items: center; justify-content: space-between;">
            <div>
                <span style="font-size: 12px; color: var(--text-muted); font-weight: 700; display: block;">أجهزة متصلة بالتطبيق</span>
                <span style="font-size: 26px; font-weight: 900; color: var(--blue);"><?= count($activeSessions) ?></span>
            </div>
            <div style="font-size: 28px; opacity: 0.8;">📱</div>
        </div>
    </div>

    <!-- Filter & Action Bar -->
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; padding: 18px 24px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">جدول المباريات وسيرفرات المشاهدة المباشرة</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">تحكم فوري ودقيق في تشغيل البث، تحديث النتيجة، المعلقين وسيرفرات الجودة العالية</p>
        </div>
        <div style="display: flex; gap: 8px; flex-wrap: wrap; align-items: center;">
            <a href="?date=yesterday" class="btn <?= ($dateParam === 'yesterday') ? 'btn-blue' : 'btn-outline' ?>">الأمس</a>
            <a href="?date=<?= $todayDate ?>" class="btn <?= ($targetDate === $todayDate && $dateParam !== 'yesterday' && $dateParam !== 'tomorrow') ? 'btn-blue' : 'btn-outline' ?>">اليوم</a>
            <a href="?date=tomorrow" class="btn <?= ($dateParam === 'tomorrow') ? 'btn-blue' : 'btn-outline' ?>">الغد</a>
            <input type="date" value="<?= $targetDate ?>" onchange="location.href='?date='+this.value" class="form-control" style="width: auto; padding: 7px 12px; display: inline-block;">
            <button class="btn" style="background: var(--gold); color: #000;" onclick="openCustomMatchModal()">➕ إضافة مباراة مخصصة</button>
        </div>
    </div>

    <div class="card" style="padding: 0; overflow: hidden;">
        <div style="padding: 14px 20px; background: rgba(255,255,255,0.02); border-bottom: 1px solid var(--card-border); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span style="font-size: 13.5px; font-weight: 800; color: #fff;">المباريات المسجلة: <span style="color: var(--gold);"><?= count($enrichedMatches) ?></span></span>
                <span class="badge" style="background: rgba(48,209,88,0.1); color: var(--green); border-color: rgba(48,209,88,0.3); font-size: 11px;">تحديث متزامن مع التطبيق</span>
            </div>
            <input type="text" id="matchSearchInput" onkeyup="filterMatchesTable()" placeholder="🔍 بحث سريع عن فريق، بطولة أو معلق..." class="form-control" style="max-width: 320px; padding: 8px 14px;">
        </div>

        <div id="matchesListContainer">
            <?php if (empty($enrichedMatches)): ?>
                <div style="padding: 50px 20px; text-align: center; color: var(--text-muted);">
                    <div style="font-size: 40px; margin-bottom: 10px;">📅</div>
                    <p style="font-size: 16px; font-weight: 700; color: #fff; margin-bottom: 6px;">لا توجد مباريات مسجلة لهذا التاريخ (<?= $targetDate ?>)</p>
                    <p style="font-size: 13px; max-width: 450px; margin: 0 auto 16px;">يمكنك إضافة مباراة جديدة بضغطة زر وتعيين سيرفرات البث، أو استخدام مستكشف الـ API الرياضي لجلب المباريات فورياً.</p>
                    <div style="display: flex; justify-content: center; gap: 10px;">
                        <button class="btn btn-blue" onclick="openCustomMatchModal()">➕ إضافة مباراة جديدة</button>
                        <button class="btn btn-outline" onclick="switchMainTab('football-api')">🌐 استيراد من الـ API الرياضي</button>
                    </div>
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
                    $chName = $m['channel_name'] ?? 'beIN SPORTS';
                    $commentator = $m['commentator'] ?? 'تعليق عربي';
                ?>
                <div class="match-row" data-search="<?= htmlspecialchars($hTeam . ' ' . $aTeam . ' ' . $comp . ' ' . $chName . ' ' . $commentator) ?>">
                    <div>
                        <span style="font-size: 11.5px; color: var(--text-muted); display: block; font-weight: 600;"><?= htmlspecialchars($comp) ?></span>
                        <div style="display: flex; align-items: center; gap: 6px; margin-top: 2px;">
                            <span style="font-weight: 800; font-size: 14px; color: var(--gold);"><?= $time ?></span>
                            <?php if ($isLive): ?>
                                <span class="badge" style="background: rgba(255,69,58,0.2); color: var(--red); border-color: var(--red); font-size: 10px; padding: 2px 8px;">مباشر <?= htmlspecialchars($m['score_time'] ?? '') ?></span>
                            <?php endif; ?>
                        </div>
                        <span style="font-size: 11px; color: #7f8b9e; display: block; margin-top: 2px;">📺 <?= htmlspecialchars($chName) ?></span>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: flex-end; gap: 10px;">
                        <span style="font-weight: 800; font-size: 14px;"><?= htmlspecialchars($hTeam) ?></span>
                        <?php if ($hLogo): ?>
                            <img src="<?= htmlspecialchars($hLogo) ?>" style="width: 28px; height: 28px; object-fit: contain; border-radius: 4px; background: rgba(255,255,255,0.03);" onerror="this.style.display='none'">
                        <?php endif; ?>
                    </div>

                    <div style="text-align: center;">
                        <span style="background: rgba(255,255,255,0.07); border: 1px solid rgba(255,255,255,0.1); padding: 5px 14px; border-radius: 8px; font-weight: 900; font-size: 15px; letter-spacing: 2px; color: #fff;">
                            <?= $hScore ?> : <?= $aScore ?>
                        </span>
                    </div>

                    <div style="display: flex; align-items: center; justify-content: flex-start; gap: 10px;">
                        <?php if ($aLogo): ?>
                            <img src="<?= htmlspecialchars($aLogo) ?>" style="width: 28px; height: 28px; object-fit: contain; border-radius: 4px; background: rgba(255,255,255,0.03);" onerror="this.style.display='none'">
                        <?php endif; ?>
                        <span style="font-weight: 800; font-size: 14px;"><?= htmlspecialchars($aTeam) ?></span>
                    </div>

                    <div style="display: flex; gap: 6px; justify-content: flex-end; align-items: center; flex-wrap: wrap;">
                        <button class="btn <?= $isStreamActive ? 'btn-green' : 'btn-outline' ?>" style="padding: 6px 11px; font-size: 11.5px;" onclick="toggleMatchStream('<?= $mId ?>', <?= $isStreamActive ? 'false' : 'true' ?>)">
                            <?= $isStreamActive ? '⚡ البث شغال' : 'تفعيل البث' ?>
                        </button>
                        <button class="btn btn-blue" style="padding: 6px 11px; font-size: 11.5px;" onclick='openServersModal("<?= $mId ?>", "<?= htmlspecialchars($hTeam . ' ضد ' . $aTeam, ENT_QUOTES) ?>", <?= json_encode($m['servers'] ?? []) ?>)'>
                            📡 السيرفرات (<?= $serversCount ?>)
                        </button>
                        <button class="btn btn-outline" style="padding: 6px 9px; font-size: 11.5px;" title="تعديل النتيجة والبيانات" onclick='openEditMatchModal("<?= $mId ?>", <?= json_encode($m) ?>)'>✏️</button>
                        <button class="btn btn-outline" style="padding: 6px 9px; font-size: 11.5px;" title="تحديث النتيجة فورياً" onclick="quickScorePrompt('<?= $mId ?>', '<?= $hScore ?>', '<?= $aScore ?>')">⚡ النتيجة</button>
                        <button class="btn btn-outline" style="padding: 6px 9px; font-size: 11.5px;" title="نسخ المباراة" onclick="cloneMatch('<?= $mId ?>')">📋</button>
                        <button class="btn btn-red" style="padding: 6px 9px; font-size: 11.5px;" title="حذف المباراة" onclick="deleteMatch('<?= $mId ?>')">🗑️</button>
                    </div>
                </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </div>
</div>
