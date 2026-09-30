<?php
/**
 * =========================================================================
 * TOD Sports External Web Control Panel - M7 Edition
 * URL: https://ayham.alwaysdata.net/m7/
 * Real-time match scoring, live channels, device sessions & stream control
 * =========================================================================
 */

$matchesFile = __DIR__ . '/matches.json';
$channelsFile = __DIR__ . '/channels.json';
$competitionsFile = __DIR__ . '/competitions.json';
$sessionsFile = __DIR__ . '/sessions.json';

// Handle Direct AJAX POST requests
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest') {
    header('Content-Type: application/json; charset=utf-8');
    $action = $_POST['action'] ?? '';

    if ($action === 'quick_score') {
        $id = $_POST['id'] ?? '';
        $deltaHome = (int)($_POST['deltaHome'] ?? 0);
        $deltaAway = (int)($_POST['deltaAway'] ?? 0);

        $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
        $updated = false;
        foreach ($matchesData['matches'] as &$m) {
            if ($m['id'] === $id) {
                $curHome = isset($m['scoreHome']) ? (int)$m['scoreHome'] : 0;
                $curAway = isset($m['scoreAway']) ? (int)$m['scoreAway'] : 0;
                $m['scoreHome'] = max(0, $curHome + $deltaHome);
                $m['scoreAway'] = max(0, $curAway + $deltaAway);
                $updated = true;
                break;
            }
        }
        if ($updated) {
            file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(['status' => 'success', 'message' => 'تم تحديث النتيجة فورياً!']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'المباراة غير موجودة']);
        }
        exit();
    } elseif ($action === 'toggle_live') {
        $id = $_POST['id'] ?? '';
        $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
        $newState = false;
        foreach ($matchesData['matches'] as &$m) {
            if ($m['id'] === $id) {
                $m['isLive'] = !($m['isLive'] ?? false);
                $newState = $m['isLive'];
                break;
            }
        }
        file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        echo json_encode(['status' => 'success', 'isLive' => $newState]);
        exit();
    } elseif ($action === 'quick_minute') {
        $id = $_POST['id'] ?? '';
        $minute = trim($_POST['liveMinute'] ?? '');
        $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
        foreach ($matchesData['matches'] as &$m) {
            if ($m['id'] === $id) {
                $m['liveMinute'] = $minute;
                break;
            }
        }
        file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        echo json_encode(['status' => 'success']);
        exit();
    }
}

// Handle Traditional POST Actions
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
                'tournament' => $_POST['tournament'] ?: 'دوري أبطال أوروبا',
                'tournamentLogo' => $_POST['tournamentLogo'] ?? '',
                'homeTeam' => $homeTeam,
                'homeLogo' => $_POST['homeLogo'] ?? '',
                'homeFlag' => $_POST['homeFlag'] ?: '⚽',
                'awayTeam' => $awayTeam,
                'awayLogo' => $_POST['awayLogo'] ?? '',
                'awayFlag' => $_POST['awayFlag'] ?: '⚽',
                'kickoffTime' => $_POST['kickoffTime'] ?: '22:00',
                'kickoffDate' => $_POST['kickoffDate'] ?: 'اليوم',
                'stadium' => $_POST['stadium'] ?: 'الملعب الرئيسي',
                'commentator' => $_POST['commentator'] ?: 'عصام الشوالي',
                'channelName' => $_POST['channelName'] ?: 'beIN SPORTS 1 HD',
                'channelId' => $_POST['channelId'] ?: 'bein_1',
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
            $message = 'تم حذف المباراة بنجاح من الخادم والتطبيق!';
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
                'categoryId' => $_POST['categoryId'] ?: 'قنوات beIN SPORTS',
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
            $message = 'تم إضافة وتحديث القناة الرياضية بنجاح!';
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
            $message = 'تم إنهاء جلسة الجهاز وفصله عن البث!';
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
    <title>TOD Sports Live Server Dashboard - M7</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-dark: #07090e;
            --glass-bg: rgba(18, 22, 36, 0.75);
            --card-border: rgba(100, 210, 255, 0.22);
            --tod-gold: #FFB800;
            --tod-blue: #007AFF;
            --tod-cyan: #64D2FF;
            --tod-red: #FF3B30;
            --tod-green: #30D158;
            --text-main: #FFFFFF;
            --text-sub: #A0A5BD;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Tajawal', sans-serif; }
        body {
            background: radial-gradient(circle at 50% 0%, #171c32 0%, #07090e 70%);
            color: var(--text-main);
            min-height: 100vh;
            padding-bottom: 80px;
        }

        /* Header Glass */
        header {
            background: rgba(13, 16, 28, 0.85);
            border-bottom: 1px solid rgba(255,255,255,0.1);
            padding: 18px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 100;
            backdrop-filter: blur(16px);
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

        .container { max-width: 1240px; margin: 24px auto; padding: 0 16px; }

        /* Alert */
        .alert {
            padding: 14px 18px;
            border-radius: 12px;
            margin-bottom: 20px;
            font-weight: 700;
            font-size: 14px;
            backdrop-filter: blur(8px);
        }
        .alert.success { background: rgba(48, 209, 88, 0.18); border: 1px solid var(--tod-green); color: #fff; }
        .alert.warning { background: rgba(255, 59, 48, 0.18); border: 1px solid var(--tod-red); color: #fff; }
        .alert.info { background: rgba(100, 210, 255, 0.18); border: 1px solid var(--tod-cyan); color: #fff; }

        /* Stats Grid */
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .stat-card {
            background: var(--glass-bg);
            border: 1px solid var(--card-border);
            border-radius: 20px;
            padding: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            backdrop-filter: blur(16px);
            box-shadow: 0 8px 30px rgba(0,0,0,0.35);
        }
        .stat-card .val { font-size: 30px; font-weight: 900; color: #fff; }
        .stat-card .lbl { font-size: 12px; color: var(--text-sub); margin-top: 4px; }
        .stat-icon { font-size: 32px; }

        /* Tabs Navigation */
        .tabs { display: flex; gap: 10px; margin-bottom: 24px; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 12px; }
        .tab-btn {
            background: rgba(255,255,255,0.04);
            border: 1px solid rgba(255,255,255,0.1);
            color: var(--text-sub);
            padding: 11px 22px;
            border-radius: 14px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 700;
            transition: all 0.2s;
            backdrop-filter: blur(10px);
        }
        .tab-btn.active, .tab-btn:hover {
            background: linear-gradient(135deg, rgba(255, 184, 0, 0.25), rgba(0, 122, 255, 0.25));
            border-color: var(--tod-gold);
            color: #fff;
            box-shadow: 0 4px 20px rgba(255, 184, 0, 0.2);
        }

        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* Glass Cards */
        .card {
            background: var(--glass-bg);
            border: 1px solid var(--card-border);
            border-radius: 22px;
            padding: 24px;
            margin-bottom: 28px;
            backdrop-filter: blur(16px);
            box-shadow: 0 12px 40px rgba(0,0,0,0.45);
        }
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            border-bottom: 1px solid rgba(255,255,255,0.08);
            padding-bottom: 14px;
        }
        .card-header h2 { font-size: 18px; color: #fff; font-weight: 800; }

        .form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; font-size: 12.5px; color: var(--text-sub); margin-bottom: 6px; font-weight: 700; }
        .form-group input, .form-group select, .form-group textarea {
            width: 100%;
            background: rgba(8, 10, 18, 0.8);
            border: 1px solid rgba(255,255,255,0.14);
            border-radius: 12px;
            padding: 11px 14px;
            color: #fff;
            font-size: 13.5px;
            outline: none;
            transition: all 0.2s;
        }
        .form-group input:focus, .form-group select:focus { border-color: var(--tod-cyan); box-shadow: 0 0 10px rgba(100, 210, 255, 0.3); }
        
        .btn {
            background: linear-gradient(135deg, #FFB800, #FFA000);
            color: #000;
            font-weight: 900;
            border: none;
            padding: 12px 24px;
            border-radius: 12px;
            cursor: pointer;
            font-size: 14px;
            box-shadow: 0 4px 15px rgba(255, 184, 0, 0.35);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: transform 0.15s, box-shadow 0.15s;
        }
        .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 184, 0, 0.5); }
        .btn.danger { background: linear-gradient(135deg, #FF3B30, #D70015); color: #fff; box-shadow: 0 4px 15px rgba(255, 59, 48, 0.35); }
        .btn.secondary { background: rgba(255,255,255,0.1); color: #fff; border: 1px solid rgba(255,255,255,0.15); box-shadow: none; }
        .btn.small { padding: 6px 12px; font-size: 12px; border-radius: 8px; }

        /* Tables & Lists */
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; text-align: right; }
        th, td { padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.06); font-size: 13px; }
        th { color: var(--tod-cyan); font-weight: 700; background: rgba(0,0,0,0.3); }
        tr:hover { background: rgba(255,255,255,0.03); }

        .score-control {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: rgba(0,0,0,0.4);
            border: 1px solid rgba(255,255,255,0.15);
            padding: 4px 8px;
            border-radius: 10px;
        }
        .score-btn {
            background: rgba(255, 184, 0, 0.25);
            color: #FFB800;
            border: 1px solid rgba(255, 184, 0, 0.4);
            border-radius: 6px;
            width: 26px;
            height: 26px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            font-weight: 900;
            font-size: 15px;
        }
        .score-btn:hover { background: #FFB800; color: #000; }
        .score-num { font-size: 16px; font-weight: 900; color: #fff; min-width: 18px; text-align: center; }

        .badge-live {
            background: var(--tod-red);
            color: #fff;
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 11px;
            font-weight: 900;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .badge-upcoming {
            background: rgba(255, 184, 0, 0.15);
            color: var(--tod-gold);
            border: 1px solid rgba(255, 184, 0, 0.3);
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 11px;
            font-weight: 700;
            cursor: pointer;
        }

        .team-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        .team-logo-img { width: 26px; height: 26px; object-fit: contain; border-radius: 50%; background: #fff; padding: 2px; }

        .session-card {
            background: rgba(11, 14, 26, 0.85);
            border: 1px solid rgba(100, 210, 255, 0.2);
            border-radius: 16px;
            padding: 16px;
            margin-bottom: 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            backdrop-filter: blur(12px);
        }
        .session-info { display: flex; align-items: center; gap: 14px; }
        .device-icon { font-size: 28px; }
        .session-details h4 { font-size: 14px; color: #fff; font-weight: 800; }
        .session-details p { font-size: 11.5px; color: var(--text-sub); margin-top: 3px; }
        .watching-badge {
            background: rgba(0, 122, 255, 0.18);
            border: 1px solid rgba(0, 122, 255, 0.45);
            color: var(--tod-cyan);
            padding: 6px 12px;
            border-radius: 10px;
            font-size: 12px;
            font-weight: 700;
        }

        /* Toast notification */
        #toast {
            position: fixed;
            bottom: 24px;
            left: 24px;
            background: linear-gradient(135deg, #007AFF, #64D2FF);
            color: #fff;
            padding: 12px 24px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 14px;
            box-shadow: 0 8px 30px rgba(0, 122, 255, 0.4);
            display: none;
            z-index: 1000;
        }
    </style>
</head>
<body>

    <header>
        <div class="brand">
            <div class="logo-badge">TOD</div>
            <div class="brand-title">
                <h1>لوحة التحكم المباشرة - مجلد M7</h1>
                <p>https://ayham.alwaysdata.net/m7</p>
            </div>
        </div>
        <div class="server-pill">
            <span class="dot"></span>
            الخادم متصل (Real-time Live Sync)
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
                    <div class="val" id="activeViewersCount"><?= count($activeSessions) ?></div>
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
                    <div class="lbl">تشفير وحماية البث الفوري</div>
                </div>
                <div class="stat-icon">🔒</div>
            </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="tabs">
            <button class="tab-btn active" onclick="switchTab('tab-matches')">⚽ إدارة المباريات والنتائج المباشرة (<?= count($matches) ?>)</button>
            <button class="tab-btn" onclick="switchTab('tab-viewers')">👁️ من يشاهد الآن (<?= count($activeSessions) ?>)</button>
            <button class="tab-btn" onclick="switchTab('tab-channels')">📺 إدارة القنوات (<?= count($channels) ?>)</button>
            <button class="tab-btn" onclick="switchTab('tab-api')">🔗 معلومات الـ API والربط</button>
        </div>

        <!-- TAB 1: MATCHES MANAGEMENT (Real-time Instant Scoring) -->
        <div id="tab-matches" class="tab-content active">
            <!-- Matches List with Realtime Quick Scoring -->
            <div class="card">
                <div class="card-header">
                    <h2>قائمة المباريات (تحديث النتائج والبث فورياً في الوقت الفعلي)</h2>
                    <span style="font-size: 12px; color: var(--tod-gold);">⚡ النتيجة والمباشر تتحدث فوراً في تطبيق المستخدم!</span>
                </div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>المباراة والشعارات</th>
                                <th>البطولة والتوقيت</th>
                                <th>الحالة المباشرة</th>
                                <th>النتيجة المباشرة (تحكم فوري)</th>
                                <th>القناة والبث</th>
                                <th>إجراءات</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($matches as $m): ?>
                                <tr id="row-<?= htmlspecialchars($m['id']) ?>">
                                    <td>
                                        <div class="team-badge">
                                            <?php if (!empty($m['homeLogo'])): ?>
                                                <img src="<?= htmlspecialchars($m['homeLogo']) ?>" class="team-logo-img">
                                            <?php else: ?>
                                                <span><?= htmlspecialchars($m['homeFlag'] ?? '⚽') ?></span>
                                            <?php endif; ?>
                                            <strong><?= htmlspecialchars($m['homeTeam']) ?></strong>
                                            <span style="color: var(--tod-gold); font-size: 11px;">VS</span>
                                            <strong><?= htmlspecialchars($m['awayTeam']) ?></strong>
                                            <?php if (!empty($m['awayLogo'])): ?>
                                                <img src="<?= htmlspecialchars($m['awayLogo']) ?>" class="team-logo-img">
                                            <?php else: ?>
                                                <span><?= htmlspecialchars($m['awayFlag'] ?? '⚽') ?></span>
                                            <?php endif; ?>
                                        </div>
                                    </td>
                                    <td>
                                        <div><strong><?= htmlspecialchars($m['tournament']) ?></strong></div>
                                        <div style="color: var(--text-sub); font-size: 11.5px;"><?= htmlspecialchars($m['kickoffTime']) ?> | <?= htmlspecialchars($m['kickoffDate']) ?></div>
                                    </td>
                                    <td>
                                        <button class="<?= !empty($m['isLive']) ? 'badge-live' : 'badge-upcoming' ?>" onclick="toggleMatchLive('<?= htmlspecialchars($m['id']) ?>', this)">
                                            <?= !empty($m['isLive']) ? '🔴 مباشر الآن' : '⏳ قادمة' ?>
                                        </button>
                                        <div style="margin-top: 4px;">
                                            <input type="text" value="<?= htmlspecialchars($m['liveMinute'] ?? '') ?>" placeholder="الدقيقة" style="width: 75px; padding: 2px 6px; font-size: 11px; background: rgba(0,0,0,0.5); border: 1px solid rgba(255,255,255,0.2); border-radius: 6px; color:#fff;" onchange="updateMatchMinute('<?= htmlspecialchars($m['id']) ?>', this.value)">
                                        </div>
                                    </td>
                                    <td>
                                        <!-- Interactive Realtime Score Buttons -->
                                        <div class="score-control">
                                            <div style="display: flex; flex-direction: column; align-items: center; gap: 2px;">
                                                <div style="font-size: 10px; color: var(--tod-cyan);"><?= htmlspecialchars($m['homeTeam']) ?></div>
                                                <div style="display: flex; align-items: center; gap: 4px;">
                                                    <button class="score-btn" onclick="adjustScore('<?= htmlspecialchars($m['id']) ?>', 1, 0)">+</button>
                                                    <span class="score-num" id="score-home-<?= htmlspecialchars($m['id']) ?>"><?= $m['scoreHome'] !== null ? $m['scoreHome'] : '0' ?></span>
                                                    <button class="score-btn" onclick="adjustScore('<?= htmlspecialchars($m['id']) ?>', -1, 0)">-</button>
                                                </div>
                                            </div>

                                            <span style="font-weight: 900; color: var(--tod-gold); margin: 0 4px;">:</span>

                                            <div style="display: flex; flex-direction: column; align-items: center; gap: 2px;">
                                                <div style="font-size: 10px; color: var(--tod-gold);"><?= htmlspecialchars($m['awayTeam']) ?></div>
                                                <div style="display: flex; align-items: center; gap: 4px;">
                                                    <button class="score-btn" onclick="adjustScore('<?= htmlspecialchars($m['id']) ?>', 0, 1)">+</button>
                                                    <span class="score-num" id="score-away-<?= htmlspecialchars($m['id']) ?>"><?= $m['scoreAway'] !== null ? $m['scoreAway'] : '0' ?></span>
                                                    <button class="score-btn" onclick="adjustScore('<?= htmlspecialchars($m['id']) ?>', 0, -1)">-</button>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div><strong><?= htmlspecialchars($m['channelName']) ?></strong></div>
                                        <div style="color: var(--text-sub); font-size: 11px;">🎙️ <?= htmlspecialchars($m['commentator']) ?></div>
                                    </td>
                                    <td>
                                        <form method="POST" style="display:inline;" onsubmit="return confirm('حذف المباراة نهائياً من الخادم والتطبيق؟');">
                                            <input type="hidden" name="action" value="delete_match">
                                            <input type="hidden" name="match_id" value="<?= htmlspecialchars($m['id']) ?>">
                                            <button type="submit" class="btn danger small">حذف 🗑️</button>
                                        </form>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Add Match Form -->
            <div class="card">
                <div class="card-header">
                    <h2>إضافة مباراة جديدة إلى جدول TOD</h2>
                </div>
                <form method="POST">
                    <input type="hidden" name="action" value="add_match">
                    
                    <div class="form-grid">
                        <div class="form-group">
                            <label>الفريق الأول (المستضيف)</label>
                            <input type="text" name="homeTeam" required placeholder="مثال: ريال مدريد">
                        </div>
                        <div class="form-group">
                            <label>شعار الفريق الأول (Logo URL)</label>
                            <input type="url" name="homeLogo" placeholder="https://upload.wikimedia.org/.../Real_Madrid.png">
                        </div>
                        <div class="form-group">
                            <label>الفريق الثاني (الضيف)</label>
                            <input type="text" name="awayTeam" required placeholder="مثال: برشلونة">
                        </div>
                        <div class="form-group">
                            <label>شعار الفريق الثاني (Logo URL)</label>
                            <input type="url" name="awayLogo" placeholder="https://upload.wikimedia.org/.../Barcelona.png">
                        </div>
                        <div class="form-group">
                            <label>البطولة</label>
                            <input type="text" name="tournament" value="دوري أبطال أوروبا" placeholder="اسم البطولة">
                        </div>
                        <div class="form-group">
                            <label>شعار البطولة (Logo URL)</label>
                            <input type="url" name="tournamentLogo" placeholder="https://example.com/champions_league.png">
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
                                <option value="0">قادمة (Upcoming)</option>
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
        </div>

        <!-- TAB 2: WHO IS WATCHING NOW (Real-time Auto Refresh) -->
        <div id="tab-viewers" class="tab-content">
            <div class="card">
                <div class="card-header">
                    <h2>الجلسات النشطة والأجهزة المتصلة بالتطبيق (تحديث مباشر وتلقائي)</h2>
                    <span style="font-size: 12px; color: var(--tod-green);">🟢 تحديث تلقائي مستمر كل 4 ثوانٍ</span>
                </div>

                <div id="sessionsContainer">
                    <?php if (empty($activeSessions)): ?>
                        <p style="color: var(--text-sub); text-align: center; padding: 24px;">لا يوجد أجهزة متصلة في هذه اللحظة (ستظهر الأجهزة فور فتح التطبيق).</p>
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
                                    <form method="POST" style="margin: 0;" onsubmit="return confirm('إنهاء جلسة هذا الجهاز فوراً؟');">
                                        <input type="hidden" name="action" value="kick_session">
                                        <input type="hidden" name="deviceId" value="<?= htmlspecialchars($sess['deviceId'] ?? '') ?>">
                                        <button type="submit" class="btn danger small">فصل</button>
                                    </form>
                                </div>
                            </div>
                        <?php endforeach; ?>
                    <?php endif; ?>
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
                            <input type="url" name="iconUrl" placeholder="https://upload.wikimedia.org/.../BeIN_Sports_1.png">
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
                                            <img src="<?= htmlspecialchars($c['iconUrl']) ?>" style="width: 32px; height: 32px; object-fit: contain; border-radius: 8px;">
                                        <?php else: ?>
                                            📺
                                        <?php endif; ?>
                                    </td>
                                    <td><strong><?= htmlspecialchars($c['name']) ?></strong></td>
                                    <td><?= htmlspecialchars($c['categoryId']) ?></td>
                                    <td style="max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--tod-cyan);"><?= htmlspecialchars($c['playUrl']) ?></td>
                                    <td>
                                        <form method="POST" style="display:inline;" onsubmit="return confirm('حذف القناة؟');">
                                            <input type="hidden" name="action" value="delete_channel">
                                            <input type="hidden" name="streamId" value="<?= htmlspecialchars($c['streamId']) ?>">
                                            <button type="submit" class="btn danger small">حذف 🗑️</button>
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
                    <h2>روابط الـ API ومجلد M7</h2>
                </div>
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

    <!-- Toast Notification Popup -->
    <div id="toast">تم التحديث بنجاح!</div>

    <script>
        function showToast(msg) {
            const toast = document.getElementById('toast');
            toast.textContent = msg;
            toast.style.display = 'block';
            setTimeout(() => { toast.style.display = 'none'; }, 2500);
        }

        function switchTab(tabId) {
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
            document.getElementById(tabId).classList.add('active');
            event.currentTarget.classList.add('active');
        }

        // Real-time Quick Score adjustment via AJAX
        function adjustScore(matchId, deltaHome, deltaAway) {
            const homeEl = document.getElementById('score-home-' + matchId);
            const awayEl = document.getElementById('score-away-' + matchId);

            let curH = parseInt(homeEl.textContent) || 0;
            let curA = parseInt(awayEl.textContent) || 0;
            homeEl.textContent = Math.max(0, curH + deltaHome);
            awayEl.textContent = Math.max(0, curA + deltaAway);

            const formData = new FormData();
            formData.append('action', 'quick_score');
            formData.append('id', matchId);
            formData.append('deltaHome', deltaHome);
            formData.append('deltaAway', deltaAway);

            fetch('index.php', {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success') {
                    showToast('⚡ تم تحديث النتيجة فورياً في التطبيق!');
                }
            })
            .catch(err => console.error(err));
        }

        // Real-time Match Live toggle
        function toggleMatchLive(matchId, btn) {
            const formData = new FormData();
            formData.append('action', 'toggle_live');
            formData.append('id', matchId);

            fetch('index.php', {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(res => res.json())
            .then(data => {
                if (data.isLive) {
                    btn.className = 'badge-live';
                    btn.textContent = '🔴 مباشر الآن';
                    showToast('⚡ تم تفعيل البث المباشر للمباراة!');
                } else {
                    btn.className = 'badge-upcoming';
                    btn.textContent = '⏳ قادمة';
                    showToast('تم تحويل المباراة إلى قادمة');
                }
            });
        }

        // Real-time Minute update
        function updateMatchMinute(matchId, minVal) {
            const formData = new FormData();
            formData.append('action', 'quick_minute');
            formData.append('id', matchId);
            formData.append('liveMinute', minVal);

            fetch('index.php', {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
            .then(() => showToast('⚡ تم تحديث دقيقة المباراة!'));
        }

        // Periodic Real-time Viewers Auto Refresh
        setInterval(() => {
            fetch('api.php?action=get_sessions')
                .then(res => res.json())
                .then(data => {
                    if (data.status === 'success') {
                        document.getElementById('activeViewersCount').textContent = data.totalActive;
                    }
                })
                .catch(() => {});
        }, 4000);
    </script>
</body>
</html>
