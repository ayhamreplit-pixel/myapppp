<?php
/**
 * =========================================================================
 * TOD Sports External Web Control Panel - M7 Ultra Dashboard
 * URL: https://ayham.alwaysdata.net/m7/
 * Real-time Match Scoring, Poster Generator, Presets & Stream Management
 * =========================================================================
 */

$matchesFile = __DIR__ . '/matches.json';
$channelsFile = __DIR__ . '/channels.json';
$competitionsFile = __DIR__ . '/competitions.json';
$sessionsFile = __DIR__ . '/sessions.json';

// Initialize files if missing
if (!file_exists($matchesFile)) file_put_contents($matchesFile, json_encode(['matches' => []], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
if (!file_exists($channelsFile)) file_put_contents($channelsFile, json_encode(['channels' => []], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
if (!file_exists($sessionsFile)) file_put_contents($sessionsFile, json_encode([], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));

// Handle AJAX POST requests
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

// Handle Form POST Actions
$message = '';
$msgType = 'info';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';

    if ($action === 'add_poster' || $action === 'add_match' || $action === 'edit_match') {
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
                'liveMinute' => $_POST['liveMinute'] ?? '30\'',
                'scoreHome' => $_POST['scoreHome'] !== '' ? (int)$_POST['scoreHome'] : 0,
                'scoreAway' => $_POST['scoreAway'] !== '' ? (int)$_POST['scoreAway'] : 0,
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
            $message = 'تم نشر البوستر وتلقائياً إضافة بطاقة المباراة إلى التطبيق مباشرة!';
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
            $message = 'تم حذف المباراة والبوستر بنجاح من التطبيق!';
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
            $message = 'تم حفظ ونشر القناة إلى التطبيق بنجاح!';
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
    }
}

// Load data
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
    <title>TOD M7 - لوحة التحكم الفورية الذكية</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-dark: #07090e;
            --glass-bg: rgba(18, 22, 36, 0.85);
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
        body {
            background: radial-gradient(circle at 50% 0%, #171c32 0%, #07090e 75%);
            color: var(--text-main);
            min-height: 100vh;
            padding-bottom: 80px;
        }

        /* Top Bar */
        header {
            background: rgba(13, 16, 28, 0.9);
            border-bottom: 1px solid rgba(255,255,255,0.12);
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 100;
            backdrop-filter: blur(16px);
        }
        .brand { display: flex; align-items: center; gap: 12px; }
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

        .container { max-width: 1280px; margin: 24px auto; padding: 0 16px; }

        /* Alert */
        .alert {
            padding: 14px 18px;
            border-radius: 12px;
            margin-bottom: 20px;
            font-weight: 700;
            font-size: 14px;
        }
        .alert.success { background: rgba(48, 209, 88, 0.2); border: 1px solid var(--tod-green); color: #fff; }
        .alert.warning { background: rgba(255, 59, 48, 0.2); border: 1px solid var(--tod-red); color: #fff; }

        /* Tabs Navigation */
        .tabs {
            display: flex;
            gap: 10px;
            margin-bottom: 24px;
            background: rgba(13, 16, 28, 0.6);
            padding: 6px;
            border-radius: 16px;
            border: 1px solid rgba(255,255,255,0.08);
            overflow-x: auto;
        }
        .tab-btn {
            background: transparent;
            border: none;
            color: var(--text-sub);
            padding: 12px 20px;
            font-size: 15px;
            font-weight: 700;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
            white-space: nowrap;
        }
        .tab-btn.active {
            background: linear-gradient(135deg, var(--tod-blue), #0055FF);
            color: #fff;
            box-shadow: 0 4px 14px rgba(0, 122, 255, 0.4);
        }
        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* Form Controls & Layout */
        .card {
            background: var(--glass-bg);
            border: 1px solid var(--card-border);
            border-radius: 20px;
            padding: 24px;
            margin-bottom: 24px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.4);
        }
        .card-title {
            font-size: 18px;
            font-weight: 900;
            color: var(--tod-gold);
            margin-bottom: 18px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 16px;
        }
        .form-group {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .form-group label {
            font-size: 13px;
            font-weight: 700;
            color: var(--tod-cyan);
        }
        .form-control, select {
            background: rgba(10, 14, 26, 0.8);
            border: 1px solid rgba(255,255,255,0.15);
            border-radius: 10px;
            padding: 10px 14px;
            color: #fff;
            font-size: 14px;
            outline: none;
            transition: border-color 0.2s;
        }
        .form-control:focus, select:focus {
            border-color: var(--tod-cyan);
            box-shadow: 0 0 10px rgba(100, 210, 255, 0.25);
        }

        /* Preset Chips Grid */
        .preset-section {
            background: rgba(0, 122, 255, 0.08);
            border: 1px dashed rgba(100, 210, 255, 0.3);
            border-radius: 14px;
            padding: 14px;
            margin-bottom: 16px;
        }
        .preset-title {
            font-size: 13px;
            font-weight: 700;
            color: var(--tod-gold);
            margin-bottom: 10px;
        }
        .chips-row {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            max-height: 140px;
            overflow-y: auto;
            padding-right: 4px;
        }
        .chip {
            background: rgba(255,255,255,0.08);
            border: 1px solid rgba(255,255,255,0.15);
            border-radius: 20px;
            padding: 6px 12px;
            font-size: 12px;
            font-weight: 700;
            color: #fff;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: all 0.15s ease;
        }
        .chip:hover {
            background: var(--tod-blue);
            border-color: var(--tod-cyan);
            transform: translateY(-2px);
        }
        .chip img { width: 18px; height: 18px; object-fit: contain; }

        /* Action Buttons */
        .btn {
            background: linear-gradient(135deg, var(--tod-gold), #FF9500);
            color: #000;
            border: none;
            padding: 12px 24px;
            font-size: 15px;
            font-weight: 900;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            box-shadow: 0 4px 15px rgba(255, 184, 0, 0.3);
        }
        .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 184, 0, 0.5); }
        .btn-danger {
            background: linear-gradient(135deg, var(--tod-red), #C70000);
            color: #fff;
            box-shadow: 0 4px 12px rgba(255, 59, 48, 0.3);
        }

        /* Match Table & Live Cards */
        .matches-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 18px;
        }
        .match-card {
            background: rgba(13, 17, 30, 0.95);
            border: 1px solid var(--card-border);
            border-radius: 16px;
            padding: 16px;
            position: relative;
        }
        .match-card.is-live {
            border-color: var(--tod-red);
            box-shadow: 0 0 15px rgba(255, 59, 48, 0.25);
        }
        .badge-live {
            background: var(--tod-red);
            color: #fff;
            font-size: 11px;
            font-weight: 900;
            padding: 3px 8px;
            border-radius: 8px;
            position: absolute;
            top: 14px;
            left: 14px;
        }
        .team-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin: 12px 0;
        }
        .team-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 700;
            font-size: 15px;
            width: 42%;
        }
        .team-item img { width: 28px; height: 28px; object-fit: contain; }
        .score-box {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 900;
            font-size: 22px;
            color: var(--tod-gold);
        }
        .score-btn {
            background: rgba(255,255,255,0.1);
            border: 1px solid rgba(255,255,255,0.2);
            color: #fff;
            width: 28px;
            height: 28px;
            border-radius: 6px;
            font-weight: 900;
            cursor: pointer;
        }
        .score-btn:hover { background: var(--tod-blue); }
    </style>
</head>
<body>

    <header>
        <div class="brand">
            <div class="logo-badge">TOD M7</div>
            <div class="brand-title">
                <h1>لوحة التحكم الفورية للبوستر والمباريات</h1>
                <p>تزامن فوري ومباشر مع تطبيق الأندرويد</p>
            </div>
        </div>
        <div style="font-size: 13px; color: var(--tod-green); font-weight: 700;">
            🟢 الخادم متصل وتفاعلي
        </div>
    </header>

    <div class="container">

        <?php if ($message): ?>
            <div class="alert <?= $msgType ?>"><?= htmlspecialchars($message) ?></div>
        <?php endif; ?>

        <!-- Tabs Navigation -->
        <div class="tabs">
            <button class="tab-btn active" onclick="switchTab('poster-tab')">🎨 إضافة بوستر ومباراة تلقائياً</button>
            <button class="tab-btn" onclick="switchTab('matches-tab')">⚽ المباريات الحالية والنتائج (<?= count($matches) ?>)</button>
            <button class="tab-btn" onclick="switchTab('channels-tab')">📺 القنوات المباشرة (<?= count($channels) ?>)</button>
            <button class="tab-btn" onclick="switchTab('sessions-tab')">📱 الأجهزة المتصلة (<?= count($activeSessions) ?>)</button>
        </div>

        <!-- 1. POSTER & MATCH CREATOR TAB -->
        <div id="poster-tab" class="tab-content active">
            <div class="card">
                <div class="card-title">🎨 إضافة بوستر مباراة ينزل تلقائياً بالهيرو وبطاقات المباريات</div>

                <!-- PRESET TEAM CHOOSER -->
                <div class="preset-section">
                    <div class="preset-title">⚡ اختار الفرق الجاهزة بنقرة واحدة (المنتخبات والأندية العربية والعالمية):</div>
                    <div class="chips-row">
                        <!-- Iraq -->
                        <div class="chip" onclick="selectTeam('home', 'العراق', 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Flag_of_Iraq.svg/512px-Flag_of_Iraq.svg.png', '🇮🇶')">
                            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Flag_of_Iraq.svg/512px-Flag_of_Iraq.svg.png"> العراق
                        </div>
                        <!-- KSA -->
                        <div class="chip" onclick="selectTeam('away', 'السعودية', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Flag_of_Saudi_Arabia.svg/512px-Flag_of_Saudi_Arabia.svg.png', '🇸🇦')">
                            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Flag_of_Saudi_Arabia.svg/512px-Flag_of_Saudi_Arabia.svg.png"> السعودية
                        </div>
                        <!-- Egypt -->
                        <div class="chip" onclick="selectTeam('away', 'مصر', 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Flag_of_Egypt.svg/512px-Flag_of_Egypt.svg.png', '🇪🇬')">
                            <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Flag_of_Egypt.svg/512px-Flag_of_Egypt.svg.png"> مصر
                        </div>
                        <!-- Real Madrid -->
                        <div class="chip" onclick="selectTeam('home', 'ريال مدريد', 'https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png', '🇪🇸')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png"> ريال مدريد
                        </div>
                        <!-- Barcelona -->
                        <div class="chip" onclick="selectTeam('away', 'برشلونة', 'https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona.svg/512px-FC_Barcelona.svg.png', '🇪🇸')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona.svg/512px-FC_Barcelona.svg.png"> برشلونة
                        </div>
                        <!-- Man City -->
                        <div class="chip" onclick="selectTeam('home', 'مانشستر سيتي', 'https://upload.wikimedia.org/wikipedia/en/thumb/e/eb/Manchester_City_FC_badge.svg/512px-Manchester_City_FC_badge.svg.png', '🏴󠁧󠁢󠁥󠁮󠁧󠁿')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/e/eb/Manchester_City_FC_badge.svg/512px-Manchester_City_FC_badge.svg.png"> مانشستر سيتي
                        </div>
                        <!-- Liverpool -->
                        <div class="chip" onclick="selectTeam('away', 'ليفربول', 'https://upload.wikimedia.org/wikipedia/en/thumb/0/0c/Liverpool_FC.svg/512px-Liverpool_FC.svg.png', '🏴󠁧󠁢󠁥󠁮󠁧󠁿')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/0/0c/Liverpool_FC.svg/512px-Liverpool_FC.svg.png"> ليفربول
                        </div>
                        <!-- Al Hilal -->
                        <div class="chip" onclick="selectTeam('home', 'الهلال', 'https://upload.wikimedia.org/wikipedia/en/thumb/a/a2/Al_Hilal_SFC_logo.svg/512px-Al_Hilal_SFC_logo.svg.png', '🇸🇦')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/a/a2/Al_Hilal_SFC_logo.svg/512px-Al_Hilal_SFC_logo.svg.png"> الهلال
                        </div>
                        <!-- Al Nassr -->
                        <div class="chip" onclick="selectTeam('away', 'النصر', 'https://upload.wikimedia.org/wikipedia/en/thumb/c/c5/Al_Nassr_FC_logo.svg/512px-Al_Nassr_FC_logo.svg.png', '🇸🇦')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/c/c5/Al_Nassr_FC_logo.svg/512px-Al_Nassr_FC_logo.svg.png"> النصر
                        </div>
                        <!-- Al Shorta Iraq -->
                        <div class="chip" onclick="selectTeam('home', 'الشرطة العراقي', 'https://upload.wikimedia.org/wikipedia/en/thumb/1/1a/Al-Shorta_SC_logo.svg/512px-Al-Shorta_SC_logo.svg.png', '🇮🇶')">
                            <img src="https://upload.wikimedia.org/wikipedia/en/thumb/1/1a/Al-Shorta_SC_logo.svg/512px-Al-Shorta_SC_logo.svg.png"> الشرطة
                        </div>
                    </div>
                </div>

                <!-- PRESET TOURNAMENT CHOOSER -->
                <div class="preset-section">
                    <div class="preset-title">🏆 اختار البطولة الجاهزة بنقرة واحدة:</div>
                    <div class="chips-row">
                        <div class="chip" onclick="selectTournament('دوري أبطال أوروبا', 'https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png')">
                            🇪🇺 دوري أبطال أوروبا
                        </div>
                        <div class="chip" onclick="selectTournament('الدوري الإنجليزي الممتاز', 'https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png')">
                            🏴󠁧󠁢󠁥󠁮󠁧󠁿 الدوري الإنجليزي
                        </div>
                        <div class="chip" onclick="selectTournament('الدوري الإسباني - لا ليغا', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png')">
                            🇪🇸 الدوري الإسباني
                        </div>
                        <div class="chip" onclick="selectTournament('دوري أبطال آسيا للنخبة', 'https://upload.wikimedia.org/wikipedia/en/thumb/0/07/Confederation_of_African_Football_logo.svg/512px-Confederation_of_African_Football_logo.svg.png')">
                            🌏 دوري أبطال آسيا
                        </div>
                        <div class="chip" onclick="selectTournament('الدوري السعودي للمحترفين', 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Saudi_Pro_League_logo.svg/512px-Saudi_Pro_League_logo.svg.png')">
                            🇸🇦 الدوري السعودي
                        </div>
                    </div>
                </div>

                <form method="POST">
                    <input type="hidden" name="action" value="add_poster">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>الفريق الأول (صاحب الأرض):</label>
                            <input type="text" id="homeTeam" name="homeTeam" class="form-control" placeholder="مثل: العراق" required>
                        </div>
                        <div class="form-group">
                            <label>رابط شعار الفريق الأول (PNG):</label>
                            <input type="url" id="homeLogo" name="homeLogo" class="form-control" placeholder="https://...">
                        </div>
                        <div class="form-group">
                            <label>الفريق الثاني (الضيف):</label>
                            <input type="text" id="awayTeam" name="awayTeam" class="form-control" placeholder="مثل: السعودية" required>
                        </div>
                        <div class="form-group">
                            <label>رابط شعار الفريق الثاني (PNG):</label>
                            <input type="url" id="awayLogo" name="awayLogo" class="form-control" placeholder="https://...">
                        </div>

                        <div class="form-group">
                            <label>اسم البطولة:</label>
                            <input type="text" id="tournament" name="tournament" class="form-control" value="تصفيات كأس العالم 2026">
                        </div>
                        <div class="form-group">
                            <label>رابط شعار البطولة:</label>
                            <input type="url" id="tournamentLogo" name="tournamentLogo" class="form-control" placeholder="https://...">
                        </div>

                        <div class="form-group">
                            <label>وقت المباراة (ساعة):</label>
                            <input type="text" name="kickoffTime" class="form-control" value="22:00">
                        </div>
                        <div class="form-group">
                            <label>تاريخ المباراة:</label>
                            <input type="text" name="kickoffDate" class="form-control" value="اليوم">
                        </div>
                        <div class="form-group">
                            <label>القناة الناقلة:</label>
                            <input type="text" name="channelName" class="form-control" value="beIN SPORTS 1 HD">
                        </div>
                        <div class="form-group">
                            <label>رابط البث المباشر (M3U8):</label>
                            <input type="url" name="streamUrl" class="form-control" value="https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8">
                        </div>
                        <div class="form-group" style="grid-column: span 2;">
                            <label>رابط صورة البوستر الهيرو (Banner URL):</label>
                            <input type="url" name="bannerUrl" class="form-control" placeholder="https://... (اختياري - سيظهر بالهيرو العلوي للتطبيق تلقائياً)">
                        </div>
                    </div>

                    <div style="margin-top: 20px; display: flex; gap: 14px; align-items: center;">
                        <label><input type="checkbox" name="isLive" value="1" checked> 🔴 جعل المباراة مباشرة الآن</label>
                        <button type="submit" class="btn">🚀 نشر البوستر والمباراة تلقائياً للتطبيق</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 2. MATCHES LIST TAB -->
        <div id="matches-tab" class="tab-content">
            <div class="matches-grid">
                <?php foreach ($matches as $m): ?>
                    <div class="match-card <?= !empty($m['isLive']) ? 'is-live' : '' ?>" id="card-<?= $m['id'] ?>">
                        <?php if (!empty($m['isLive'])): ?>
                            <span class="badge-live">🔴 مباشر الآن</span>
                        <?php endif; ?>
                        
                        <div style="font-size: 12px; color: var(--tod-cyan); font-weight: 700;">
                            🏆 <?= htmlspecialchars($m['tournament'] ?? '') ?>
                        </div>

                        <div class="team-row">
                            <div class="team-item">
                                <?php if (!empty($m['homeLogo'])): ?>
                                    <img src="<?= htmlspecialchars($m['homeLogo']) ?>">
                                <?php endif; ?>
                                <span><?= htmlspecialchars($m['homeTeam'] ?? '') ?></span>
                            </div>

                            <div class="score-box">
                                <button class="score-btn" onclick="quickScore('<?= $m['id'] ?>', -1, 0)">-</button>
                                <span id="score-home-<?= $m['id'] ?>"><?= $m['scoreHome'] ?? 0 ?></span>
                                <span>-</span>
                                <span id="score-away-<?= $m['id'] ?>"><?= $m['scoreAway'] ?? 0 ?></span>
                                <button class="score-btn" onclick="quickScore('<?= $m['id'] ?>', 1, 0)">+</button>
                            </div>

                            <div class="team-item" style="justify-content: flex-end;">
                                <span><?= htmlspecialchars($m['awayTeam'] ?? '') ?></span>
                                <?php if (!empty($m['awayLogo'])): ?>
                                    <img src="<?= htmlspecialchars($m['awayLogo']) ?>">
                                <?php endif; ?>
                            </div>
                        </div>

                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 14px; pt: 10px; border-top: 1px solid rgba(255,255,255,0.1);">
                            <button class="btn" style="padding: 6px 12px; font-size: 12px;" onclick="toggleLive('<?= $m['id'] ?>')">
                                🔄 تبديل حالة المباشر
                            </button>
                            <form method="POST" style="display: inline;" onsubmit="return confirm('هل تريد حذف هذه المباراة والبوستر؟');">
                                <input type="hidden" name="action" value="delete_match">
                                <input type="hidden" name="match_id" value="<?= $m['id'] ?>">
                                <button type="submit" class="btn btn-danger" style="padding: 6px 12px; font-size: 12px;">🗑️ حذف</button>
                            </form>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>

        <!-- 3. CHANNELS TAB -->
        <div id="channels-tab" class="tab-content">
            <div class="card">
                <div class="card-title">📺 إضافة/إدارة القنوات المباشرة</div>
                <form method="POST">
                    <input type="hidden" name="action" value="add_channel">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>اسم القناة:</label>
                            <input type="text" name="name" class="form-control" placeholder="beIN SPORTS 1 HD" required>
                        </div>
                        <div class="form-group">
                            <label>رابط الشعار (PNG):</label>
                            <input type="url" name="iconUrl" class="form-control" placeholder="https://...">
                        </div>
                        <div class="form-group">
                            <label>رابط البث (M3U8):</label>
                            <input type="url" name="playUrl" class="form-control" placeholder="https://..." required>
                        </div>
                    </div>
                    <button type="submit" class="btn" style="margin-top: 16px;">➕ إضافة القناة فورياً</button>
                </form>
            </div>
        </div>

        <!-- 4. SESSIONS TAB -->
        <div id="sessions-tab" class="tab-content">
            <div class="card">
                <div class="card-title">📱 الأجهزة النشطة حالياً بالتطبيق (من يشاهد الآن)</div>
                <p style="color: var(--text-sub); font-size: 14px;">عدد المشتركين الأونلاين بالتطبيق الآن: <strong style="color: var(--tod-green);"><?= count($activeSessions) ?> جهاز</strong></p>
            </div>
        </div>

    </div>

    <script>
        function switchTab(tabId) {
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            
            event.target.classList.add('active');
            document.getElementById(tabId).classList.add('active');
        }

        function selectTeam(type, name, logo, flag) {
            if (type === 'home') {
                document.getElementById('homeTeam').value = name;
                document.getElementById('homeLogo').value = logo;
            } else {
                document.getElementById('awayTeam').value = name;
                document.getElementById('awayLogo').value = logo;
            }
        }

        function selectTournament(name, logo) {
            document.getElementById('tournament').value = name;
            document.getElementById('tournamentLogo').value = logo;
        }

        function quickScore(id, deltaHome, deltaAway) {
            let formData = new FormData();
            formData.append('action', 'quick_score');
            formData.append('id', id);
            formData.append('deltaHome', deltaHome);
            formData.append('deltaAway', deltaAway);

            fetch('index.php', {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success') {
                    location.reload();
                }
            });
        }

        function toggleLive(id) {
            let formData = new FormData();
            formData.append('action', 'toggle_live');
            formData.append('id', id);

            fetch('index.php', {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                location.reload();
            });
        }
    </script>
</body>
</html>
