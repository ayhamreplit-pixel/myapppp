<?php
/**
 * =========================================================================
 *  لوحة تحكم وسيرفر TOD / HERO Cast للمباريات والبث المباشر (m7)
 *  - جلب تلقائي لمباريات اليوم من YSScores مع نظام كاش متطور لمنع الحظر
 *  - لوحة تحكم عربية متقدمة: إضافة وتعديل السيرفرات، النتائج، الدقائق، والمعلقين
 *  - مشغل فيديو مدمج (HLS / m3u8) لفحص واختبار سيرفرات البث قبل النشر
 *  - تحديث تلقائي مباشر (Live Polling & Auto-Refresh) بدون إعادة تحميل الصفحة
 *  - تصنيف وترتيب حسب البطولات مع بحث سريع وفلترة حسب التواريخ
 *  - API فائق السرعة لتطبيق الأندرويد مع دعم البث المتعدد والكاش الذكي
 * =========================================================================
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

date_default_timezone_set('Asia/Riyadh');
$todayDate = date('Y-m-d');

// إعداد مسار حفظ ملفات البيانات والتعديلات
$dataDir = __DIR__ . '/data';
if (!is_dir($dataDir)) {
    @mkdir($dataDir, 0777, true);
}
$overridesFile = $dataDir . '/match_overrides.json';
$customMatchesFile = $dataDir . '/custom_matches.json';
$settingsFile = $dataDir . '/settings.json';

// تهيئة الملفات إذا لم تكن موجودة
if (!file_exists($overridesFile)) {
    @file_put_contents($overridesFile, json_encode([], JSON_UNESCAPED_UNICODE));
}
if (!file_exists($customMatchesFile)) {
    @file_put_contents($customMatchesFile, json_encode([], JSON_UNESCAPED_UNICODE));
}
if (!file_exists($settingsFile)) {
    @file_put_contents($settingsFile, json_encode(["site_title" => "HERO Cast / TOD"], JSON_UNESCAPED_UNICODE));
}

function getJsonData($file) {
    if (!file_exists($file)) return [];
    $content = @file_get_contents($file);
    $decoded = json_decode($content, true);
    return is_array($decoded) ? $decoded : [];
}

function saveJsonData($file, $data) {
    return @file_put_contents($file, json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
}

// دالة جلب مباريات YSScores مع كاش ذكي
function fetchYsscoresMatches($targetDate, $forceRefresh = false) {
    global $todayDate;
    $cacheDir = sys_get_temp_dir() . "/kora_cache";
    if (!is_dir($cacheDir)) {
        @mkdir($cacheDir, 0777, true);
    }
    $cacheFile = $cacheDir . "/matches_" . $targetDate . ".json";
    $cacheTTL = ($targetDate === $todayDate) ? 25 : 600; // 25 ثانية لليوم لمنع الحظر ومواكبة الدقائق والنتائج

    if (!$forceRefresh && file_exists($cacheFile) && (time() - filemtime($cacheFile) < $cacheTTL)) {
        $cached = @file_get_contents($cacheFile);
        $decoded = json_decode($cached, true);
        if ($decoded && isset($decoded['data'])) {
            return $decoded['data'];
        }
    }

    $apiUrl = "https://api-ar.ysscores.com/api/matches/matches_date_get/" . $targetDate . "/%5B%5D/%5B%5D/%5B%5D/D/180";
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $apiUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 9);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
        'Accept: application/json, text/plain, */*',
        'Referer: https://ysscores.com/',
        'Origin: https://ysscores.com'
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode === 200 && !empty($response)) {
        $decoded = json_decode($response, true);
        if ($decoded !== null && isset($decoded['data'])) {
            @file_put_contents($cacheFile, $response);
            return $decoded['data'];
        }
    }

    if (file_exists($cacheFile)) {
        $cached = @file_get_contents($cacheFile);
        $decoded = json_decode($cached, true);
        if ($decoded && isset($decoded['data'])) {
            return $decoded['data'];
        }
    }

    return [];
}

// تحديد التاريخ
$dateParam = isset($_GET['date']) ? trim($_GET['date']) : '';
if ($dateParam === 'yesterday') {
    $targetDate = date('Y-m-d', strtotime('-1 day'));
} elseif ($dateParam === 'tomorrow') {
    $targetDate = date('Y-m-d', strtotime('+1 day'));
} elseif (preg_match('/^\d{4}-\d{2}-\d{2}$/', $dateParam)) {
    $targetDate = $dateParam;
} else {
    $targetDate = $todayDate;
}

$forceRefresh = isset($_GET['refresh']) && ($_GET['refresh'] == '1' || $_GET['refresh'] == 'true');
$action = isset($_GET['action']) ? trim($_GET['action']) : '';

// -------------------------------------------------------------------------
// معالجة طلبات POST من لوحة التحكم والتطبيق
// -------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    header('Content-Type: application/json; charset=utf-8');
    $input = json_decode(file_get_contents('php://input'), true);
    if (!$input) {
        $input = $_POST;
    }

    $postAction = isset($input['action']) ? $input['action'] : $action;
    $overrides = getJsonData($overridesFile);

    // 1. إضافة أو تعديل سيرفر بث لمباراة
    if ($postAction === 'save_server') {
        $matchId = trim($input['match_id'] ?? '');
        $serverName = trim($input['server_name'] ?? 'سيرفر 1');
        $serverUrl = trim($input['stream_url'] ?? '');
        $quality = trim($input['quality'] ?? 'FHD');
        $serverId = trim($input['server_id'] ?? '');

        if (empty($matchId) || empty($serverUrl)) {
            echo json_encode(["status" => false, "message" => "بيانات السيرفر غير مكتملة (يرجى إدخال الرابط)"]);
            exit;
        }

        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false];
        }

        if (!isset($overrides[$matchId]['servers'])) {
            $overrides[$matchId]['servers'] = [];
        }

        if (!empty($serverId)) {
            // تحديث سيرفر موجود
            foreach ($overrides[$matchId]['servers'] as &$srv) {
                if ($srv['id'] === $serverId) {
                    $srv['name'] = $serverName;
                    $srv['url'] = $serverUrl;
                    $srv['quality'] = $quality;
                    break;
                }
            }
        } else {
            // إضافة سيرفر جديد
            $newId = 'srv_' . time() . '_' . rand(100, 999);
            $overrides[$matchId]['servers'][] = [
                'id' => $newId,
                'name' => $serverName,
                'url' => $serverUrl,
                'quality' => $quality
            ];
        }

        saveJsonData($overridesFile, $overrides);
        echo json_encode(["status" => true, "message" => "تم حفظ وتفعيل سيرفر البث بنجاح"]);
        exit;
    }

    // 2. حذف سيرفر بث
    if ($postAction === 'delete_server') {
        $matchId = trim($input['match_id'] ?? '');
        $serverId = trim($input['server_id'] ?? '');

        if (isset($overrides[$matchId]['servers'])) {
            $overrides[$matchId]['servers'] = array_values(array_filter($overrides[$matchId]['servers'], function($s) use ($serverId) {
                return $s['id'] !== $serverId;
            }));
            saveJsonData($overridesFile, $overrides);
        }

        echo json_encode(["status" => true, "message" => "تم حذف السيرفر"]);
        exit;
    }

    // 3. تعديل تفاصيل المباراة يدوياً (نتيجة، دقيقة، معلق، قناة، حالة)
    if ($postAction === 'edit_match') {
        $matchId = trim($input['match_id'] ?? '');
        if (empty($matchId)) {
            echo json_encode(["status" => false, "message" => "معرف المباراة مفقود"]);
            exit;
        }

        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false];
        }

        if (isset($input['home_scores']) && $input['home_scores'] !== '') {
            $overrides[$matchId]['home_scores'] = intval($input['home_scores']);
        } else {
            unset($overrides[$matchId]['home_scores']);
        }

        if (isset($input['away_scores']) && $input['away_scores'] !== '') {
            $overrides[$matchId]['away_scores'] = intval($input['away_scores']);
        } else {
            unset($overrides[$matchId]['away_scores']);
        }

        if (isset($input['status']) && $input['status'] !== '') {
            $overrides[$matchId]['status'] = intval($input['status']);
            $overrides[$matchId]['live'] = in_array(intval($input['status']), [2, 3]) ? 1 : 0;
        }

        if (isset($input['score_time'])) {
            $overrides[$matchId]['score_time'] = trim($input['score_time']);
        }

        if (isset($input['channel_name'])) {
            $overrides[$matchId]['channel_name'] = trim($input['channel_name']);
        }

        if (isset($input['commentator'])) {
            $overrides[$matchId]['commentator'] = trim($input['commentator']);
        }

        if (isset($input['stadium'])) {
            $overrides[$matchId]['stadium'] = trim($input['stadium']);
        }

        saveJsonData($overridesFile, $overrides);
        echo json_encode(["status" => true, "message" => "تم حفظ تعديلات المباراة بنجاح"]);
        exit;
    }

    // 4. تمييز في البانر (Toggle Feature)
    if ($postAction === 'toggle_feature') {
        $matchId = trim($input['match_id'] ?? '');
        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false];
        }
        $overrides[$matchId]['featured'] = !($overrides[$matchId]['featured'] ?? false);
        saveJsonData($overridesFile, $overrides);
        echo json_encode(["status" => true, "featured" => $overrides[$matchId]['featured']]);
        exit;
    }

    // 5. إخفاء / إظهار مباراة
    if ($postAction === 'toggle_hidden') {
        $matchId = trim($input['match_id'] ?? '');
        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false];
        }
        $overrides[$matchId]['hidden'] = !($overrides[$matchId]['hidden'] ?? false);
        saveJsonData($overridesFile, $overrides);
        echo json_encode(["status" => true, "hidden" => $overrides[$matchId]['hidden']]);
        exit;
    }

    // 6. إضافة مباراة مخصصة
    if ($postAction === 'add_custom_match') {
        $customMatches = getJsonData($customMatchesFile);
        $newMatch = [
            'match_id' => 'custom_' . time(),
            'championship' => [
                'title' => trim($input['championship'] ?? 'مباراة خاصة'),
                'image' => trim($input['championship_image'] ?? '')
            ],
            'home_team' => [
                'title' => trim($input['home_team'] ?? 'الفريق الأول'),
                'image' => trim($input['home_team_image'] ?? '')
            ],
            'away_team' => [
                'title' => trim($input['away_team'] ?? 'الفريق الثاني'),
                'image' => trim($input['away_team_image'] ?? '')
            ],
            'match_time' => trim($input['match_time'] ?? '21:00:00'),
            'match_date' => trim($input['match_date'] ?? $todayDate),
            'status' => intval($input['status'] ?? 1),
            'live' => intval($input['live'] ?? 0),
            'home_scores' => isset($input['home_scores']) && $input['home_scores'] !== '' ? intval($input['home_scores']) : null,
            'away_scores' => isset($input['away_scores']) && $input['away_scores'] !== '' ? intval($input['away_scores']) : null,
            'score_time' => trim($input['score_time'] ?? ''),
            'channel_name' => trim($input['channel_name'] ?? 'beIN SPORTS 1 HD'),
            'commentator' => trim($input['commentator'] ?? 'تعليق عربي'),
            'stadium' => trim($input['stadium'] ?? 'الملعب الرئيسي'),
            'servers' => []
        ];
        if (!empty($input['stream_url'])) {
            $newMatch['servers'][] = [
                'id' => 'srv_1',
                'name' => trim($input['server_name'] ?? 'سيرفر البث الرئيسي'),
                'url' => trim($input['stream_url']),
                'quality' => trim($input['quality'] ?? 'FHD')
            ];
        }
        $customMatches[] = $newMatch;
        saveJsonData($customMatchesFile, $customMatches);
        echo json_encode(["status" => true, "message" => "تمت إضافة المباراة المخصصة بنجاح"]);
        exit;
    }

    // 7. استعادة النسخة الاحتياطية
    if ($postAction === 'restore_backup') {
        $backupData = $input['backup_data'] ?? null;
        if (is_array($backupData)) {
            if (isset($backupData['overrides'])) saveJsonData($overridesFile, $backupData['overrides']);
            if (isset($backupData['custom_matches'])) saveJsonData($customMatchesFile, $backupData['custom_matches']);
            echo json_encode(["status" => true, "message" => "تمت استعادة النسخة الاحتياطية بنجاح"]);
            exit;
        }
        echo json_encode(["status" => false, "message" => "ملف النسخة الاحتياطية غير صالح"]);
        exit;
    }
}

// -------------------------------------------------------------------------
// دمج بيانات YSScores مع تعديلات لوحة التحكم (Enrichment Engine)
// -------------------------------------------------------------------------
if ($action === 'clear_cache') {
    $cacheDir = sys_get_temp_dir() . "/kora_cache";
    $cacheFile = $cacheDir . "/matches_" . $targetDate . ".json";
    if (file_exists($cacheFile)) {
        @unlink($cacheFile);
    }
    $forceRefresh = true;
}

$rawMatches = fetchYsscoresMatches($targetDate, $forceRefresh);
$overrides = getJsonData($overridesFile);
$customMatches = getJsonData($customMatchesFile);

$enrichedMatches = [];

// 1. إضافة المباريات المخصصة الخاصة بالتاريخ
foreach ($customMatches as $cm) {
    if (($cm['match_date'] ?? $todayDate) === $targetDate) {
        $cmId = strval($cm['match_id']);
        if (isset($overrides[$cmId]) && !empty($overrides[$cmId]['hidden'])) {
            continue;
        }
        if (isset($overrides[$cmId])) {
            $ov = $overrides[$cmId];
            if (!empty($ov['servers'])) $cm['servers'] = $ov['servers'];
            if (isset($ov['home_scores'])) $cm['home_scores'] = $ov['home_scores'];
            if (isset($ov['away_scores'])) $cm['away_scores'] = $ov['away_scores'];
            if (isset($ov['status'])) $cm['status'] = $ov['status'];
            if (isset($ov['live'])) $cm['live'] = $ov['live'];
            if (!empty($ov['score_time'])) $cm['score_time'] = $ov['score_time'];
            if (!empty($ov['channel_name'])) $cm['channel_name'] = $ov['channel_name'];
            if (!empty($ov['commentator'])) $cm['commentator'] = $ov['commentator'];
            if (!empty($ov['stadium'])) $cm['stadium'] = $ov['stadium'];
            $cm['is_featured'] = !empty($ov['featured']);
        }
        if (!empty($cm['servers'])) {
            $cm['streamUrl'] = $cm['servers'][0]['url'];
        }
        $enrichedMatches[] = $cm;
    }
}

// 2. دمج مباريات YSScores مع السيرفرات والتعديلات
foreach ($rawMatches as $m) {
    $mId = strval($m['match_id']);
    if (isset($overrides[$mId])) {
        $ov = $overrides[$mId];
        if (!empty($ov['hidden'])) {
            continue; // مخفية
        }
        $m['servers'] = $ov['servers'] ?? [];
        $m['is_featured'] = !empty($ov['featured']);

        if (isset($ov['home_scores'])) $m['home_scores'] = $ov['home_scores'];
        if (isset($ov['away_scores'])) $m['away_scores'] = $ov['away_scores'];
        if (isset($ov['status'])) {
            $m['status'] = $ov['status'];
            $m['live'] = in_array($ov['status'], [2, 3]) ? 1 : 0;
        }
        if (!empty($ov['score_time'])) $m['score_time'] = $ov['score_time'];
        if (!empty($ov['channel_name'])) {
            $m['channel_commm'] = [['channel_name' => $ov['channel_name'], 'commentator' => $ov['commentator'] ?? 'تعليق عربي']];
            $m['channel_name'] = $ov['channel_name'];
        }
        if (!empty($ov['commentator'])) $m['commentator'] = $ov['commentator'];
        if (!empty($ov['stadium'])) $m['stadium'] = $ov['stadium'];

        if (!empty($m['servers'])) {
            $m['streamUrl'] = $m['servers'][0]['url'];
        }
    } else {
        $m['servers'] = [];
        $m['is_featured'] = false;
    }
    $enrichedMatches[] = $m;
}

// -------------------------------------------------------------------------
// API Responses (للتطبيق وأوامر الـ AJAX المباشرة)
// -------------------------------------------------------------------------

// أ) إرجاع كائن التحديث الخفيف للنتائج والدقائق فقط (Lightweight Live Polling)
if ($action === 'live_scores') {
    header('Content-Type: application/json; charset=utf-8');
    $scores = [];
    foreach ($enrichedMatches as $m) {
        $scores[] = [
            'id' => strval($m['match_id']),
            'status' => $m['status'] ?? 1,
            'live' => $m['live'] ?? 0,
            'home_scores' => $m['home_scores'] ?? null,
            'away_scores' => $m['away_scores'] ?? null,
            'score_time' => $m['score_time'] ?? null
        ];
    }
    echo json_encode(["status" => true, "date" => $targetDate, "live_scores" => $scores], JSON_UNESCAPED_UNICODE);
    exit;
}

// ب) تصدير النسخة الاحتياطية كملف JSON
if ($action === 'export_backup') {
    header('Content-Type: application/json; charset=utf-8');
    header('Content-Disposition: attachment; filename="m7_backup_' . date('Y-m-d_H-i') . '.json"');
    echo json_encode([
        "version" => "2.0",
        "created_at" => date('Y-m-d H:i:s'),
        "overrides" => $overrides,
        "custom_matches" => $customMatches
    ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
    exit;
}

// ج) إرجاع قائمة المباريات الكاملة لتطبيق الأندرويد
$isApiRequest = (
    $action === 'matches' ||
    $action === 'ysscores' ||
    isset($_GET['json']) ||
    (isset($_SERVER['HTTP_ACCEPT']) && strpos($_SERVER['HTTP_ACCEPT'], 'application/json') !== false) ||
    (isset($_SERVER['HTTP_USER_AGENT']) && strpos($_SERVER['HTTP_USER_AGENT'], 'TOD-Android') !== false)
);

if ($isApiRequest) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        "status" => true,
        "status_code" => 200,
        "date" => $targetDate,
        "total_matches" => count($enrichedMatches),
        "matches_live" => count(array_filter($enrichedMatches, function($m) { return !empty($m['live']) || in_array($m['status'] ?? 0, [2, 3]); })),
        "data" => $enrichedMatches,
        "matches" => $enrichedMatches
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// =========================================================================
//  لوحة التحكم الرسومية المتقدمة باللغة العربية (Full Modern Web Dashboard)
// =========================================================================
$liveCount = 0;
$streamedCount = 0;
$championshipsMap = [];

foreach ($enrichedMatches as $m) {
    $isLive = !empty($m['live']) || in_array($m['status'] ?? 0, [2, 3]);
    if ($isLive) $liveCount++;
    if (!empty($m['servers'])) $streamedCount++;

    $champTitle = $m['championship']['title'] ?? 'أخرى';
    if (!isset($championshipsMap[$champTitle])) {
        $championshipsMap[$champTitle] = 0;
    }
    $championshipsMap[$champTitle]++;
}
?>
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HERO Cast / TOD - لوحة إدارة مباريات وسيرفرات البث المباشر (m7)</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
    <style>
        :root {
            --bg-dark: #07090E;
            --card-bg: #10141F;
            --card-border: #1E253A;
            --tod-gold: #FFB800;
            --tod-gold-glow: rgba(255, 184, 0, 0.25);
            --tod-accent: #0A84FF;
            --tod-red: #E50914;
            --tod-green: #30D158;
            --text-main: #FFFFFF;
            --text-muted: #8E9BAE;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Cairo', sans-serif; }
        body { background: var(--bg-dark); color: var(--text-main); min-height: 100vh; padding: 20px; }
        .container { max-width: 1240px; margin: 0 auto; }
        
        /* Header */
        .header { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #12192B, #090D15); padding: 22px 30px; border-radius: 22px; border: 1px solid var(--card-border); margin-bottom: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.6); flex-wrap: wrap; gap: 14px; }
        .logo-box { display: flex; align-items: center; gap: 16px; }
        .badge-tod { background: var(--tod-gold); color: #000; font-weight: 900; padding: 5px 14px; border-radius: 9px; font-size: 17px; letter-spacing: 1px; box-shadow: 0 4px 15px var(--tod-gold-glow); }
        .title { font-size: 23px; font-weight: 900; }
        .subtitle { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
        .header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

        /* Stats Bar */
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 16px; margin-bottom: 24px; }
        .stat-card { background: var(--card-bg); border: 1px solid var(--card-border); padding: 18px 22px; border-radius: 18px; text-align: right; position: relative; overflow: hidden; }
        .stat-card::after { content: ""; position: absolute; top: 0; right: 0; width: 4px; height: 100%; background: var(--tod-accent); }
        .stat-card.gold::after { background: var(--tod-gold); }
        .stat-card.red::after { background: var(--tod-red); }
        .stat-card.green::after { background: var(--tod-green); }
        .stat-val { font-size: 30px; font-weight: 900; line-height: 1.1; }
        .stat-lbl { font-size: 13px; color: var(--text-muted); margin-top: 6px; }

        /* Controls & Filter Bar */
        .controls-bar { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 18px; padding: 14px 20px; margin-bottom: 22px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; }
        .date-nav { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
        .btn-date { background: #161C2C; border: 1px solid var(--card-border); color: var(--text-main); padding: 8px 16px; border-radius: 12px; text-decoration: none; font-weight: 700; font-size: 13px; transition: 0.2s; }
        .btn-date:hover, .btn-date.active { background: var(--tod-accent); border-color: var(--tod-accent); color: #fff; }
        .date-input { background: #0A0D16; border: 1px solid var(--card-border); color: #fff; padding: 7px 12px; border-radius: 10px; font-size: 13px; cursor: pointer; }

        .search-box { display: flex; align-items: center; gap: 10px; flex: 1; max-width: 380px; }
        .search-input { width: 100%; background: #0A0D16; border: 1px solid var(--card-border); color: #fff; padding: 9px 14px; border-radius: 12px; font-size: 13.5px; }
        .search-input:focus { border-color: var(--tod-accent); outline: none; }
        .select-filter { background: #0A0D16; border: 1px solid var(--card-border); color: #fff; padding: 9px 12px; border-radius: 12px; font-size: 13px; cursor: pointer; }

        /* Auto refresh bar */
        .refresh-status { display: flex; align-items: center; gap: 10px; font-size: 12px; color: var(--text-muted); }
        .live-dot { width: 9px; height: 9px; background: var(--tod-green); border-radius: 50%; box-shadow: 0 0 8px var(--tod-green); animation: pulse 1.5s infinite; }
        @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }

        /* Matches List */
        .matches-grid { display: flex; flex-direction: column; gap: 14px; }
        .match-row { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 18px; padding: 18px 22px; display: grid; grid-template-columns: 2.2fr 1.3fr 2.5fr 2fr; align-items: center; gap: 16px; transition: 0.2s; }
        .match-row:hover { border-color: rgba(255, 184, 0, 0.4); }
        .match-row.has-streams { border-right: 4px solid var(--tod-green); }
        .match-row.is-featured { border-right: 4px solid var(--tod-gold); background: linear-gradient(90deg, rgba(255, 184, 0, 0.05), transparent); }

        .team-box { display: flex; flex-direction: column; gap: 9px; }
        .team-item { display: flex; align-items: center; gap: 10px; font-weight: 800; font-size: 15px; }
        .team-logo { width: 28px; height: 28px; object-fit: contain; }
        .score-pill { background: #07090E; border: 1px solid var(--card-border); padding: 2px 10px; border-radius: 7px; font-weight: 900; margin-right: auto; min-width: 28px; text-align: center; color: var(--tod-gold); }

        .time-box { display: flex; flex-direction: column; align-items: center; text-align: center; }
        .champ-box { font-size: 12px; color: var(--tod-accent); font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 170px; }
        .live-badge { background: var(--tod-red); color: #fff; font-size: 11px; font-weight: 900; padding: 2px 9px; border-radius: 6px; animation: pulse 1.5s infinite; margin-top: 4px; }
        .ended-badge { background: #22293A; color: #8E9BAE; font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 6px; margin-top: 4px; }

        /* Servers Cell */
        .servers-cell { display: flex; flex-direction: column; gap: 6px; align-items: flex-start; }
        .server-pills { display: flex; flex-wrap: wrap; gap: 6px; }
        .srv-pill { background: rgba(10, 132, 255, 0.15); border: 1px solid var(--tod-accent); color: #64D2FF; font-size: 11.5px; padding: 4px 9px; border-radius: 8px; display: flex; align-items: center; gap: 6px; }
        .srv-del { cursor: pointer; color: var(--tod-red); font-weight: 900; padding: 0 4px; font-size: 13px; }
        .srv-test { cursor: pointer; color: var(--tod-gold); font-size: 11px; font-weight: bold; background: rgba(255,184,0,0.15); padding: 1px 6px; border-radius: 4px; }

        /* Actions */
        .actions-cell { display: flex; gap: 6px; justify-content: flex-end; flex-wrap: wrap; }
        .btn-action { background: #182032; border: 1px solid var(--card-border); color: #fff; padding: 7px 12px; border-radius: 10px; font-weight: 700; font-size: 12px; cursor: pointer; transition: 0.2s; text-decoration: none; display: inline-flex; align-items: center; gap: 5px; }
        .btn-action:hover { background: var(--tod-gold); color: #000; }
        .btn-add-srv { background: var(--tod-gold); color: #000; font-weight: 900; border: none; }
        .btn-add-srv:hover { background: #FFA000; }
        .btn-edit { background: #1E273D; border-color: #2D3958; }

        /* Modal */
        .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.85); backdrop-filter: blur(8px); display: none; justify-content: center; align-items: center; z-index: 1000; padding: 16px; }
        .modal { background: #101524; border: 1px solid var(--tod-gold); width: 100%; max-width: 540px; padding: 26px; border-radius: 22px; box-shadow: 0 20px 60px rgba(0,0,0,0.9); max-height: 90vh; overflow-y: auto; }
        .modal-title { font-size: 18px; font-weight: 900; margin-bottom: 16px; color: var(--tod-gold); display: flex; align-items: center; gap: 8px; }
        .form-group { margin-bottom: 14px; text-align: right; }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
        .form-label { display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 5px; font-weight: 700; }
        .form-input { width: 100%; padding: 10px 14px; background: #07090F; border: 1px solid var(--card-border); border-radius: 11px; color: #fff; font-size: 13.5px; }
        .form-input:focus { border-color: var(--tod-gold); outline: none; }
        .modal-btns { display: flex; gap: 10px; margin-top: 22px; }
        .btn-save { flex: 1; background: var(--tod-gold); color: #000; font-weight: 900; padding: 12px; border: none; border-radius: 12px; cursor: pointer; font-size: 14px; }
        .btn-cancel { flex: 1; background: #182032; color: #fff; font-weight: 700; padding: 12px; border: none; border-radius: 12px; cursor: pointer; font-size: 14px; }

        /* Video Preview Modal */
        #videoPlayerBox { width: 100%; height: 260px; background: #000; border-radius: 14px; margin-bottom: 14px; overflow: hidden; display: flex; align-items: center; justify-content: center; }
        #previewVideo { width: 100%; height: 100%; object-fit: contain; }

        @media (max-width: 900px) {
            .match-row { grid-template-columns: 1fr; gap: 12px; }
            .time-box { flex-direction: row; justify-content: space-between; }
            .actions-cell { justify-content: stretch; }
            .btn-action { flex: 1; justify-content: center; }
        }
    </style>
</head>
<body>
<div class="container">
    <!-- Header -->
    <div class="header">
        <div class="logo-box">
            <span class="badge-tod">TOD</span>
            <div>
                <h1 class="title">لوحة سيرفرات ومباريات HERO Cast (m7)</h1>
                <p class="subtitle">تحكم كامل ببث ومباريات اليوم - التحديث أوتوماتيكي ومباشر مع تطبيقك</p>
            </div>
        </div>
        <div class="header-actions">
            <button class="btn-action" onclick="openCustomMatchModal()">+ مباراة جديدة</button>
            <a href="?action=export_backup" class="btn-action">💾 تصدير نسخة</a>
            <a href="?action=clear_cache&date=<?= $targetDate ?>" class="btn-action" style="border-color: var(--tod-gold); color: var(--tod-gold);">⚡ إفراغ الكاش والتحديث</a>
            <a href="?json=1&date=<?= $targetDate ?>" target="_blank" class="btn-action" style="background: var(--tod-accent); border-color: var(--tod-accent);">رابط الـ API (JSON)</a>
        </div>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-val"><?= count($enrichedMatches) ?></div>
            <div class="stat-lbl">إجمالي المباريات المتاحة</div>
        </div>
        <div class="stat-card red">
            <div class="stat-val" style="color: var(--tod-red);"><?= $liveCount ?></div>
            <div class="stat-lbl">المباريات المباشرة الآن</div>
        </div>
        <div class="stat-card green">
            <div class="stat-val" style="color: var(--tod-green);"><?= $streamedCount ?></div>
            <div class="stat-lbl">مباريات بسيرفرات مشاهدة جاهزة</div>
        </div>
        <div class="stat-card gold">
            <div class="stat-val" style="color: var(--tod-gold);"><?= count($championshipsMap) ?></div>
            <div class="stat-lbl">عدد الدوريات والبطولات</div>
        </div>
    </div>

    <!-- Controls Bar -->
    <div class="controls-bar">
        <div class="date-nav">
            <a href="?date=yesterday" class="btn-date <?= $targetDate === date('Y-m-d', strtotime('-1 day')) ? 'active' : '' ?>">الأمس</a>
            <a href="?date=<?= $todayDate ?>" class="btn-date <?= $targetDate === $todayDate ? 'active' : '' ?>">اليوم</a>
            <a href="?date=tomorrow" class="btn-date <?= $targetDate === date('Y-m-d', strtotime('+1 day')) ? 'active' : '' ?>">الغد</a>
            <input type="date" class="date-input" value="<?= $targetDate ?>" onchange="location.href='?date=' + this.value">
        </div>

        <div class="search-box">
            <input type="text" id="matchSearch" class="search-input" placeholder="🔍 ابحث عن فريق أو بطولة..." onkeyup="filterMatches()">
            <select id="champFilter" class="select-filter" onchange="filterMatches()">
                <option value="ALL">جميع البطولات (<?= count($enrichedMatches) ?>)</option>
                <?php foreach ($championshipsMap as $chName => $cnt): ?>
                    <option value="<?= htmlspecialchars($chName) ?>"><?= htmlspecialchars($chName) ?> (<?= $cnt ?>)</option>
                <?php endforeach; ?>
            </select>
        </div>

        <div class="refresh-status">
            <span class="live-dot"></span>
            <span>تحديث مباشر كل 25 ثانية</span>
        </div>
    </div>

    <!-- Matches Grid -->
    <div class="matches-grid" id="matchesContainer">
        <?php if (empty($enrichedMatches)): ?>
            <div style="text-align: center; padding: 50px; background: var(--card-bg); border-radius: 18px; color: var(--text-muted);">
                لا توجد مباريات مسجلة لهذا التاريخ حالياً. يمكنك إضافة مباراة مخصصة بالضغط على «+ مباراة جديدة» أعلاه.
            </div>
        <?php else: ?>
            <?php foreach ($enrichedMatches as $m): 
                $mId = strval($m['match_id']);
                $isLive = !empty($m['live']) || in_array($m['status'] ?? 0, [2, 3]);
                $isEnded = ($m['status'] ?? 0) === 4;
                $homeName = $m['home_team']['title'] ?? 'فريق 1';
                $awayName = $m['away_team']['title'] ?? 'فريق 2';
                $homeImg = !empty($m['home_team']['image']) ? (strpos($m['home_team']['image'], 'http') === 0 ? $m['home_team']['image'] : "https://img.ysscores.com/teams/" . $m['home_team']['image']) : '';
                $awayImg = !empty($m['away_team']['image']) ? (strpos($m['away_team']['image'], 'http') === 0 ? $m['away_team']['image'] : "https://img.ysscores.com/teams/" . $m['away_team']['image']) : '';
                $champName = $m['championship']['title'] ?? 'بطولة كروية';
                $servers = $m['servers'] ?? [];
                $liveMinute = !empty($m['score_time']) ? $m['score_time'] : ($isLive ? 'مباشر' : '');
            ?>
            <div class="match-row <?= !empty($servers) ? 'has-streams' : '' ?> <?= !empty($m['is_featured']) ? 'is-featured' : '' ?>" data-champ="<?= htmlspecialchars($champName) ?>" data-names="<?= htmlspecialchars(strtolower($homeName . ' ' . $awayName . ' ' . $champName)) ?>">
                <!-- Teams -->
                <div class="team-box">
                    <div class="team-item">
                        <?php if ($homeImg): ?><img src="<?= htmlspecialchars($homeImg) ?>" class="team-logo" onerror="this.style.display='none'"><?php else: ?>⚽<?php endif; ?>
                        <span><?= htmlspecialchars($homeName) ?></span>
                        <?php if (isset($m['home_scores'])): ?>
                            <span class="score-pill"><?= $m['home_scores'] ?></span>
                        <?php endif; ?>
                    </div>
                    <div class="team-item">
                        <?php if ($awayImg): ?><img src="<?= htmlspecialchars($awayImg) ?>" class="team-logo" onerror="this.style.display='none'"><?php else: ?>⚽<?php endif; ?>
                        <span><?= htmlspecialchars($awayName) ?></span>
                        <?php if (isset($m['away_scores'])): ?>
                            <span class="score-pill"><?= $m['away_scores'] ?></span>
                        <?php endif; ?>
                    </div>
                </div>

                <!-- Info & Time -->
                <div class="time-box">
                    <span class="champ-box" title="<?= htmlspecialchars($champName) ?>"><?= htmlspecialchars($champName) ?></span>
                    <span style="font-weight: 900; font-size: 15px; margin-top: 4px;">
                        <?= substr($m['match_time'] ?? '20:00:00', 0, 5) ?>
                    </span>
                    <?php if ($isLive): ?>
                        <span class="live-badge">مباشر <?= htmlspecialchars($liveMinute) ?></span>
                    <?php elseif ($isEnded): ?>
                        <span class="ended-badge">انتهت</span>
                    <?php endif; ?>
                </div>

                <!-- Servers Cell -->
                <div class="servers-cell">
                    <span style="font-size: 11px; color: var(--text-muted); font-weight: 700;">سيرفرات المشاهدة المربوطة:</span>
                    <?php if (empty($servers)): ?>
                        <span style="font-size: 11.5px; color: #777;">(بدون سيرفر بث حالياً)</span>
                    <?php else: ?>
                        <div class="server-pills">
                            <?php foreach ($servers as $s): ?>
                                <span class="srv-pill">
                                    📺 <?= htmlspecialchars($s['name']) ?> (<?= htmlspecialchars($s['quality']) ?>)
                                    <span class="srv-test" onclick="previewStream('<?= htmlspecialchars(addslashes($s['url'])) ?>', '<?= htmlspecialchars(addslashes($s['name'])) ?>')">فحص</span>
                                    <span class="srv-del" onclick="deleteServer('<?= $mId ?>', '<?= $s['id'] ?>')">×</span>
                                </span>
                            <?php endforeach; ?>
                        </div>
                    <?php endif; ?>
                </div>

                <!-- Actions -->
                <div class="actions-cell">
                    <button class="btn-action btn-add-srv" onclick="openAddServerModal('<?= $mId ?>', '<?= htmlspecialchars(addslashes($homeName . ' ضد ' . $awayName)) ?>')">
                        + سيرفر بث
                    </button>
                    <button class="btn-action btn-edit" onclick="openEditMatchModal(<?= htmlspecialchars(json_encode([
                        'id' => $mId,
                        'title' => $homeName . ' ضد ' . $awayName,
                        'home_scores' => $m['home_scores'] ?? '',
                        'away_scores' => $m['away_scores'] ?? '',
                        'status' => $m['status'] ?? 1,
                        'score_time' => $m['score_time'] ?? '',
                        'channel_name' => $m['channel_name'] ?? ($m['channel_commm'][0]['channel_name'] ?? 'beIN SPORTS 1 HD'),
                        'commentator' => $m['commentator'] ?? ($m['channel_commm'][0]['commentator'] ?? 'تعليق عربي'),
                        'stadium' => $m['stadium'] ?? ''
                    ])) ?>)">
                        ✏️ تعديل
                    </button>
                    <button class="btn-action" onclick="toggleFeature('<?= $mId ?>')" style="<?= !empty($m['is_featured']) ? 'border-color: var(--tod-gold); color: var(--tod-gold);' : '' ?>">
                        <?= !empty($m['is_featured']) ? '★ بالبانر' : '☆ تمييز' ?>
                    </button>
                    <button class="btn-action" onclick="toggleHidden('<?= $mId ?>')" style="color: #ff6b6b;">
                        إخفاء
                    </button>
                </div>
            </div>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>
</div>

<!-- Modal 1: إضافة سيرفر بث -->
<div class="modal-overlay" id="addServerModal">
    <div class="modal">
        <h3 class="modal-title" id="modalMatchTitle">إضافة سيرفر مشاهدة جديد</h3>
        <input type="hidden" id="modalMatchId">
        <div class="form-group">
            <label class="form-label">اسم السيرفر / القناة (مثال: سيرفر 1 beIN 1 FHD):</label>
            <input type="text" id="modalServerName" class="form-input" placeholder="سيرفر 1 (FHD 1080p)">
        </div>
        <div class="form-group">
            <label class="form-label">رابط البث المباشر (HLS / m3u8 / MP4):</label>
            <input type="text" id="modalServerUrl" class="form-input" placeholder="https://example.com/live/ch1/master.m3u8" dir="ltr">
        </div>
        <div class="form-group">
            <label class="form-label">الجودة المعروضة في التطبيق:</label>
            <select id="modalQuality" class="form-input">
                <option value="FHD" selected>1080p FHD (جودة عالية)</option>
                <option value="4K">4K Ultra HD</option>
                <option value="HD">720p HD</option>
                <option value="SD">480p SD (سريع للإنترنت الضعيف)</option>
                <option value="Audio">بث صوتي (تعليق فقط)</option>
            </select>
        </div>
        <div class="modal-btns">
            <button class="btn-save" onclick="saveServer()">حفظ السيرفر وتفعيله بالتطبيق</button>
            <button class="btn-cancel" onclick="closeModal('addServerModal')">إلغاء</button>
        </div>
    </div>
</div>

<!-- Modal 2: تعديل تفاصيل المباراة (النتيجة، الدقيقة، المعلق، القناة) -->
<div class="modal-overlay" id="editMatchModal">
    <div class="modal">
        <h3 class="modal-title">✏️ تعديل بيانات ونتيجة المباراة</h3>
        <input type="hidden" id="editMatchId">
        <div class="form-group">
            <label class="form-label" id="editMatchTitleLabel">المباراة:</label>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">أهداف الفريق المضيف:</label>
                <input type="number" id="editHomeScore" class="form-input" min="0" placeholder="0">
            </div>
            <div class="form-group">
                <label class="form-label">أهداف الفريق الضيف:</label>
                <input type="number" id="editAwayScore" class="form-input" min="0" placeholder="0">
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">حالة المباراة:</label>
                <select id="editStatus" class="form-input">
                    <option value="1">لم تبدأ بعد</option>
                    <option value="2">مباشر - الشوط الأول</option>
                    <option value="3">مباشر - الشوط الثاني</option>
                    <option value="4">انتهت المباراة</option>
                    <option value="5">تأجلت المباراة</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">الدقيقة الحالية (مثال: '78):</label>
                <input type="text" id="editScoreTime" class="form-input" placeholder="'75">
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">القناة الناقلة:</label>
                <input type="text" id="editChannel" class="form-input" placeholder="beIN SPORTS 1 HD">
            </div>
            <div class="form-group">
                <label class="form-label">المعلق الرياضي:</label>
                <input type="text" id="editCommentator" class="form-input" placeholder="عصام الشوالي">
            </div>
        </div>
        <div class="form-group">
            <label class="form-label">الملعب:</label>
            <input type="text" id="editStadium" class="form-input" placeholder="الملعب الرئيسي">
        </div>
        <div class="modal-btns">
            <button class="btn-save" onclick="saveMatchEdit()">حفظ التعديلات</button>
            <button class="btn-cancel" onclick="closeModal('editMatchModal')">إلغاء</button>
        </div>
    </div>
</div>

<!-- Modal 3: فحص واختبار سيرفر البث بمشغل الفيديو المدمج -->
<div class="modal-overlay" id="previewPlayerModal">
    <div class="modal" style="max-width: 600px;">
        <h3 class="modal-title" id="previewTitle">فحص مشغل البث التجريبي</h3>
        <div id="videoPlayerBox">
            <video id="previewVideo" controls autoplay playsinline></video>
        </div>
        <div style="font-size: 12px; color: var(--text-muted); word-break: break-all; margin-bottom: 14px;" id="previewUrlText"></div>
        <div class="modal-btns">
            <button class="btn-cancel" onclick="closePreviewPlayer()">إغلاق المشغل</button>
        </div>
    </div>
</div>

<!-- Modal 4: إضافة مباراة مخصصة جديدة -->
<div class="modal-overlay" id="customMatchModal">
    <div class="modal">
        <h3 class="modal-title">+ إضافة مباراة مخصصة جديدة</h3>
        <div class="form-group">
            <label class="form-label">اسم البطولة أو الدوري:</label>
            <input type="text" id="custChamp" class="form-input" placeholder="دوري أبطال أوروبا">
        </div>
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">الفريق الأول (المستضيف):</label>
                <input type="text" id="custHome" class="form-input" placeholder="ريال مدريد">
            </div>
            <div class="form-group">
                <label class="form-label">الفريق الثاني (الضيف):</label>
                <input type="text" id="custAway" class="form-input" placeholder="برشلونة">
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label class="form-label">وقت المباراة (بتوقيت مكة):</label>
                <input type="time" id="custTime" class="form-input" value="22:00">
            </div>
            <div class="form-group">
                <label class="form-label">تاريخ المباراة:</label>
                <input type="date" id="custDate" class="form-input" value="<?= $targetDate ?>">
            </div>
        </div>
        <div class="form-group">
            <label class="form-label">رابط سيرفر البث (اختياري):</label>
            <input type="text" id="custStreamUrl" class="form-input" placeholder="https://example.com/stream.m3u8" dir="ltr">
        </div>
        <div class="modal-btns">
            <button class="btn-save" onclick="saveCustomMatch()">إضافة المباراة للجدول</button>
            <button class="btn-cancel" onclick="closeModal('customMatchModal')">إلغاء</button>
        </div>
    </div>
</div>

<script>
let hlsInstance = null;

function filterMatches() {
    const q = document.getElementById('matchSearch').value.toLowerCase().trim();
    const champ = document.getElementById('champFilter').value;
    const rows = document.querySelectorAll('.match-row');

    rows.forEach(r => {
        const names = r.getAttribute('data-names') || '';
        const rowChamp = r.getAttribute('data-champ') || '';

        const matchesQuery = !q || names.includes(q);
        const matchesChamp = champ === 'ALL' || rowChamp === champ;

        r.style.display = (matchesQuery && matchesChamp) ? 'grid' : 'none';
    });
}

function openAddServerModal(matchId, matchTitle) {
    document.getElementById('modalMatchId').value = matchId;
    document.getElementById('modalMatchTitle').innerText = 'إضافة سيرفر لـ: ' + matchTitle;
    document.getElementById('modalServerName').value = 'سيرفر 1 (FHD)';
    document.getElementById('modalServerUrl').value = '';
    document.getElementById('addServerModal').style.display = 'flex';
}

function openEditMatchModal(data) {
    document.getElementById('editMatchId').value = data.id;
    document.getElementById('editMatchTitleLabel').innerText = 'المباراة: ' + data.title;
    document.getElementById('editHomeScore').value = data.home_scores !== undefined && data.home_scores !== null ? data.home_scores : '';
    document.getElementById('editAwayScore').value = data.away_scores !== undefined && data.away_scores !== null ? data.away_scores : '';
    document.getElementById('editStatus').value = data.status || 1;
    document.getElementById('editScoreTime').value = data.score_time || '';
    document.getElementById('editChannel').value = data.channel_name || '';
    document.getElementById('editCommentator').value = data.commentator || '';
    document.getElementById('editStadium').value = data.stadium || '';
    document.getElementById('editMatchModal').style.display = 'flex';
}

function openCustomMatchModal() {
    document.getElementById('customMatchModal').style.display = 'flex';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function saveServer() {
    const matchId = document.getElementById('modalMatchId').value;
    const serverName = document.getElementById('modalServerName').value.trim();
    const serverUrl = document.getElementById('modalServerUrl').value.trim();
    const quality = document.getElementById('modalQuality').value;

    if (!serverUrl) {
        alert('يرجى إدخال رابط البث المباشر!');
        return;
    }

    fetch('?action=save_server', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            action: 'save_server',
            match_id: matchId,
            server_name: serverName || 'سيرفر بث',
            stream_url: serverUrl,
            quality: quality
        })
    }).then(r => r.json()).then(res => {
        if (res.status) {
            location.reload();
        } else {
            alert(res.message || 'حدث خطأ أثناء الحفظ');
        }
    }).catch(e => alert('خطأ في الاتصال: ' + e));
}

function saveMatchEdit() {
    const matchId = document.getElementById('editMatchId').value;
    const homeScore = document.getElementById('editHomeScore').value;
    const awayScore = document.getElementById('editAwayScore').value;
    const status = document.getElementById('editStatus').value;
    const scoreTime = document.getElementById('editScoreTime').value;
    const channelName = document.getElementById('editChannel').value;
    const commentator = document.getElementById('editCommentator').value;
    const stadium = document.getElementById('editStadium').value;

    fetch('?action=edit_match', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            action: 'edit_match',
            match_id: matchId,
            home_scores: homeScore,
            away_scores: awayScore,
            status: status,
            score_time: scoreTime,
            channel_name: channelName,
            commentator: commentator,
            stadium: stadium
        })
    }).then(r => r.json()).then(res => {
        if (res.status) {
            location.reload();
        } else {
            alert(res.message || 'حدث خطأ');
        }
    });
}

function saveCustomMatch() {
    const champ = document.getElementById('custChamp').value.trim();
    const home = document.getElementById('custHome').value.trim();
    const away = document.getElementById('custAway').value.trim();
    const time = document.getElementById('custTime').value.trim();
    const date = document.getElementById('custDate').value.trim();
    const stream = document.getElementById('custStreamUrl').value.trim();

    if (!home || !away) {
        alert('يرجى إدخال اسم الفريقين!');
        return;
    }

    fetch('?action=add_custom_match', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            action: 'add_custom_match',
            championship: champ || 'مباراة خاصة',
            home_team: home,
            away_team: away,
            match_time: time || '21:00:00',
            match_date: date || '<?= $todayDate ?>',
            stream_url: stream
        })
    }).then(r => r.json()).then(res => {
        if (res.status) {
            location.reload();
        } else {
            alert(res.message || 'خطأ في الإضافة');
        }
    });
}

function deleteServer(matchId, serverId) {
    if (!confirm('هل تريد بالتأكيد حذف سيرفر المشاهدة هذا؟')) return;
    fetch('?action=delete_server', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            action: 'delete_server',
            match_id: matchId,
            server_id: serverId
        })
    }).then(r => r.json()).then(res => {
        if (res.status) location.reload();
    });
}

function toggleFeature(matchId) {
    fetch('?action=toggle_feature', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'toggle_feature', match_id: matchId })
    }).then(r => r.json()).then(res => {
        if (res.status) location.reload();
    });
}

function toggleHidden(matchId) {
    if (!confirm('هل أنت متأكد من إخفاء هذه المباراة؟ لن تظهر في التطبيق.')) return;
    fetch('?action=toggle_hidden', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'toggle_hidden', match_id: matchId })
    }).then(r => r.json()).then(res => {
        if (res.status) location.reload();
    });
}

// مشغل الفيديو التجريبي
function previewStream(url, name) {
    const modal = document.getElementById('previewPlayerModal');
    const video = document.getElementById('previewVideo');
    document.getElementById('previewTitle').innerText = 'فحص البث: ' + name;
    document.getElementById('previewUrlText').innerText = url;
    modal.style.display = 'flex';

    if (hlsInstance) {
        hlsInstance.destroy();
        hlsInstance = null;
    }

    if (Hls.isSupported() && url.includes('.m3u8')) {
        hlsInstance = new Hls();
        hlsInstance.loadSource(url);
        hlsInstance.attachMedia(video);
        hlsInstance.on(Hls.Events.MANIFEST_PARSED, function() {
            video.play();
        });
    } else {
        video.src = url;
        video.play();
    }
}

function closePreviewPlayer() {
    const video = document.getElementById('previewVideo');
    video.pause();
    video.src = '';
    if (hlsInstance) {
        hlsInstance.destroy();
        hlsInstance = null;
    }
    document.getElementById('previewPlayerModal').style.display = 'none';
}

// تحديث النتائج والدقائق تلقائياً كل 25 ثانية
setInterval(() => {
    fetch('?action=live_scores&date=<?= $targetDate ?>')
        .then(r => r.json())
        .then(res => {
            if (res.status && res.live_scores) {
                // تحديث مباشر بدون إعادة تحميل
                res.live_scores.forEach(s => {
                    const row = document.querySelector(`[onclick*="'${s.id}'"]`)?.closest('.match-row');
                    if (row) {
                        const scorePills = row.querySelectorAll('.score-pill');
                        if (scorePills.length >= 2) {
                            if (s.home_scores !== null) scorePills[0].innerText = s.home_scores;
                            if (s.away_scores !== null) scorePills[1].innerText = s.away_scores;
                        }
                    }
                });
            }
        }).catch(() => {});
}, 25000);
</script>
</body>
</html>
