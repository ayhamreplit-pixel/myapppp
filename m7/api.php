<?php
/**
 * =========================================================================
 *  لوحة تحكم وسيرفر HERO Cast / TOD الرياضي الشامل (m7)
 *  المسار: /m7/api.php أو /m7/index.php
 *  
 *  المميزات الرئيسية:
 *  1. إدارة المباريات والبث: تفعيل/تعطيل البث بزر مباشر، إضافة وتعديل عدة سيرفرات (4K, FHD, HD, Low).
 *  2. تعديل فوري للنتائج والدقائق، المعلقين، القنوات، والملاعب مع دعم إضافة مباريات مخصصة.
 *  3. إدارة القنوات التلفزيونية المباشرة: إضافة، تعديل، وحذف القنوات مع تحديد القسم واللوغو.
 *  4. إدارة البوسترات والسلايدر (Hero Banners): إضافة وحذف البوسترات الترويجية.
 *  5. نظام الإشعارات والتنبيهات الفورية: إرسال تنبيه مباشر لجميع مستخدمي التطبيق مع ربطه بمباراة أو قناة.
 *  6. مشغل فيديو مدمج (HLS / m3u8 / MP4) لفحص وتجربة السيرفرات قبل النشر.
 *  7. كاش ذكي فائق السرعة لمنع حظر IP من مصادر المباريات والأخبار.
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

// إعداد مسار حفظ ملفات البيانات داخل مجلد m7
$dataDir = __DIR__ . '/data';
if (!is_dir($dataDir)) {
    @mkdir($dataDir, 0777, true);
}

$overridesFile = $dataDir . '/match_overrides.json';
$customMatchesFile = $dataDir . '/custom_matches.json';
$channelsFile = $dataDir . '/channels.json';
$sliderFile = $dataDir . '/slider.json';
$announcementFile = $dataDir . '/announcement.json';

// استيراد القنوات الافتراضية إذا لم تكن موجودة
if (!file_exists($channelsFile)) {
    $defaultChannels = [
        ["streamId" => "ch_bein1", "name" => "beIN SPORTS 1 HD", "iconUrl" => "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/BeIN_Sports_1_logo.png/512px-BeIN_Sports_1_logo.png", "categoryId" => "قنوات beIN SPORTS", "playUrl" => "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "quality" => "1080p FHD"],
        ["streamId" => "ch_bein2", "name" => "beIN SPORTS 2 HD", "iconUrl" => "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/BeIN_Sports_2_logo.png/512px-BeIN_Sports_2_logo.png", "categoryId" => "قنوات beIN SPORTS", "playUrl" => "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "quality" => "1080p FHD"],
        ["streamId" => "ch_bein3", "name" => "beIN SPORTS 3 HD", "iconUrl" => "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/BeIN_Sports_2_logo.png/512px-BeIN_Sports_2_logo.png", "categoryId" => "قنوات beIN SPORTS", "playUrl" => "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "quality" => "1080p FHD"],
        ["streamId" => "ch_alkass1", "name" => "Alkass One HD", "iconUrl" => "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e6/Al_Kass_logo.png/512px-Al_Kass_logo.png", "categoryId" => "قنوات الكأس", "playUrl" => "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "quality" => "720p HD"]
    ];
    @file_put_contents($channelsFile, json_encode($defaultChannels, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
}

// استيراد السلايدر الافتراضي إذا لم يكن موجوداً
if (!file_exists($sliderFile)) {
    $defaultSlider = [
        ["id" => "b1", "title" => "دوري أبطال أوروبا • القمة النارية", "image" => "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=1200&auto=format&fit=crop", "link" => ""],
        ["id" => "b2", "title" => "دوري روشن السعودي • الجولة المرتقبة", "image" => "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=1200&auto=format&fit=crop", "link" => ""]
    ];
    @file_put_contents($sliderFile, json_encode($defaultSlider, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
}

// استيراد ملف الإشعارات
if (!file_exists($announcementFile)) {
    $defaultAnnouncement = [
        "enabled" => false,
        "id" => "notif_" . time(),
        "title" => "إشعار عاجل",
        "message" => "",
        "target_type" => "none",
        "target_id" => ""
    ];
    @file_put_contents($announcementFile, json_encode($defaultAnnouncement, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
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
    $cacheTTL = ($targetDate === $todayDate) ? 20 : 600;

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
    curl_setopt($ch, CURLOPT_TIMEOUT, 8);
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

// معالجة التاريخ والـ action
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
// معالجة طلبات POST (تعديل السيرفرات، القنوات، البوسترات، والإشعارات)
// -------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    header('Content-Type: application/json; charset=utf-8');
    $input = json_decode(file_get_contents('php://input'), true);
    if (!$input) {
        $input = $_POST;
    }

    $postAction = isset($input['action']) ? $input['action'] : $action;
    $overrides = getJsonData($overridesFile);

    // 1. تفعيل / تعطيل البث المباشر لمباراة
    if ($postAction === 'toggle_stream') {
        $matchId = trim($input['match_id'] ?? '');
        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false, "stream_active" => false];
        }
        $newState = isset($input['stream_active']) ? (bool)$input['stream_active'] : !($overrides[$matchId]['stream_active'] ?? false);
        $overrides[$matchId]['stream_active'] = $newState;

        // عند تفعيل البث، إذا كانت المباراة لا تمتلك سيرفرات بعد، نضيف لها تلقائياً باقة سيرفرات سريعة
        if ($newState && empty($overrides[$matchId]['servers'])) {
            $overrides[$matchId]['servers'] = [
                [
                    'id' => 'srv_' . time() . '_1',
                    'name' => 'سيرفر TOD الرئيسي (FHD)',
                    'url' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
                    'quality' => '1080p FHD'
                ],
                [
                    'id' => 'srv_' . time() . '_2',
                    'name' => 'سيرفر beIN 4K فائق السرعة',
                    'url' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
                    'quality' => '4K UHD'
                ],
                [
                    'id' => 'srv_' . time() . '_3',
                    'name' => 'سيرفر الجوال والأجهزة الضعيفة (HD)',
                    'url' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
                    'quality' => '720p HD'
                ]
            ];
        }
        saveJsonData($overridesFile, $overrides);
        echo json_encode([
            "status" => true,
            "stream_active" => $newState,
            "servers" => $overrides[$matchId]['servers'] ?? [],
            "message" => $newState ? "تم تفعيل البث المباشر وإضافة سيرفرات المشاهدة بنجاح ⚡" : "تم تعطيل البث للمباراة"
        ]);
        exit;
    }

    // 2. إضافة / تعديل سيرفر بث لمباراة
    if ($postAction === 'save_server') {
        $matchId = trim($input['match_id'] ?? '');
        $serverName = trim($input['server_name'] ?? 'سيرفر 1');
        $serverUrl = trim($input['stream_url'] ?? '');
        $quality = trim($input['quality'] ?? 'FHD');
        $serverId = trim($input['server_id'] ?? '');

        if (empty($matchId) || empty($serverUrl)) {
            echo json_encode(["status" => false, "message" => "يرجى إدخال رابط البث"]);
            exit;
        }

        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false, "stream_active" => true];
        }
        $overrides[$matchId]['stream_active'] = true; // تلقائياً تفعيل البث عند إضافة سيرفر

        if (!isset($overrides[$matchId]['servers'])) {
            $overrides[$matchId]['servers'] = [];
        }

        if (!empty($serverId)) {
            foreach ($overrides[$matchId]['servers'] as &$srv) {
                if ($srv['id'] === $serverId) {
                    $srv['name'] = $serverName;
                    $srv['url'] = $serverUrl;
                    $srv['quality'] = $quality;
                    break;
                }
            }
        } else {
            $newId = 'srv_' . time() . '_' . rand(100, 999);
            $overrides[$matchId]['servers'][] = [
                'id' => $newId,
                'name' => $serverName,
                'url' => $serverUrl,
                'quality' => $quality
            ];
        }

        saveJsonData($overridesFile, $overrides);
        echo json_encode(["status" => true, "message" => "تم حفظ وتفعيل سيرفر البث بنجاح", "servers" => $overrides[$matchId]['servers']]);
        exit;
    }

    // 3. حذف سيرفر بث
    if ($postAction === 'delete_server') {
        $matchId = trim($input['match_id'] ?? '');
        $serverId = trim($input['server_id'] ?? '');
        if (isset($overrides[$matchId]['servers'])) {
            $overrides[$matchId]['servers'] = array_values(array_filter($overrides[$matchId]['servers'], function($s) use ($serverId) {
                return $s['id'] !== $serverId;
            }));
            if (empty($overrides[$matchId]['servers'])) {
                $overrides[$matchId]['stream_active'] = false;
            }
            saveJsonData($overridesFile, $overrides);
        }
        echo json_encode(["status" => true, "message" => "تم حذف السيرفر"]);
        exit;
    }

    // 4. تعديل بيانات المباراة
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
        }
        if (isset($input['away_scores']) && $input['away_scores'] !== '') {
            $overrides[$matchId]['away_scores'] = intval($input['away_scores']);
        }
        if (isset($input['status']) && $input['status'] !== '') {
            $overrides[$matchId]['status'] = intval($input['status']);
            $overrides[$matchId]['live'] = in_array(intval($input['status']), [2, 3]) ? 1 : 0;
        }
        if (isset($input['score_time'])) $overrides[$matchId]['score_time'] = trim($input['score_time']);
        if (isset($input['channel_name'])) $overrides[$matchId]['channel_name'] = trim($input['channel_name']);
        if (isset($input['commentator'])) $overrides[$matchId]['commentator'] = trim($input['commentator']);
        if (isset($input['stadium'])) $overrides[$matchId]['stadium'] = trim($input['stadium']);

        saveJsonData($overridesFile, $overrides);
        echo json_encode(["status" => true, "message" => "تم حفظ التعديلات بنجاح"]);
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
            'championship' => ['title' => trim($input['championship'] ?? 'مباراة خاصة'), 'image' => trim($input['championship_image'] ?? '')],
            'home_team' => ['title' => trim($input['home_team'] ?? 'الفريق الأول'), 'image' => trim($input['home_team_image'] ?? '')],
            'away_team' => ['title' => trim($input['away_team'] ?? 'الفريق الثاني'), 'image' => trim($input['away_team_image'] ?? '')],
            'match_time' => trim($input['match_time'] ?? '21:00:00'),
            'match_date' => trim($input['match_date'] ?? $todayDate),
            'status' => intval($input['status'] ?? 1),
            'live' => intval($input['live'] ?? 0),
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
        echo json_encode(["status" => true, "message" => "تمت إضافة المباراة بنجاح"]);
        exit;
    }

    // 7. إدارة القنوات (Save Channel)
    if ($postAction === 'save_channel') {
        $channels = getJsonData($channelsFile);
        $chId = trim($input['streamId'] ?? ('ch_' . time()));
        $chName = trim($input['name'] ?? '');
        $chPlayUrl = trim($input['playUrl'] ?? '');
        $chIcon = trim($input['iconUrl'] ?? '');
        $chCategory = trim($input['categoryId'] ?? 'قنوات beIN SPORTS');
        $chQuality = trim($input['quality'] ?? '1080p FHD');

        if (empty($chName) || empty($chPlayUrl)) {
            echo json_encode(["status" => false, "message" => "يرجى كتابة اسم القناة ورابط البث"]);
            exit;
        }

        $exists = false;
        foreach ($channels as &$ch) {
            if ($ch['streamId'] === $chId) {
                $ch['name'] = $chName;
                $ch['playUrl'] = $chPlayUrl;
                $ch['iconUrl'] = $chIcon;
                $ch['categoryId'] = $chCategory;
                $ch['quality'] = $chQuality;
                $exists = true;
                break;
            }
        }
        if (!$exists) {
            $channels[] = [
                'streamId' => $chId,
                'name' => $chName,
                'playUrl' => $chPlayUrl,
                'iconUrl' => $chIcon,
                'categoryId' => $chCategory,
                'quality' => $chQuality
            ];
        }
        saveJsonData($channelsFile, $channels);
        echo json_encode(["status" => true, "message" => "تم حفظ القناة بنجاح"]);
        exit;
    }

    // 8. حذف قناة (Delete Channel)
    if ($postAction === 'delete_channel') {
        $chId = trim($input['streamId'] ?? '');
        $channels = getJsonData($channelsFile);
        $channels = array_values(array_filter($channels, function($c) use ($chId) { return $c['streamId'] !== $chId; }));
        saveJsonData($channelsFile, $channels);
        echo json_encode(["status" => true, "message" => "تم حذف القناة بنجاح"]);
        exit;
    }

    // 9. إدارة البوسترات والسلايدر (Save Banner)
    if ($postAction === 'save_slider') {
        $slider = getJsonData($sliderFile);
        $bId = trim($input['id'] ?? ('b_' . time()));
        $bTitle = trim($input['title'] ?? '');
        $bImage = trim($input['image'] ?? '');
        $bLink = trim($input['link'] ?? '');

        if (empty($bImage)) {
            echo json_encode(["status" => false, "message" => "يرجى إدخال رابط صورة البوستر"]);
            exit;
        }

        $exists = false;
        foreach ($slider as &$b) {
            if ($b['id'] === $bId) {
                $b['title'] = $bTitle;
                $b['image'] = $bImage;
                $b['link'] = $bLink;
                $exists = true;
                break;
            }
        }
        if (!$exists) {
            $slider[] = ['id' => $bId, 'title' => $bTitle, 'image' => $bImage, 'link' => $bLink];
        }
        saveJsonData($sliderFile, $slider);
        echo json_encode(["status" => true, "message" => "تم حفظ البوستر بنجاح"]);
        exit;
    }

    // 10. حذف بوستر (Delete Banner)
    if ($postAction === 'delete_slider') {
        $bId = trim($input['id'] ?? '');
        $slider = getJsonData($sliderFile);
        $slider = array_values(array_filter($slider, function($b) use ($bId) { return $b['id'] !== $bId; }));
        saveJsonData($sliderFile, $slider);
        echo json_encode(["status" => true, "message" => "تم حذف البوستر"]);
        exit;
    }

    // 11. إرسال وتحديث الإشعار المباشر (Save Announcement / Push Notification)
    if ($postAction === 'save_announcement') {
        $notif = [
            "enabled" => isset($input['enabled']) ? (bool)$input['enabled'] : true,
            "id" => "notif_" . time(),
            "title" => trim($input['title'] ?? 'تنبيه مباشر'),
            "message" => trim($input['message'] ?? ''),
            "target_type" => trim($input['target_type'] ?? 'none'), // 'match', 'channel', 'news', 'none'
            "target_id" => trim($input['target_id'] ?? '')
        ];
        saveJsonData($announcementFile, $notif);
        echo json_encode(["status" => true, "message" => "تم نشر الإشعار لجميع مستخدمي التطبيق بنجاح", "data" => $notif]);
        exit;
    }

    // 12. إيقاف / مسح الإشعار
    if ($postAction === 'clear_announcement') {
        $notif = [
            "enabled" => false,
            "id" => "notif_" . time(),
            "title" => "",
            "message" => "",
            "target_type" => "none",
            "target_id" => ""
        ];
        saveJsonData($announcementFile, $notif);
        echo json_encode(["status" => true, "message" => "تم تعطيل الإشعار"]);
        exit;
    }
}

// -------------------------------------------------------------------------
// طلبات GET الخاصة بالـ API
// -------------------------------------------------------------------------

// 1. القنوات المباشرة API
if ($action === 'channels') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "channels" => getJsonData($channelsFile)], JSON_UNESCAPED_UNICODE);
    exit;
}

// 2. السلايدر والبوسترات API
if ($action === 'slider') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "slider" => getJsonData($sliderFile)], JSON_UNESCAPED_UNICODE);
    exit;
}

// 3. الإشعارات الحية API
if ($action === 'announcement') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "announcement" => getJsonData($announcementFile)], JSON_UNESCAPED_UNICODE);
    exit;
}

// 4. الأخبار الرياضية API
if ($action === 'news') {
    header('Content-Type: application/json; charset=utf-8');
    $newsCache = sys_get_temp_dir() . "/sports_news.json";
    if (file_exists($newsCache) && (time() - filemtime($newsCache) < 300)) {
        echo file_get_contents($newsCache);
        exit;
    }
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "https://sportfeeds.gemini.media/yallakoraapi/NewsList?pageIndex=1&pageSize=30&otherSportsNews=false");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 6);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    $newsResp = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode === 200 && !empty($newsResp)) {
        @file_put_contents($newsCache, $newsResp);
        echo $newsResp;
        exit;
    }
    if (file_exists($newsCache)) {
        echo file_get_contents($newsCache);
        exit;
    }
    echo json_encode(["status" => true, "news" => []]);
    exit;
}

// 5. تفريغ الكاش
if ($action === 'clear_cache') {
    $cacheDir = sys_get_temp_dir() . "/kora_cache";
    $cacheFile = $cacheDir . "/matches_" . $targetDate . ".json";
    if (file_exists($cacheFile)) @unlink($cacheFile);
    $forceRefresh = true;
}

// -------------------------------------------------------------------------
// جلب ودمج بيانات المباريات والسيرفرات
// -------------------------------------------------------------------------
$rawMatches = fetchYsscoresMatches($targetDate, $forceRefresh);
$overrides = getJsonData($overridesFile);
$customMatches = getJsonData($customMatchesFile);

$enrichedMatches = [];

// إضافة المباريات المخصصة
foreach ($customMatches as $cm) {
    if (($cm['match_date'] ?? $todayDate) === $targetDate) {
        $cmId = strval($cm['match_id']);
        if (isset($overrides[$cmId]) && !empty($overrides[$cmId]['hidden'])) continue;
        if (isset($overrides[$cmId])) {
            $ov = $overrides[$cmId];
            if (!empty($ov['servers'])) $cm['servers'] = $ov['servers'];
            if (isset($ov['stream_active'])) $cm['stream_active'] = $ov['stream_active'] ? 1 : 0;
            if (isset($ov['home_scores'])) $cm['home_scores'] = $ov['home_scores'];
            if (isset($ov['away_scores'])) $cm['away_scores'] = $ov['away_scores'];
            if (isset($ov['status'])) $cm['status'] = $ov['status'];
            if (isset($ov['live'])) $cm['live'] = $ov['live'];
            if (!empty($ov['score_time'])) $cm['score_time'] = $ov['score_time'];
            if (!empty($ov['channel_name'])) $cm['channel_name'] = $ov['channel_name'];
            if (!empty($ov['commentator'])) $cm['commentator'] = $ov['commentator'];
            if (!empty($ov['stadium'])) $cm['stadium'] = $ov['stadium'];
        }
        $cm['is_stream_active'] = !empty($cm['servers']) || !empty($cm['stream_active']);
        if (!empty($cm['servers'])) $cm['streamUrl'] = $cm['servers'][0]['url'];
        $enrichedMatches[] = $cm;
    }
}

// دمج مباريات YSScores
foreach ($rawMatches as $m) {
    $mId = strval($m['match_id']);
    if (isset($overrides[$mId]) && !empty($overrides[$mId]['hidden'])) continue;

    $m['servers'] = [];
    $m['is_stream_active'] = false;

    if (isset($overrides[$mId])) {
        $ov = $overrides[$mId];
        $isStreamActive = !empty($ov['stream_active']);
        $m['is_stream_active'] = $isStreamActive;
        if ($isStreamActive && !empty($ov['servers'])) {
            $m['servers'] = $ov['servers'];
            $m['streamUrl'] = $ov['servers'][0]['url'];
        } else {
            $m['servers'] = [];
            $m['streamUrl'] = '';
            $m['is_stream_active'] = false;
        }
        if (isset($ov['home_scores'])) $m['home_scores'] = $ov['home_scores'];
        if (isset($ov['away_scores'])) $m['away_scores'] = $ov['away_scores'];
        if (isset($ov['status'])) {
            $m['status'] = $ov['status'];
            $m['live'] = in_array(intval($ov['status']), [2, 3]) ? 1 : 0;
        }
        if (!empty($ov['score_time'])) $m['score_time'] = $ov['score_time'];
        if (!empty($ov['channel_name'])) $m['channel_name'] = $ov['channel_name'];
        if (!empty($ov['commentator'])) $m['commentator'] = $ov['commentator'];
        if (!empty($ov['stadium'])) $m['stadium'] = $ov['stadium'];
    }

    $enrichedMatches[] = $m;
}

// إذا كان الطلب من تطبيق الأندرويد (JSON API)
$isJsonRequest = (isset($_SERVER['HTTP_ACCEPT']) && strpos($_SERVER['HTTP_ACCEPT'], 'application/json') !== false) ||
                 (isset($_SERVER['HTTP_USER_AGENT']) && (strpos($_SERVER['HTTP_USER_AGENT'], 'TOD') !== false || strpos($_SERVER['HTTP_USER_AGENT'], 'okhttp') !== false)) ||
                 (isset($_GET['format']) && $_GET['format'] === 'json') ||
                 ($action === 'matches');

if ($isJsonRequest) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        "status" => true,
        "date" => $targetDate,
        "count" => count($enrichedMatches),
        "data" => $enrichedMatches,
        "announcement" => getJsonData($announcementFile)
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// -------------------------------------------------------------------------
// واجهة لوحة التحكم الشاملة (Web Dashboard HTML/JS/CSS)
// -------------------------------------------------------------------------
$currentAnnounce = getJsonData($announcementFile);
$currentChannels = getJsonData($channelsFile);
$currentSlider = getJsonData($sliderFile);
?>
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>لوحة تحكم HERO Cast & TOD الشاملة</title>
    <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
    <style>
        :root {
            --bg-dark: #07090e;
            --card-bg: #101422;
            --card-border: #1e263d;
            --gold: #ffb800;
            --gold-hover: #e0a300;
            --blue: #0a84ff;
            --green: #30d158;
            --red: #ff453a;
            --text-main: #ffffff;
            --text-muted: #8e9bae;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Cairo', sans-serif; }
        body { background-color: var(--bg-dark); color: var(--text-main); min-height: 100vh; padding-bottom: 60px; }
        .header-bar {
            background: linear-gradient(180deg, #161b2e 0%, #0d101a 100%);
            border-bottom: 1px solid var(--card-border);
            padding: 16px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        .brand { display: flex; align-items: center; gap: 12px; }
        .brand-icon {
            width: 44px; height: 44px; background: linear-gradient(135deg, var(--gold) 0%, #ff8c00 100%);
            border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #000; font-weight: 900;
        }
        .brand-title h1 { font-size: 20px; font-weight: 900; color: var(--gold); }
        .brand-title p { font-size: 12px; color: var(--text-muted); }
        
        /* Navigation Tabs */
        .main-tabs { display: flex; gap: 8px; background: #0c0f18; padding: 6px; border-radius: 14px; border: 1px solid var(--card-border); }
        .main-tab-btn {
            background: transparent; border: none; color: var(--text-muted); padding: 8px 18px; border-radius: 10px; font-size: 13.5px; font-weight: 700; cursor: pointer; transition: 0.2s;
        }
        .main-tab-btn.active { background: var(--gold); color: #000; }

        .container { max-width: 1400px; margin: 20px auto; padding: 0 16px; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* General UI components */
        .card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 18px; padding: 20px; margin-bottom: 20px; }
        .btn {
            background: var(--gold); color: #000; border: none; padding: 9px 18px; border-radius: 10px; font-weight: 700; font-size: 13px; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; gap: 6px;
        }
        .btn:hover { background: var(--gold-hover); transform: translateY(-1px); }
        .btn-blue { background: var(--blue); color: #fff; }
        .btn-blue:hover { background: #0070e0; }
        .btn-red { background: rgba(255, 69, 58, 0.15); color: var(--red); border: 1px solid var(--red); }
        .btn-red:hover { background: var(--red); color: #fff; }
        .btn-green { background: rgba(48, 209, 88, 0.15); color: var(--green); border: 1px solid var(--green); }
        .btn-green:hover { background: var(--green); color: #000; }
        .btn-outline { background: transparent; color: #fff; border: 1px solid var(--card-border); }
        .btn-outline:hover { background: rgba(255,255,255,0.05); }

        /* Matches Grid */
        .date-pills { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 16px; }
        .date-pill {
            background: #151a2b; border: 1px solid var(--card-border); color: #fff; padding: 8px 16px; border-radius: 12px; font-weight: 700; text-decoration: none; font-size: 13px; white-space: nowrap; transition: 0.2s;
        }
        .date-pill.active { background: var(--gold); color: #000; border-color: var(--gold); }

        .matches-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 16px; }
        .match-card {
            background: #121727; border: 1px solid var(--card-border); border-radius: 16px; padding: 16px; position: relative; transition: 0.2s;
        }
        .match-card:hover { border-color: var(--gold); transform: translateY(-2px); }
        .match-card.live-match { border-left: 4px solid var(--green); background: linear-gradient(90deg, #131d2e 0%, #121727 100%); }
        .match-card.has-stream { border-color: rgba(255, 184, 0, 0.4); }

        .match-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 12px; color: var(--text-muted); }
        .live-tag { background: var(--green); color: #000; font-weight: 900; padding: 2px 8px; border-radius: 6px; font-size: 11px; }
        .stream-tag { background: rgba(255, 184, 0, 0.15); color: var(--gold); border: 1px solid var(--gold); padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 700; }
        .no-stream-tag { background: rgba(255, 255, 255, 0.08); color: var(--text-muted); padding: 2px 8px; border-radius: 6px; font-size: 11px; }

        .teams-row { display: flex; align-items: center; justify-content: space-between; margin: 14px 0; }
        .team-box { display: flex; flex-direction: column; align-items: center; gap: 6px; width: 40%; text-align: center; }
        .team-logo { width: 42px; height: 42px; object-fit: contain; }
        .team-name { font-size: 13.5px; font-weight: 700; }
        .score-box { font-size: 22px; font-weight: 900; color: var(--gold); background: #0c0f18; padding: 4px 14px; border-radius: 10px; }

        .match-actions { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 14px; padding-top: 12px; border-top: 1px solid rgba(255,255,255,0.06); }

        /* Modal */
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.8); z-index: 1000; align-items: center; justify-content: center; }
        .modal.show { display: flex; }
        .modal-content { background: #131828; border: 1px solid var(--gold); border-radius: 20px; width: 92%; max-width: 540px; max-height: 90vh; overflow-y: auto; padding: 24px; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; font-size: 12.5px; font-weight: 700; margin-bottom: 6px; color: var(--gold); }
        .form-control { width: 100%; background: #0c0f18; border: 1px solid var(--card-border); color: #fff; padding: 10px 14px; border-radius: 10px; font-size: 13.5px; }
        .form-control:focus { outline: none; border-color: var(--gold); }

        /* Table */
        .styled-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .styled-table th, .styled-table td { padding: 12px 14px; text-align: right; border-bottom: 1px solid var(--card-border); }
        .styled-table th { background: #0d111d; color: var(--gold); font-size: 13px; }
        .styled-table tr:hover { background: rgba(255,255,255,0.02); }
    </style>
</head>
<body>

<header class="header-bar">
    <div class="brand">
        <div class="brand-icon">⚡</div>
        <div class="brand-title">
            <h1>لوحة تحكم HERO Cast (m7)</h1>
            <p>سيرفر البث وإدارة المباريات والقنوات والإشعارات</p>
        </div>
    </div>

    <div class="main-tabs">
        <button class="main-tab-btn active" onclick="switchMainTab('matches')">⚽ المباريات والبث</button>
        <button class="main-tab-btn" onclick="switchMainTab('channels')">📺 القنوات المباشرة</button>
        <button class="main-tab-btn" onclick="switchMainTab('slider')">🖼️ البوسترات والسلايدر</button>
        <button class="main-tab-btn" onclick="switchMainTab('notifications')">🔔 الإشعارات والتنبيهات</button>
        <button class="main-tab-btn" onclick="switchMainTab('player')">▶️ مشغل الفيديو</button>
    </div>

    <div>
        <a href="?date=<?= $targetDate ?>&refresh=1" class="btn btn-blue">🔄 تحديث فوري للكاش</a>
    </div>
</header>

<div class="container">

    <!-- 1. TAB: MATCHES & STREAMS -->
    <div id="tab-matches" class="tab-content active">
        <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
            <div>
                <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">جدول المباريات وسيرفرات المشاهدة</h2>
                <p style="font-size: 12.5px; color: var(--text-muted);">تحكم كامل في تفعيل البث، النتائج، المعلقين، وإضافة سيرفرات 4K و FHD لكل مباراة</p>
            </div>
            <div style="display: flex; gap: 8px;">
                <button class="btn" onclick="openCustomMatchModal()">➕ إضافة مباراة خاصة</button>
            </div>
        </div>

        <!-- Date navigation -->
        <div class="date-pills">
            <a href="?date=yesterday" class="date-pill <?= $dateParam === 'yesterday' ? 'active' : '' ?>">أمس</a>
            <a href="?date=today" class="date-pill <?= ($dateParam === 'today' || empty($dateParam)) ? 'active' : '' ?>">مباريات اليوم ⚡</a>
            <a href="?date=tomorrow" class="date-pill <?= $dateParam === 'tomorrow' ? 'active' : '' ?>">مباريات الغد</a>
        </div>

        <div class="matches-grid">
            <?php foreach ($enrichedMatches as $m): 
                $mId = strval($m['match_id']);
                $hasServers = !empty($m['servers']);
                $isStreamActive = !empty($m['is_stream_active']);
                $isLive = !empty($m['live']) || in_array(intval($m['status'] ?? 1), [2, 3]);
            ?>
            <div class="match-card <?= $isLive ? 'live-match' : '' ?> <?= $isStreamActive ? 'has-stream' : '' ?>" id="card-<?= $mId ?>">
                <div class="match-header">
                    <span><?= htmlspecialchars($m['championship']['title'] ?? 'بطولة') ?></span>
                    <div>
                        <?php if ($isLive): ?>
                            <span class="live-tag">مباشر <?= htmlspecialchars($m['score_time'] ?? '') ?></span>
                        <?php else: ?>
                            <span><?= htmlspecialchars($m['match_time'] ?? '') ?></span>
                        <?php endif; ?>
                        
                        <?php if ($isStreamActive): ?>
                            <span class="stream-tag">⚡ البث مفعل (<?= count($m['servers']) ?>)</span>
                        <?php else: ?>
                            <span class="no-stream-tag">بدون بث</span>
                        <?php endif; ?>
                    </div>
                </div>

                <div class="teams-row">
                    <div class="team-box">
                        <img src="<?= htmlspecialchars($m['home_team']['image'] ?? '') ?>" class="team-logo" onerror="this.src='https://cdn-icons-png.flaticon.com/512/53/53283.png'">
                        <span class="team-name"><?= htmlspecialchars($m['home_team']['title'] ?? 'الفريق 1') ?></span>
                    </div>

                    <div class="score-box">
                        <?= ($m['home_scores'] ?? '-') ?> : <?= ($m['away_scores'] ?? '-') ?>
                    </div>

                    <div class="team-box">
                        <img src="<?= htmlspecialchars($m['away_team']['image'] ?? '') ?>" class="team-logo" onerror="this.src='https://cdn-icons-png.flaticon.com/512/53/53283.png'">
                        <span class="team-name"><?= htmlspecialchars($m['away_team']['title'] ?? 'الفريق 2') ?></span>
                    </div>
                </div>

                <div style="font-size: 11.5px; color: var(--text-muted); display: flex; justify-content: space-between; margin-bottom: 6px;">
                    <span>🎙️ <?= htmlspecialchars($m['commentator'] ?? 'تعليق عربي') ?></span>
                    <span>📺 <?= htmlspecialchars($m['channel_name'] ?? 'beIN SPORTS') ?></span>
                </div>

                <div class="match-actions">
                    <!-- Toggle Stream Active -->
                    <button class="btn <?= $isStreamActive ? 'btn-green' : 'btn-outline' ?>" onclick="toggleMatchStream('<?= $mId ?>', <?= $isStreamActive ? 'false' : 'true' ?>)">
                        <?= $isStreamActive ? '🟢 البث شغال' : '⚪ تفعيل البث' ?>
                    </button>

                    <!-- Add/Manage Servers -->
                    <button class="btn btn-blue" onclick="openServersModal('<?= $mId ?>', '<?= addslashes($m['home_team']['title'] . ' ضد ' . $m['away_team']['title']) ?>', <?= htmlspecialchars(json_encode($m['servers'])) ?>)">
                        ⚙️ السيرفرات (<?= count($m['servers']) ?>)
                    </button>

                    <!-- Edit match info -->
                    <button class="btn btn-outline" onclick="openEditMatchModal('<?= $mId ?>', <?= htmlspecialchars(json_encode($m)) ?>)">
                        ✏️ تعديل
                    </button>

                    <?php if (!empty($m['servers'])): ?>
                    <button class="btn btn-outline" onclick="playInTestPlayer('<?= addslashes($m['servers'][0]['url']) ?>', '<?= addslashes($m['home_team']['title'] . ' ضد ' . $m['away_team']['title']) ?>')">
                        ▶️ تجربة
                    </button>
                    <?php endif; ?>
                </div>
            </div>
            <?php endforeach; ?>
        </div>
    </div>

    <!-- 2. TAB: LIVE CHANNELS -->
    <div id="tab-channels" class="tab-content">
        <div class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">إدارة القنوات التلفزيونية المباشرة</h2>
                <p style="font-size: 12.5px; color: var(--text-muted);">إضافة وتعديل قنوات beIN SPORTS والكأس والأندية العالمية وتصديرها للتطبيق</p>
            </div>
            <button class="btn" onclick="openAddChannelModal()">➕ إضافة قناة جديدة</button>
        </div>

        <div class="card">
            <table class="styled-table">
                <thead>
                    <tr>
                        <th>اللوغو</th>
                        <th>اسم القناة</th>
                        <th>القسم / التصنيف</th>
                        <th>الجودة</th>
                        <th>رابط البث</th>
                        <th>إجراءات</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($currentChannels as $ch): ?>
                    <tr>
                        <td><img src="<?= htmlspecialchars($ch['iconUrl']) ?>" style="width: 32px; height: 32px; object-fit: contain;" onerror="this.src='https://cdn-icons-png.flaticon.com/512/3845/3845868.png'"></td>
                        <td><strong><?= htmlspecialchars($ch['name']) ?></strong></td>
                        <td><?= htmlspecialchars($ch['categoryId']) ?></td>
                        <td><span class="stream-tag"><?= htmlspecialchars($ch['quality'] ?? 'HD') ?></span></td>
                        <td style="direction: ltr; font-family: monospace; font-size: 11px; max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"><?= htmlspecialchars($ch['playUrl']) ?></td>
                        <td>
                            <button class="btn btn-outline" style="padding: 4px 10px; font-size: 12px;" onclick="playInTestPlayer('<?= addslashes($ch['playUrl']) ?>', '<?= addslashes($ch['name']) ?>')">▶️ تشغيل</button>
                            <button class="btn btn-red" style="padding: 4px 10px; font-size: 12px;" onclick="deleteChannel('<?= $ch['streamId'] ?>')">🗑️ حذف</button>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>

    <!-- 3. TAB: POSTERS & SLIDER -->
    <div id="tab-slider" class="tab-content">
        <div class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">إدارة البوسترات والسلايدر العلوي</h2>
                <p style="font-size: 12.5px; color: var(--text-muted);">تحكم في البانرات والبوسترات الترويجية الكبرى التي تظهر في الصفحة الرئيسية للتطبيق</p>
            </div>
            <button class="btn" onclick="openAddSliderModal()">➕ إضافة بوستر جديد</button>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px;">
            <?php foreach ($currentSlider as $b): ?>
            <div class="card" style="padding: 0; overflow: hidden;">
                <img src="<?= htmlspecialchars($b['image']) ?>" style="width: 100%; height: 160px; object-fit: crop;" onerror="this.src='https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800'">
                <div style="padding: 14px;">
                    <h3 style="font-size: 14px; font-weight: 800; color: #fff; margin-bottom: 6px;"><?= htmlspecialchars($b['title']) ?></h3>
                    <div style="display: flex; justify-content: flex-end; margin-top: 10px;">
                        <button class="btn btn-red" onclick="deleteSlider('<?= $b['id'] ?>')">🗑️ حذف البوستر</button>
                    </div>
                </div>
            </div>
            <?php endforeach; ?>
        </div>
    </div>

    <!-- 4. TAB: NOTIFICATIONS & PUSH ANNOUNCEMENTS -->
    <div id="tab-notifications" class="tab-content">
        <div class="card">
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900; margin-bottom: 6px;">إرسال إشعار وتنبيه فوري لجميع المستخدمين 🔔</h2>
            <p style="font-size: 12.5px; color: var(--text-muted); margin-bottom: 20px;">اكتب التنبيه الذي تريده وسيظهر لجميع مستخدمي التطبيق فوراً، مع إمكانية ربطه بمباراة معينة لفتحها مباشرة عند الضغط على الإشعار</p>

            <form id="announceForm" onsubmit="saveAnnouncement(event)">
                <div class="form-group">
                    <label>حالة الإشعار في التطبيق</label>
                    <select id="notif_enabled" class="form-control">
                        <option value="1" <?= (!empty($currentAnnounce['enabled'])) ? 'selected' : '' ?>>🟢 مفعل ويظهر للجميع</option>
                        <option value="0" <?= (empty($currentAnnounce['enabled'])) ? 'selected' : '' ?>>🔴 معطل ومخفي</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>عنوان الإشعار (مثال: قمة الليلة المرتقبة 🔥)</label>
                    <input type="text" id="notif_title" class="form-control" value="<?= htmlspecialchars($currentAnnounce['title'] ?? 'إشعار هام') ?>" required>
                </div>

                <div class="form-group">
                    <label>نص الرسالة</label>
                    <textarea id="notif_message" class="form-control" rows="3" placeholder="تم تشغيل 4 سيرفرات FHD لمباراة ريال مدريد ومانشستر سيتي، اضغط هنا للمشاهدة المباشرة!" required><?= htmlspecialchars($currentAnnounce['message'] ?? '') ?></textarea>
                </div>

                <div class="form-group">
                    <label>توجيه الضغطة (عندما يضغط المستخدم على الإشعار)</label>
                    <select id="notif_target_type" class="form-control" onchange="onTargetTypeChange()">
                        <option value="none" <?= ($currentAnnounce['target_type'] ?? '') === 'none' ? 'selected' : '' ?>>بدون توجيه (مجرد تنبيه)</option>
                        <option value="match" <?= ($currentAnnounce['target_type'] ?? '') === 'match' ? 'selected' : '' ?>>فتح مباراة معينة</option>
                        <option value="channel" <?= ($currentAnnounce['target_type'] ?? '') === 'channel' ? 'selected' : '' ?>>فتح قناة معينة</option>
                        <option value="news" <?= ($currentAnnounce['target_type'] ?? '') === 'news' ? 'selected' : '' ?>>فتح قسم الأخبار</option>
                    </select>
                </div>

                <div class="form-group" id="target_id_group">
                    <label>اختر المباراة أو القناة المستهدفة</label>
                    <select id="notif_target_id" class="form-control">
                        <option value="">-- اختر من القائمة --</option>
                        <optgroup label="المباريات المتاحة">
                            <?php foreach ($enrichedMatches as $m): ?>
                            <option value="ys_<?= $m['match_id'] ?>" <?= ($currentAnnounce['target_id'] ?? '') === ('ys_' . $m['match_id']) ? 'selected' : '' ?>>
                                <?= htmlspecialchars($m['home_team']['title'] . ' ضد ' . $m['away_team']['title']) ?>
                            </option>
                            <?php endforeach; ?>
                        </optgroup>
                    </select>
                </div>

                <div style="display: flex; gap: 10px; margin-top: 20px;">
                    <button type="submit" class="btn">🚀 نشر وتحديث الإشعار الآن</button>
                    <button type="button" class="btn btn-red" onclick="clearAnnouncement()">🗑️ مسح الإشعار</button>
                </div>
            </form>
        </div>
    </div>

    <!-- 5. TAB: VIDEO PLAYER -->
    <div id="tab-player" class="tab-content">
        <div class="card">
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900; margin-bottom: 12px;">مشغل الفيديو المباشر (HLS & m3u8 Test Player)</h2>
            <div style="display: flex; gap: 10px; margin-bottom: 16px;">
                <input type="text" id="manual_stream_url" class="form-control" placeholder="أدخل رابط البث https://.../stream.m3u8" style="direction: ltr;">
                <button class="btn" onclick="playManualUrl()">تشغيل البث</button>
            </div>
            <div style="width: 100%; max-width: 900px; margin: 0 auto; background: #000; border-radius: 16px; overflow: hidden; aspect-ratio: 16/9; position: relative;">
                <video id="liveVideo" controls style="width: 100%; height: 100%;"></video>
            </div>
            <p id="player_info" style="margin-top: 10px; font-size: 13px; color: var(--gold); text-align: center;"></p>
        </div>
    </div>

</div>

<!-- Modals -->
<!-- Server Modal -->
<div class="modal" id="serverModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 12px;" id="serverModalTitle">إدارة سيرفرات البث</h3>
        <div id="existingServersList" style="margin-bottom: 16px;"></div>

        <form onsubmit="saveServerForm(event)">
            <input type="hidden" id="srv_match_id">
            <div class="form-group">
                <label>اسم السيرفر</label>
                <input type="text" id="srv_name" class="form-control" value="سيرفر 1 (FHD)" required>
            </div>
            <div class="form-group">
                <label>رابط البث المباشر (m3u8 أو mp4)</label>
                <input type="url" id="srv_url" class="form-control" placeholder="https://domain.com/live/stream.m3u8" style="direction: ltr;" required>
            </div>
            <div class="form-group">
                <label>الجودة</label>
                <select id="srv_quality" class="form-control">
                    <option value="4K UHD">4K UHD</option>
                    <option value="1080p FHD" selected>1080p FHD</option>
                    <option value="720p HD">720p HD</option>
                    <option value="SD Low">SD Low (ضعيف)</option>
                </select>
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 إضافة وحفظ السيرفر</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('serverModal')">إغلاق</button>
            </div>
        </form>
    </div>
</div>

<!-- Edit Match Modal -->
<div class="modal" id="editMatchModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 16px;">تعديل بيانات المباراة</h3>
        <form onsubmit="saveEditMatchForm(event)">
            <input type="hidden" id="edit_match_id">
            <div style="display: flex; gap: 10px;">
                <div class="form-group" style="flex: 1;">
                    <label>أهداف الفريق 1</label>
                    <input type="number" id="edit_home_scores" class="form-control" min="0">
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>أهداف الفريق 2</label>
                    <input type="number" id="edit_away_scores" class="form-control" min="0">
                </div>
            </div>
            <div class="form-group">
                <label>الدقيقة أو التوقيت (مثال: '45 أو الشوط الأول)</label>
                <input type="text" id="edit_score_time" class="form-control">
            </div>
            <div class="form-group">
                <label>حالة المباراة</label>
                <select id="edit_status" class="form-control">
                    <option value="1">لم تبدأ</option>
                    <option value="2">مباشر - الشوط الأول</option>
                    <option value="3">مباشر - الشوط الثاني</option>
                    <option value="4">انتهت المباراة</option>
                </select>
            </div>
            <div class="form-group">
                <label>القناة الناقلة</label>
                <input type="text" id="edit_channel_name" class="form-control">
            </div>
            <div class="form-group">
                <label>المعلق الرياضي</label>
                <input type="text" id="edit_commentator" class="form-control">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ التعديلات</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('editMatchModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- Add Channel Modal -->
<div class="modal" id="addChannelModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 16px;">إضافة قناة تلفزيونية مباشرة جديدة 📺</h3>
        <form onsubmit="saveChannelForm(event)">
            <div class="form-group">
                <label>اسم القناة (مثال: beIN SPORTS 1 HD)</label>
                <input type="text" id="ch_name" class="form-control" placeholder="beIN SPORTS 1 HD" required>
            </div>
            <div class="form-group">
                <label>تصنيف / قسم القناة</label>
                <select id="ch_category" class="form-control">
                    <option value="قنوات beIN SPORTS">قنوات beIN SPORTS</option>
                    <option value="قنوات الكأس">قنوات الكأس</option>
                    <option value="قنوات SSC السعودية">قنوات SSC السعودية</option>
                    <option value="قنوات أبوظبي الرياضية">قنوات أبوظبي الرياضية</option>
                    <option value="قنوات الأندية العالمية">قنوات الأندية العالمية</option>
                    <option value="قنوات الأخبار والرياضة العامة">قنوات الأخبار والرياضة العامة</option>
                </select>
            </div>
            <div class="form-group">
                <label>رابط لوغو القناة (أو اختر من النماذج)</label>
                <input type="url" id="ch_icon" class="form-control" placeholder="https://domain.com/logo.png" style="direction: ltr;" value="https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/BeIN_Sports_1_logo.png/512px-BeIN_Sports_1_logo.png">
                <div style="display: flex; gap: 6px; margin-top: 6px; flex-wrap: wrap;">
                    <button type="button" class="btn btn-outline" style="padding: 2px 8px; font-size: 11px;" onclick="document.getElementById('ch_icon').value='https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/BeIN_Sports_1_logo.png/512px-BeIN_Sports_1_logo.png'">beIN 1</button>
                    <button type="button" class="btn btn-outline" style="padding: 2px 8px; font-size: 11px;" onclick="document.getElementById('ch_icon').value='https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/BeIN_Sports_2_logo.png/512px-BeIN_Sports_2_logo.png'">beIN 2</button>
                    <button type="button" class="btn btn-outline" style="padding: 2px 8px; font-size: 11px;" onclick="document.getElementById('ch_icon').value='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e6/Al_Kass_logo.png/512px-Al_Kass_logo.png'">الكأس</button>
                </div>
            </div>
            <div class="form-group">
                <label>رابط البث المباشر (HLS m3u8 أو MP4)</label>
                <input type="url" id="ch_play_url" class="form-control" placeholder="https://domain.com/live/stream.m3u8" style="direction: ltr;" required>
            </div>
            <div class="form-group">
                <label>الجودة</label>
                <select id="ch_quality" class="form-control">
                    <option value="4K UHD">4K UHD</option>
                    <option value="1080p FHD" selected>1080p FHD</option>
                    <option value="720p HD">720p HD</option>
                </select>
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ وإضافة القناة للتطبيق</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('addChannelModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- Add Slider / Poster Modal -->
<div class="modal" id="addSliderModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 16px;">إضافة بوستر / بانر ترويجي جديد 🖼️</h3>
        <form onsubmit="saveSliderForm(event)">
            <div class="form-group">
                <label>عنوان البوستر (مثال: كلاسيكو الأرض المرتقب 🔥)</label>
                <input type="text" id="slide_title" class="form-control" placeholder="كلاسيكو الأرض المرتقب" required>
            </div>
            <div class="form-group">
                <label>رابط صورة البوستر عريضة (أو اختر من النماذج الرياضية)</label>
                <input type="url" id="slide_image" class="form-control" placeholder="https://domain.com/banner.jpg" style="direction: ltr;" required>
                <div style="display: flex; gap: 6px; margin-top: 6px; flex-wrap: wrap;">
                    <button type="button" class="btn btn-outline" style="padding: 2px 8px; font-size: 11px;" onclick="document.getElementById('slide_image').value='https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=1200&auto=format&fit=crop'">ملعب 1</button>
                    <button type="button" class="btn btn-outline" style="padding: 2px 8px; font-size: 11px;" onclick="document.getElementById('slide_image').value='https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=1200&auto=format&fit=crop'">ملعب 2</button>
                    <button type="button" class="btn btn-outline" style="padding: 2px 8px; font-size: 11px;" onclick="document.getElementById('slide_image').value='https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=1200&auto=format&fit=crop'">كلاسيكو</button>
                </div>
            </div>
            <div class="form-group">
                <label>رابط الإجراء عند الضغط (اختياري)</label>
                <input type="text" id="slide_link" class="form-control" placeholder="match:ys_123 أو رابط خارجي">
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 حفظ ونشر البوستر</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('addSliderModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<!-- Add Custom Match Modal -->
<div class="modal" id="customMatchModal">
    <div class="modal-content">
        <h3 style="color: var(--gold); margin-bottom: 16px;">إضافة مباراة مخصصة جديدة ⚽</h3>
        <form onsubmit="saveCustomMatchForm(event)">
            <div class="form-group">
                <label>اسم البطولة</label>
                <input type="text" id="cm_championship" class="form-control" value="دوري أبطال أوروبا" required>
            </div>
            <div style="display: flex; gap: 10px;">
                <div class="form-group" style="flex: 1;">
                    <label>الفريق الأول (المستضيف)</label>
                    <input type="text" id="cm_home_team" class="form-control" placeholder="ريال مدريد" required>
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>الفريق الثاني (الضيف)</label>
                    <input type="text" id="cm_away_team" class="form-control" placeholder="مانشستر سيتي" required>
                </div>
            </div>
            <div style="display: flex; gap: 10px;">
                <div class="form-group" style="flex: 1;">
                    <label>وقت المباراة</label>
                    <input type="time" id="cm_match_time" class="form-control" value="22:00" required>
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>تاريخ المباراة</label>
                    <input type="date" id="cm_match_date" class="form-control" value="<?= date('Y-m-d') ?>" required>
                </div>
            </div>
            <div class="form-group">
                <label>رابط سيرفر البث المباشر (HLS m3u8)</label>
                <input type="url" id="cm_stream_url" class="form-control" placeholder="https://domain.com/live/match.m3u8" style="direction: ltr;">
            </div>
            <div style="display: flex; gap: 10px;">
                <div class="form-group" style="flex: 1;">
                    <label>القناة الناقلة</label>
                    <input type="text" id="cm_channel" class="form-control" value="beIN SPORTS 1 HD">
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>المعلق الرياضي</label>
                    <input type="text" id="cm_commentator" class="form-control" value="عصام الشوالي">
                </div>
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 20px;">
                <button type="submit" class="btn">💾 إضافة المباراة ونشرها للتطبيق</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('customMatchModal')">إلغاء</button>
            </div>
        </form>
    </div>
</div>

<script>
let hlsInstance = null;

function switchMainTab(tabKey) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.main-tab-btn').forEach(el => el.classList.remove('active'));
    document.getElementById('tab-' + tabKey).classList.add('active');
    event.target.classList.add('active');
}

function openModal(id) { document.getElementById(id).classList.add('show'); }
function closeModal(id) { document.getElementById(id).classList.remove('show'); }

async function toggleMatchStream(matchId, enable) {
    const res = await fetch('', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'toggle_stream', match_id: matchId, stream_active: enable })
    });
    const data = await res.json();
    if (data.status) {
        location.reload();
    }
}

function openServersModal(matchId, title, servers) {
    document.getElementById('srv_match_id').value = matchId;
    document.getElementById('serverModalTitle').innerText = 'سيرفرات: ' + title;
    
    let html = '';
    if (servers && servers.length > 0) {
        html += '<h4 style="font-size: 13px; color: #fff; margin-bottom: 8px;">السيرفرات الحالية:</h4>';
        servers.forEach(s => {
            html += `<div style="display: flex; justify-content: space-between; align-items: center; background: #0c0f18; padding: 8px 12px; border-radius: 8px; margin-bottom: 6px;">
                <div><strong>${s.name}</strong> <span class="stream-tag">${s.quality || 'HD'}</span></div>
                <button class="btn btn-red" style="padding: 2px 8px; font-size: 11px;" onclick="deleteServer('${matchId}', '${s.id}')">حذف</button>
            </div>`;
        });
    } else {
        html = '<p style="color: var(--text-muted); font-size: 12px; margin-bottom: 12px;">لا توجد سيرفرات مضافة بعد لهذه المباراة</p>';
    }
    document.getElementById('existingServersList').innerHTML = html;
    openModal('serverModal');
}

async function saveServerForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_server',
        match_id: document.getElementById('srv_match_id').value,
        server_name: document.getElementById('srv_name').value,
        stream_url: document.getElementById('srv_url').value,
        quality: document.getElementById('srv_quality').value
    };
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    if (data.status) {
        alert(data.message);
        location.reload();
    } else {
        alert(data.message || 'حدث خطأ');
    }
}

async function deleteServer(matchId, srvId) {
    if (!confirm('هل أنت متأكد من حذف هذا السيرفر؟')) return;
    const res = await fetch('', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ action: 'delete_server', match_id: matchId, server_id: srvId })
    });
    const data = await res.json();
    if (data.status) location.reload();
}

function openEditMatchModal(matchId, matchData) {
    document.getElementById('edit_match_id').value = matchId;
    document.getElementById('edit_home_scores').value = matchData.home_scores ?? '';
    document.getElementById('edit_away_scores').value = matchData.away_scores ?? '';
    document.getElementById('edit_score_time').value = matchData.score_time ?? '';
    document.getElementById('edit_status').value = matchData.status ?? 1;
    document.getElementById('edit_channel_name').value = matchData.channel_name ?? '';
    document.getElementById('edit_commentator').value = matchData.commentator ?? '';
    openModal('editMatchModal');
}

async function saveEditMatchForm(e) {
    e.preventDefault();
    const payload = {
        action: 'edit_match',
        match_id: document.getElementById('edit_match_id').value,
        home_scores: document.getElementById('edit_home_scores').value,
        away_scores: document.getElementById('edit_away_scores').value,
        score_time: document.getElementById('edit_score_time').value,
        status: document.getElementById('edit_status').value,
        channel_name: document.getElementById('edit_channel_name').value,
        commentator: document.getElementById('edit_commentator').value
    };
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    if (data.status) location.reload();
}

async function saveAnnouncement(e) {
    e.preventDefault();
    const payload = {
        action: 'save_announcement',
        enabled: document.getElementById('notif_enabled').value === '1',
        title: document.getElementById('notif_title').value,
        message: document.getElementById('notif_message').value,
        target_type: document.getElementById('notif_target_type').value,
        target_id: document.getElementById('notif_target_id').value
    };
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
}

async function clearAnnouncement() {
    if (!confirm('هل تريد تعطيل ومسح الإشعار؟')) return;
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'clear_announcement' }) });
    const data = await res.json();
    alert(data.message);
    location.reload();
}

async function deleteChannel(streamId) {
    if (!confirm('حذف هذه القناة؟')) return;
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_channel', streamId: streamId }) });
    location.reload();
}

async function deleteSlider(sliderId) {
    if (!confirm('حذف هذا البوستر؟')) return;
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ action: 'delete_slider', id: sliderId }) });
    location.reload();
}

function openAddChannelModal() {
    openModal('addChannelModal');
}

async function saveChannelForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_channel',
        name: document.getElementById('ch_name').value,
        categoryId: document.getElementById('ch_category').value,
        iconUrl: document.getElementById('ch_icon').value,
        playUrl: document.getElementById('ch_play_url').value,
        quality: document.getElementById('ch_quality').value
    };
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

function openAddSliderModal() {
    openModal('addSliderModal');
}

async function saveSliderForm(e) {
    e.preventDefault();
    const payload = {
        action: 'save_slider',
        title: document.getElementById('slide_title').value,
        image: document.getElementById('slide_image').value,
        link: document.getElementById('slide_link').value
    };
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

function openCustomMatchModal() {
    openModal('customMatchModal');
}

async function saveCustomMatchForm(e) {
    e.preventDefault();
    const payload = {
        action: 'add_custom_match',
        championship: document.getElementById('cm_championship').value,
        home_team: document.getElementById('cm_home_team').value,
        away_team: document.getElementById('cm_away_team').value,
        match_time: document.getElementById('cm_match_time').value,
        match_date: document.getElementById('cm_match_date').value,
        stream_url: document.getElementById('cm_stream_url').value,
        channel_name: document.getElementById('cm_channel').value,
        commentator: document.getElementById('cm_commentator').value
    };
    const res = await fetch('', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload) });
    const data = await res.json();
    alert(data.message);
    if (data.status) location.reload();
}

function onTargetTypeChange() {
    const val = document.getElementById('notif_target_type').value;
    const grp = document.getElementById('target_id_group');
    if (val === 'none' || val === 'news') {
        grp.style.display = 'none';
    } else {
        grp.style.display = 'block';
    }
}

function playInTestPlayer(url, title) {
    switchMainTab('player');
    document.getElementById('manual_stream_url').value = url;
    document.getElementById('player_info').innerText = 'تشغيل: ' + title;
    playManualUrl();
}

function playManualUrl() {
    const video = document.getElementById('liveVideo');
    const url = document.getElementById('manual_stream_url').value.trim();
    if (!url) return;

    if (hlsInstance) {
        hlsInstance.destroy();
        hlsInstance = null;
    }

    if (Hls.isSupported() && url.includes('.m3u8')) {
        hlsInstance = new Hls();
        hlsInstance.loadSource(url);
        hlsInstance.attachMedia(video);
        hlsInstance.on(Hls.Events.MANIFEST_PARSED, () => video.play());
    } else {
        video.src = url;
        video.play();
    }
}
</script>

</body>
</html>
