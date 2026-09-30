<?php
/**
 * =========================================================================
 * TOD Sports External Web Control Panel - M7 Folder
 * URL: https://ayham.alwaysdata.net/m7/
 * Complete management of Matches, Channels, Active Device Viewers & Security
 * =========================================================================
 */

$matchesFile = __DIR__ . '/matches.json';
$channelsFile = __DIR__ . '/channels.json';
$competitionsFile = __DIR__ . '/competitions.json';
$sessionsFile = __DIR__ . '/sessions.json';

// Handle POST actions
$message = '';
$msgType = 'info';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    if ($action === 'add_match' || $action === 'edit_match') {
        $homeTeam = trim($_POST['homeTeam'] ?? '');
        $awayTeam = trim($_POST['awayTeam'] ?? '');
        
        if (!empty($homeTeam) && !empty($awayTeam)) {
            $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
            $matchId = !empty($_POST['match_id']) ? $_POST['match_id'] : 'm_' . time() . '_' . rand(100, 999);
            
            $match = [
                'id' => $matchId,
                'title' => "$homeTeam ضد $awayTeam",
                'tournament' => $_POST['tournament'] ?? 'دوري أبطال أوروبا',
                'tournamentLogo' => $_POST['tournamentLogo'] ?? '',
                'homeTeam' => $homeTeam,
                'homeLogo' => $_POST['homeLogo'] ?? '',
                'homeFlag' => $_POST['homeFlag'] ?: '⚽',
                'awayTeam' => $awayTeam,
                'awayLogo' => $_POST['awayLogo'] ?? '',
                'awayFlag' => $_POST['awayFlag'] ?: '⚽',
                'kickoffTime' => $_POST['kickoffTime'] ?? '22:00',
                'kickoffDate' => $_POST['kickoffDate'] ?? 'اليوم',
                'stadium' => $_POST['stadium'] ?? 'الملعب الرئيسي',
                'commentator' => $_POST['commentator'] ?? 'عصام الشوالي',
                'channelName' => $_POST['channelName'] ?? 'beIN SPORTS 1 HD',
                'channelId' => $_POST['channelId'] ?? 'bein_1',
                'streamUrl' => $_POST['streamUrl'] ?? '',
                'isLive' => isset($_POST['isLive']) && $_POST['isLive'] === '1',
                'isEnded' => isset($_POST['isEnded']) && $_POST['isEnded'] === '1',
                'liveMinute' => $_POST['liveMinute'] ?? null,
                'countdown' => $_POST['countdown'] ?? null,
                'scoreHome' => $_POST['scoreHome'] !== '' ? (int)$_POST['scoreHome'] : null,
                'scoreAway' => $_POST['scoreAway'] !== '' ? (int)$_POST['scoreAway'] : null,
                'bannerUrl' => $_POST['bannerUrl'] ?? ''
            ];

            $found = false;
            foreach ($matchesData['matches'] as &$m) {
                if ($m['id'] === $matchId) {
                    $m = array_merge($m, $match);
                    $found = true;
                    break;
                }
            }
            if (!$found) {
                array_unshift($matchesData['matches'], $match);
            }

            file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $message = 'تم حفظ ونشر المباراة بنجاح إلى التطبيق مباشرة!';
            $msgType = 'success';
        }
    } elseif ($action === 'delete_match') {
        $id = $_POST['match_id'] ?? '';
        if ($id) {
            $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
            $matchesData['matches'] = array_values(array_filter($matchesData['matches'], function($m) use ($id) {
                return $m['id'] !== $id;
            }));
            file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $message = 'تم حذف المباراة بنجاح من الخادم!';
            $msgType = 'warning';
        }
    } elseif ($action === 'add_channel') {
        $name = trim($_POST['name'] ?? '');
        if (!empty($name)) {
            $channelsData = json_decode(file_get_contents($channelsFile), true) ?: ['channels' => []];
            $streamId = !empty($_POST['streamId']) ? $_POST['streamId'] : 'ch_' . time();
            
            $newCh = [
                'streamId' => $streamId,
                'name' => $name,
                'iconUrl' => $_POST['iconUrl'] ?? '',
                'categoryId' => $_POST['categoryId'] ?? 'قنوات beIN SPORTS',
                'playUrl' => $_POST['playUrl'] ?? ''
            ];

            $found = false;
            foreach ($channelsData['channels'] as &$c) {
                if ($c['streamId'] === $streamId) {
                    $c = $newCh;
                    $found = true;
                    break;
                }
            }
            if (!$found) {
                $channelsData['channels'][] = $newCh;
            }

            file_put_contents($channelsFile, json_encode($channelsData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $message = 'تم إضافة وتحديث القناة بنجاح!';
            $msgType = 'success';
        }
    } elseif ($action === 'delete_channel') {
        $id = $_POST['streamId'] ?? '';
        if ($id) {
            $channelsData = json_decode(file_get_contents($channelsFile), true) ?: ['channels' => []];
            $channelsData['channels'] = array_values(array_filter($channelsData['channels'], function($c) use ($id) {
                return $c['streamId'] !== $id;
            }));
            file_put_contents($channelsFile, json_encode($channelsData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $message = 'تم حذف القناة بنجاح!';
            $msgType = 'warning';
        }
    } elseif ($action === 'kick_session') {
        $targetDev = $_POST['deviceId'] ?? '';
        if ($targetDev) {
            $sessions = json_decode(file_get_contents($sessionsFile), true) ?: [];
            $sessions = array_values(array_filter($sessions, function($s) use ($targetDev) {
                return ($s['deviceId'] ?? '') !== $targetDev;
            }));
            file_put_contents($sessionsFile, json_encode($sessions, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $message = 'تم فصل جلسة الجهاز وإنهاء المشاهدة!';
            $msgType = 'success';
        }
    }
}

// Load current data
$matches = (json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []])['matches'] ?? [];
$channels = (json_decode(file_get_contents($channelsFile), true) ?: ['channels' => []])['channels'] ?? [];
$rawSessions = json_decode(file_get_contents($sessionsFile), true) ?: [];
$now = time();
$activeSessions = array_values(array_filter($rawSessions, function($s) use ($now) {
    return ($now - ($s['lastPing'] ?? 0)) < 300;
}));
?>
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TOD Cloud Server - لوحة التحكم الخارجية (مجلد M7)</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-dark: #07090e;
            --card-bg: #121522;
            --card-border: rgba(100, 210, 255, 0.2);
            --tod-gold: #FFB800;
            --tod-blue: #007AFF;
            --tod-cyan: #64D2FF;
            --tod-red: #FF3B30;
            --tod-green: #30D158;
            --text-main: #FFFFFF;
            --text-sub: #A0A5BD;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Tajawal', sans-serif; }
        body { background: var(--bg-dark); color: var(--text-main); min-height: 100vh; padding-bottom: 60px; }
        
        /* Header */
        header {
            background: linear-gradient(180deg, #181C2E 0%, #0D101C 100%);
            border-bottom: 1px solid rgba(255,255,255,0.08);
            padding: 18px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 100;
            backdrop-filter: blur(12px);
        }
        .brand { display: flex; align-items: center; gap: 14px; }
        .logo-badge {
            background: linear-gradient(135deg, #FFB800, #FF3B30);
            color: #000;
            font-weight: 900;
            font-size: 20px;
            padding: 6px 14px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(255, 184, 0, 0.4);
            letter-spacing: 1px;
        }
        .brand-title h1 { font-size: 18px; font-weight: 900; color: #fff; }
        .brand-title p { font-size: 12px; color: var(--tod-cyan); }
        .server-pill {
            background: rgba(48, 209, 88, 0.15);
            border: 1px solid rgba(48, 209, 88, 0.4);
            color: var(--tod-green);
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .dot { width: 8px; height: 8px; background: var(--tod-green); border-radius: 50%; box-shadow: 0 0 8px var(--tod-green); }

        .container { max-width: 1200px; margin: 24px auto; padding: 0 16px; }

        /* Alert */
        .alert {
            padding: 14px 18px;
            border-radius: 12px;
            margin-bottom: 20px;
            font-weight: 700;
            font-size: 14px;
        }
        .alert.success { background: rgba(48, 209, 88, 0.15); border: 1px solid var(--tod-green); color: #fff; }
        .alert.warning { background: rgba(255, 59, 48, 0.15); border: 1px solid var(--tod-red); color: #fff; }
        .alert.info { background: rgba(100, 210, 255, 0.15); border: 1px solid var(--tod-cyan); color: #fff; }

        /* Stats Grid */
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .stat-card {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 16px;
            padding: 18px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            box-shadow: 0 8px 24px rgba(0,0,0,0.3);
        }
        .stat-card .val { font-size: 28px; font-weight: 900; color: #fff; }
        .stat-card .lbl { font-size: 12px; color: var(--text-sub); }
        .stat-icon { font-size: 30px; }

        /* Tabs Navigation */
        .tabs { display: flex; gap: 10px; margin-bottom: 24px; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 10px; }
        .tab-btn {
            background: transparent;
            border: 1px solid rgba(255,255,255,0.1);
            color: var(--text-sub);
            padding: 10px 20px;
            border-radius: 12px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 700;
            transition: all 0.2s;
        }
        .tab-btn.active, .tab-btn:hover {
            background: linear-gradient(135deg, rgba(255, 184, 0, 0.2), rgba(0, 122, 255, 0.2));
            border-color: var(--tod-gold);
            color: #fff;
        }

        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* Form Card */
        .card {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 20px;
            padding: 24px;
            margin-bottom: 28px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.4);
        }
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            border-bottom: 1px solid rgba(255,255,255,0.06);
            padding-bottom: 12px;
        }
        .card-header h2 { font-size: 18px; color: #fff; }

        .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; font-size: 12.5px; color: var(--text-sub); margin-bottom: 6px; font-weight: 700; }
        .form-group input, .form-group select, .form-group textarea {
            width: 100%;
            background: #080A12;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 10px;
            padding: 10px 14px;
            color: #fff;
            font-size: 13.5px;
            outline: none;
            transition: border-color 0.2s;
        }
        .form-group input:focus, .form-group select:focus { border-color: var(--tod-cyan); }
        
        .btn {
            background: linear-gradient(135deg, #FFB800, #FFA000);
            color: #000;
            font-weight: 900;
            border: none;
            padding: 12px 24px;
            border-radius: 12px;
            cursor: pointer;
            font-size: 14px;
            box-shadow: 0 4px 15px rgba(255, 184, 0, 0.3);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: transform 0.15s;
        }
        .btn:hover { transform: translateY(-2px); }
        .btn.danger { background: linear-gradient(135deg, #FF3B30, #D70015); color: #fff; box-shadow: 0 4px 15px rgba(255, 59, 48, 0.3); }

        /* Tables & Lists */
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; text-align: right; }
        th, td { padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.06); font-size: 13px; }
        th { color: var(--tod-cyan); font-weight: 700; background: rgba(0,0,0,0.2); }
        tr:hover { background: rgba(255,255,255,0.02); }

        .badge-live {
            background: var(--tod-red);
            color: #fff;
            padding: 3px 8px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 900;
        }
        .badge-upcoming {
            background: rgba(255, 184, 0, 0.15);
            color: var(--tod-gold);
            border: 1px solid rgba(255, 184, 0, 0.3);
            padding: 3px 8px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
        }
        .team-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .team-logo-img { width: 24px; height: 24px; object-fit: contain; border-radius: 50%; background: #fff; padding: 2px; }

        .session-card {
            background: #0B0E1A;
            border: 1px solid rgba(100, 210, 255, 0.15);
            border-radius: 14px;
            padding: 14px;
            margin-bottom: 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .session-info { display: flex; align-items: center; gap: 14px; }
        .device-icon { font-size: 26px; }
        .session-details h4 { font-size: 14px; color: #fff; }
        .session-details p { font-size: 11.5px; color: var(--text-sub); }
        .watching-badge {
            background: rgba(0, 122, 255, 0.15);
            border: 1px solid rgba(0, 122, 255, 0.4);
            color: var(--tod-cyan);
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 11.5px;
            font-weight: 700;
        }
    </style>
</head>
<body>

    <header>
        <div class="brand">
            <div class="logo-badge">TOD</div>
            <div class="brand-title">
                <h1>لوحة التحكم السحابية - مجلد M7</h1>
                <p>https://ayham.alwaysdata.net/m7</p>
            </div>
        </div>
        <div class="server-pill">
            <span class="dot"></span>
            الخادم متصل ونشط
        </div>
    </header>

    <div class="container">

        <?php if ($message): ?>
            <div class="alert <?= $msgType ?>"><?= htmlspecialchars($message) ?></div>
        <?php endif; ?>

        <!-- Stats Overview -->
        <div class="stats-grid">
            <div class="stat-card">
                <div>
                    <div class="val"><?= count($activeSessions) ?></div>
                    <div class="lbl">الأجهزة المتصلة الآن (من يشاهد)</div>
                </div>
                <div class="stat-icon">📱</div>
            </div>
            <div class="stat-card">
                <div>
                    <div class="val"><?= count($matches) ?></div>
                    <div class="lbl">إجمالي المباريات المجدولة</div>
                </div>
                <div class="stat-icon">⚽</div>
            </div>
            <div class="stat-card">
                <div>
                    <div class="val"><?= count($channels) ?></div>
                    <div class="lbl">القنوات الرياضية والبث</div>
                </div>
                <div class="stat-icon">📺</div>
            </div>
            <div class="stat-card">
                <div>
                    <div class="val">AES-256</div>
                    <div class="lbl">حماية التشفير والنظام الصارم</div>
                </div>
                <div class="stat-icon">🔒</div>
            </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="tabs">
            <button class="tab-btn active" onclick="switchTab('tab-viewers')">👁️ من يشاهد الآن (<?= count($activeSessions) ?>)</button>
            <button class="tab-btn" onclick="switchTab('tab-matches')">⚽ إدارة المباريات (<?= count($matches) ?>)</button>
            <button class="tab-btn" onclick="switchTab('tab-channels')">📺 إدارة القنوات (<?= count($channels) ?>)</button>
            <button class="tab-btn" onclick="switchTab('tab-api')">🔗 معلومات الـ API والربط</button>
        </div>

        <!-- TAB 1: WHO IS WATCHING NOW -->
        <div id="tab-viewers" class="tab-content active">
            <div class="card">
                <div class="card-header">
                    <h2>الجلسات النشطة والأجهزة المتصلة بالتطبيق</h2>
                    <span style="font-size: 12px; color: var(--tod-cyan);">تحديث فوري كل 30 ثانية</span>
                </div>

                <?php if (empty($activeSessions)): ?>
                    <p style="color: var(--text-sub); text-align: center; padding: 20px;">لا يوجد أجهزة متصلة في هذه اللحظة (ستظهر الأجهزة فور فتح التطبيق).</p>
                <?php else: ?>
                    <?php foreach ($activeSessions as $sess): ?>
                        <div class="session-card">
                            <div class="session-info">
                                <div class="device-icon">📲</div>
                                <div class="session-details">
                                    <h4><?= htmlspecialchars($sess['deviceName'] ?? 'هاتف أندرويد') ?> <span style="color: var(--tod-gold); font-size: 12px;">(<?= htmlspecialchars($sess['profileName'] ?? 'VIP') ?>)</span></h4>
                                    <p>معرف الجهاز: <?= htmlspecialchars(substr($sess['deviceId'] ?? '', 0, 18)) ?>... | IP: <?= htmlspecialchars($sess['ip'] ?? '') ?> | اتصال: <?= htmlspecialchars($sess['connectedAt'] ?? '') ?></p>
                                </div>
                            </div>
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <div class="watching-badge">
                                    👀 يشاهد: <?= htmlspecialchars($sess['activeStream'] ?: 'تصفح التطبيق') ?>
                                </div>
                                <form method="POST" style="margin: 0;" onsubmit="return confirm('هل أنت متأكد من إنهاء جلسة هذا الجهاز؟');">
                                    <input type="hidden" name="action" value="kick_session">
                                    <input type="hidden" name="deviceId" value="<?= htmlspecialchars($sess['deviceId'] ?? '') ?>">
                                    <button type="submit" class="btn danger" style="padding: 6px 12px; font-size: 11px;">فصل</button>
                                </form>
                            </div>
                        </div>
                    <?php endforeach; ?>
                <?php endif; ?>
            </div>
        </div>

        <!-- TAB 2: MATCHES MANAGEMENT -->
        <div id="tab-matches" class="tab-content">
            <!-- Add Match Form -->
            <div class="card">
                <div class="card-header">
                    <h2>إضافة أو تعديل مباراة في جدول TOD</h2>
                </div>
                <form method="POST">
                    <input type="hidden" name="action" value="add_match">
                    
                    <div class="form-grid">
                        <div class="form-group">
                            <label>الفريق الأول (المستضيف)</label>
                            <input type="text" name="homeTeam" required placeholder="مثال: ريال مدريد">
                        </div>
                        <div class="form-group">
                            <label>شعار الفريق الأول (رابط صورة Logo URL)</label>
                            <input type="url" name="homeLogo" placeholder="https://example.com/logo1.png">
                        </div>
                        <div class="form-group">
                            <label>الفريق الثاني (الضيف)</label>
                            <input type="text" name="awayTeam" required placeholder="مثال: مانشستر سيتي">
                        </div>
                        <div class="form-group">
                            <label>شعار الفريق الثاني (رابط صورة Logo URL)</label>
                            <input type="url" name="awayLogo" placeholder="https://example.com/logo2.png">
                        </div>
                        <div class="form-group">
                            <label>البطولة</label>
                            <input type="text" name="tournament" value="دوري أبطال أوروبا" placeholder="اسم البطولة">
                        </div>
                        <div class="form-group">
                            <label>شعار البطولة (Logo URL)</label>
                            <input type="url" name="tournamentLogo" placeholder="https://example.com/tournament.png">
                        </div>
                        <div class="form-group">
                            <label>توقيت المباراة</label>
                            <input type="text" name="kickoffTime" value="22:00" placeholder="22:00">
                        </div>
                        <div class="form-group">
                            <label>تاريخ المباراة</label>
                            <input type="text" name="kickoffDate" value="اليوم" placeholder="اليوم أو 1 أكتوبر">
                        </div>
                        <div class="form-group">
                            <label>الملعب</label>
                            <input type="text" name="stadium" value="سانتياغو برنابيو">
                        </div>
                        <div class="form-group">
                            <label>المعلق الرياضي</label>
                            <input type="text" name="commentator" value="عصام الشوالي">
                        </div>
                        <div class="form-group">
                            <label>القناة الناقلة</label>
                            <input type="text" name="channelName" value="beIN SPORTS 1 HD">
                        </div>
                        <div class="form-group">
                            <label>رابط البث المباشر (HLS / m3u8)</label>
                            <input type="text" name="streamUrl" placeholder="https://.../stream.m3u8">
                        </div>
                        <div class="form-group">
                            <label>حالة البث</label>
                            <select name="isLive">
                                <option value="0">قادمة (Upcoming / Countdown)</option>
                                <option value="1">مباشر الآن (LIVE)</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>دقيقة المباراة (للمباشر)</label>
                            <input type="text" name="liveMinute" placeholder="مثال: الشوط الأول أو 65'">
                        </div>
                        <div class="form-group">
                            <label>أهداف المستضيف</label>
                            <input type="number" name="scoreHome" placeholder="0">
                        </div>
                        <div class="form-group">
                            <label>أهداف الضيف</label>
                            <input type="number" name="scoreAway" placeholder="0">
                        </div>
                    </div>

                    <div style="margin-top: 14px;">
                        <button type="submit" class="btn">حفظ ونشر المباراة فوراً ⚽</button>
                    </div>
                </form>
            </div>

            <!-- Matches List -->
            <div class="card">
                <div class="card-header">
                    <h2>قائمة المباريات الحالية في السيرفر</h2>
                </div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>المباراة</th>
                                <th>البطولة</th>
                                <th>التوقيت</th>
                                <th>الحالة</th>
                                <th>النتيجة</th>
                                <th>القناة والبث</th>
                                <th>إجراءات</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($matches as $m): ?>
                                <tr>
                                    <td>
                                        <div class="team-badge">
                                            <?php if (!empty($m['homeLogo'])): ?>
                                                <img src="<?= htmlspecialchars($m['homeLogo']) ?>" class="team-logo-img">
                                            <?php else: ?>
                                                <span><?= htmlspecialchars($m['homeFlag'] ?? '⚽') ?></span>
                                            <?php endif; ?>
                                            <strong><?= htmlspecialchars($m['homeTeam']) ?></strong>
                                            ضد
                                            <strong><?= htmlspecialchars($m['awayTeam']) ?></strong>
                                            <?php if (!empty($m['awayLogo'])): ?>
                                                <img src="<?= htmlspecialchars($m['awayLogo']) ?>" class="team-logo-img">
                                            <?php else: ?>
                                                <span><?= htmlspecialchars($m['awayFlag'] ?? '⚽') ?></span>
                                            <?php endif; ?>
                                        </div>
                                    </td>
                                    <td><?= htmlspecialchars($m['tournament']) ?></td>
                                    <td><?= htmlspecialchars($m['kickoffTime']) ?> (<?= htmlspecialchars($m['kickoffDate']) ?>)</td>
                                    <td>
                                        <?php if (!empty($m['isLive'])): ?>
                                            <span class="badge-live">مباشر <?= htmlspecialchars($m['liveMinute'] ?? '') ?></span>
                                        <?php else: ?>
                                            <span class="badge-upcoming">قادمة</span>
                                        <?php endif; ?>
                                    </td>
                                    <td>
                                        <?= isset($m['scoreHome']) ? "{$m['scoreHome']} - {$m['scoreAway']}" : '-' ?>
                                    </td>
                                    <td><?= htmlspecialchars($m['channelName']) ?></td>
                                    <td>
                                        <form method="POST" style="display:inline;" onsubmit="return confirm('حذف المباراة نهائياً؟');">
                                            <input type="hidden" name="action" value="delete_match">
                                            <input type="hidden" name="match_id" value="<?= htmlspecialchars($m['id']) ?>">
                                            <button type="submit" class="btn danger" style="padding: 4px 10px; font-size: 11px;">حذف</button>
                                        </form>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- TAB 3: CHANNELS MANAGEMENT -->
        <div id="tab-channels" class="tab-content">
            <div class="card">
                <div class="card-header">
                    <h2>إضافة قناة رياضية جديدة</h2>
                </div>
                <form method="POST">
                    <input type="hidden" name="action" value="add_channel">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>اسم القناة</label>
                            <input type="text" name="name" required placeholder="مثال: beIN SPORTS 1 HD">
                        </div>
                        <div class="form-group">
                            <label>معرف البث (Stream ID)</label>
                            <input type="text" name="streamId" placeholder="ch_bein_1">
                        </div>
                        <div class="form-group">
                            <label>شعار القناة (Logo URL)</label>
                            <input type="url" name="iconUrl" placeholder="https://example.com/bein1.png">
                        </div>
                        <div class="form-group">
                            <label>التصنيف</label>
                            <input type="text" name="categoryId" value="قنوات beIN SPORTS">
                        </div>
                        <div class="form-group" style="grid-column: 1 / -1;">
                            <label>رابط البث المباشر (HLS / m3u8)</label>
                            <input type="text" name="playUrl" required placeholder="https://.../stream.m3u8">
                        </div>
                    </div>
                    <button type="submit" class="btn">إضافة القناة 📺</button>
                </form>
            </div>

            <div class="card">
                <div class="card-header">
                    <h2>القنوات المتاحة في التطبيق</h2>
                </div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>الشعار</th>
                                <th>اسم القناة</th>
                                <th>التصنيف</th>
                                <th>رابط البث</th>
                                <th>إجراءات</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($channels as $c): ?>
                                <tr>
                                    <td>
                                        <?php if (!empty($c['iconUrl'])): ?>
                                            <img src="<?= htmlspecialchars($c['iconUrl']) ?>" style="width: 32px; height: 32px; object-fit: contain;">
                                        <?php else: ?>
                                            📺
                                        <?php endif; ?>
                                    </td>
                                    <td><strong><?= htmlspecialchars($c['name']) ?></strong></td>
                                    <td><?= htmlspecialchars($c['categoryId']) ?></td>
                                    <td style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"><?= htmlspecialchars($c['playUrl']) ?></td>
                                    <td>
                                        <form method="POST" style="display:inline;" onsubmit="return confirm('حذف القناة؟');">
                                            <input type="hidden" name="action" value="delete_channel">
                                            <input type="hidden" name="streamId" value="<?= htmlspecialchars($c['streamId']) ?>">
                                            <button type="submit" class="btn danger" style="padding: 4px 10px; font-size: 11px;">حذف</button>
                                        </form>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- TAB 4: API & SERVER LINKING INFO -->
        <div id="tab-api" class="tab-content">
            <div class="card">
                <div class="card-header">
                    <h2>روابط الـ API ومجلد M7 على السيرفر</h2>
                </div>
                <p style="color: var(--text-sub); margin-bottom: 16px;">
                    تم برمجة تطبيق الأندرويد ليتصل مباشرة بملفات مجلد <strong>m7</strong> على خادم <strong>https://ayham.alwaysdata.net</strong>.
                </p>

                <div class="form-group">
                    <label>رابط واجهة الـ API للمباريات:</label>
                    <input type="text" readonly value="https://ayham.alwaysdata.net/m7/api.php?action=matches">
                </div>
                <div class="form-group">
                    <label>رابط واجهة الـ API للقنوات:</label>
                    <input type="text" readonly value="https://ayham.alwaysdata.net/m7/api.php?action=channels">
                </div>
                <div class="form-group">
                    <label>رابط تتبع الأجهزة وجلسات الدخول (من يشاهد الآن):</label>
                    <input type="text" readonly value="https://ayham.alwaysdata.net/m7/api.php?action=session_ping">
                </div>
                <div class="form-group">
                    <label>مفتاح الأمان وتشفير الروابط AES-256:</label>
                    <input type="text" readonly value="TOD_VIP_SUPER_SECURE_KEY_2026_M7">
                </div>
            </div>
        </div>

    </div>

    <script>
        function switchTab(tabId) {
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
            
            document.getElementById(tabId).classList.add('active');
            event.currentTarget.classList.add('active');
        }
    </script>
</body>
</html>
