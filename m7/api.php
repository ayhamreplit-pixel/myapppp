<?php
/**
 * M7 Streaming Server - Central REST API Router
 * Handles all JSON requests, authentication, and AES-256 encrypted stream delivery.
 */

define('M7_ROOT', __DIR__);
require_once __DIR__ . '/config/config.php';

Security::setCorsHeaders();

// Detect target date
$todayDate = date('Y-m-d');
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
// 1. POST ACTIONS CONTROLLER
// -------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    header('Content-Type: application/json; charset=utf-8');
    $input = json_decode(file_get_contents('php://input'), true);
    if (!$input) {
        $input = $_POST;
    }
    $postAction = isset($input['action']) ? $input['action'] : $action;

    // 1. Toggle Live Stream
    if ($postAction === 'toggle_stream') {
        $matchId = trim($input['match_id'] ?? '');
        $state = isset($input['stream_active']) ? (bool)$input['stream_active'] : null;
        echo json_encode(MatchesService::toggleStream($matchId, $state));
        exit;
    }

    // 2. Save Server
    if ($postAction === 'save_server') {
        $matchId = trim($input['match_id'] ?? '');
        $serverName = trim($input['server_name'] ?? 'سيرفر 1');
        $serverUrl = trim($input['stream_url'] ?? '');
        $quality = trim($input['quality'] ?? '1080p FHD');
        $serverId = trim($input['server_id'] ?? '');

        if (empty($matchId) || empty($serverUrl)) {
            echo json_encode(["status" => false, "message" => "يرجى إدخال رابط البث"]);
            exit;
        }

        $overrides = Storage::get('match_overrides.json', []);
        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false, "stream_active" => true];
        }
        $overrides[$matchId]['stream_active'] = true;

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
            $overrides[$matchId]['servers'][] = [
                'id' => 'srv_' . time() . '_' . rand(100, 999),
                'name' => $serverName,
                'url' => $serverUrl,
                'quality' => $quality
            ];
        }

        Storage::save('match_overrides.json', $overrides);
        echo json_encode(["status" => true, "message" => "تم حفظ وتفعيل سيرفر البث بنجاح", "servers" => $overrides[$matchId]['servers']]);
        exit;
    }

    // 3. Delete Server
    if ($postAction === 'delete_server') {
        $matchId = trim($input['match_id'] ?? '');
        $serverId = trim($input['server_id'] ?? '');
        $overrides = Storage::get('match_overrides.json', []);
        if (isset($overrides[$matchId]['servers'])) {
            $overrides[$matchId]['servers'] = array_values(array_filter($overrides[$matchId]['servers'], function($s) use ($serverId) {
                return $s['id'] !== $serverId;
            }));
            if (empty($overrides[$matchId]['servers'])) {
                $overrides[$matchId]['stream_active'] = false;
            }
            Storage::save('match_overrides.json', $overrides);
        }
        echo json_encode(["status" => true, "message" => "تم حذف السيرفر"]);
        exit;
    }

    // 4. Save / Edit Match (Manual Match Manager)
    if ($postAction === 'save_match' || $postAction === 'add_custom_match' || $postAction === 'edit_match') {
        echo json_encode(MatchesService::saveMatch($input));
        exit;
    }

    // 4.1 Delete Match
    if ($postAction === 'delete_match') {
        $mId = trim($input['match_id'] ?? '');
        echo json_encode(MatchesService::deleteMatch($mId));
        exit;
    }

    // 4.2 Quick Update Score & Minute
    if ($postAction === 'quick_score') {
        $mId = trim($input['match_id'] ?? '');
        $hScore = $input['home_scores'] ?? null;
        $aScore = $input['away_scores'] ?? null;
        $status = $input['status'] ?? null;
        $time = $input['score_time'] ?? null;
        echo json_encode(MatchesService::quickUpdateScore($mId, $hScore, $aScore, $status, $time));
        exit;
    }

    // 4.3 Clone Match
    if ($postAction === 'clone_match') {
        $mId = trim($input['match_id'] ?? '');
        echo json_encode(MatchesService::cloneMatch($mId));
        exit;
    }

    // 4.4 Backup Export & Restore
    if ($postAction === 'backup_export') {
        $backup = [
            'version' => M7_VERSION,
            'timestamp' => time(),
            'custom_matches' => Storage::get('custom_matches.json', []),
            'channels' => Storage::get('channels.json', []),
            'hero_banners' => Storage::get('hero_banners.json', []),
            'featured_weekly' => Storage::get('featured_weekly.json', []),
            'slider' => Storage::get('slider.json', []),
            'announcement' => Storage::get('announcement.json', []),
            'settings' => Storage::get('settings.json', [])
        ];
        echo json_encode(["status" => true, "backup" => $backup]);
        exit;
    }

    if ($postAction === 'backup_restore') {
        $data = $input['backup_data'] ?? [];
        if (is_string($data)) $data = json_decode($data, true);
        if (!empty($data) && is_array($data)) {
            if (isset($data['custom_matches'])) Storage::save('custom_matches.json', $data['custom_matches']);
            if (isset($data['channels'])) Storage::save('channels.json', $data['channels']);
            if (isset($data['hero_banners'])) Storage::save('hero_banners.json', $data['hero_banners']);
            if (isset($data['featured_weekly'])) Storage::save('featured_weekly.json', $data['featured_weekly']);
            if (isset($data['slider'])) Storage::save('slider.json', $data['slider']);
            if (isset($data['announcement'])) Storage::save('announcement.json', $data['announcement']);
            if (isset($data['settings'])) Storage::save('settings.json', $data['settings']);
            echo json_encode(["status" => true, "message" => "تمت استعادة كافة البيانات والنسخة الاحتياطية بنجاح 🔄"]);
            exit;
        }
        echo json_encode(["status" => false, "message" => "ملف النسخة الاحتياطية غير صالح"]);
        exit;
    }

    // 7. Channels Actions
    if ($postAction === 'save_channel') {
        echo json_encode(ChannelsService::save($input));
        exit;
    }
    if ($postAction === 'delete_channel') {
        echo json_encode(ChannelsService::delete($input['streamId'] ?? ''));
        exit;
    }

    // 8. Hero & Featured Actions
    if ($postAction === 'save_hero_banner') {
        echo json_encode(HeroService::saveHeroBanner($input));
        exit;
    }
    if ($postAction === 'delete_hero_banner') {
        echo json_encode(HeroService::deleteHeroBanner($input['id'] ?? ''));
        exit;
    }
    if ($postAction === 'save_featured_weekly') {
        echo json_encode(HeroService::saveFeaturedWeekly($input));
        exit;
    }
    if ($postAction === 'delete_featured_weekly') {
        echo json_encode(HeroService::deleteFeaturedWeekly($input['id'] ?? ''));
        exit;
    }

    // 9. Slider Actions
    if ($postAction === 'save_slider') {
        $slider = Storage::get('slider.json', []);
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
        Storage::save('slider.json', $slider);
        echo json_encode(["status" => true, "message" => "تم حفظ البوستر بنجاح"]);
        exit;
    }
    if ($postAction === 'delete_slider') {
        $bId = trim($input['id'] ?? '');
        $slider = Storage::get('slider.json', []);
        $slider = array_values(array_filter($slider, function($b) use ($bId) { return $b['id'] !== $bId; }));
        Storage::save('slider.json', $slider);
        echo json_encode(["status" => true, "message" => "تم حذف البوستر"]);
        exit;
    }

    // 10. Announcements
    if ($postAction === 'save_announcement') {
        echo json_encode(AnnouncementService::save($input));
        exit;
    }
    if ($postAction === 'clear_announcement') {
        echo json_encode(AnnouncementService::clear());
        exit;
    }

    // 11. Security Settings & Sandbox
    if ($postAction === 'save_settings') {
        $settings = Storage::get('settings.json', []);
        if (isset($input['encrypt_streams'])) $settings['encrypt_streams'] = (bool)$input['encrypt_streams'];
        if (!empty($input['master_key'])) $settings['master_key'] = trim($input['master_key']);
        Storage::save('settings.json', $settings);
        echo json_encode(["status" => true, "message" => "تم تحديث إعدادات الأمان والتشفير بنجاح", "settings" => $settings]);
        exit;
    }
    if ($postAction === 'test_encrypt') {
        $key = !empty($input['secret_key']) ? trim($input['secret_key']) : Security::getMasterKey();
        $rawUrl = trim($input['url'] ?? '');
        $encrypted = Security::encrypt($rawUrl, $key);
        echo json_encode(["status" => true, "raw_url" => $rawUrl, "encrypted" => $encrypted, "key_used" => $key]);
        exit;
    }
    // 12. Image & File Upload
    if ($postAction === 'upload' || $action === 'upload') {
        echo json_encode(UploadService::handleUpload('image'));
        exit;
    }

    // 13. Device Authentication & Handshake
    if ($postAction === 'device_auth' || $postAction === 'handshake') {
        echo json_encode(AuthService::authenticateDevice($input));
        exit;
    }

    // 14. Session Heartbeat
    if ($postAction === 'heartbeat') {
        $dId = trim($input['deviceId'] ?? $input['device_id'] ?? '');
        $dName = trim($input['deviceName'] ?? $input['device_name'] ?? '');
        $pName = trim($input['profileName'] ?? $input['profile_name'] ?? '');
        $stream = trim($input['activeStreamTitle'] ?? $input['active_stream'] ?? '');
        $ok = AuthService::updateHeartbeat($dId, $dName, $pName, $stream);
        echo json_encode(["status" => $ok, "timestamp" => time()]);
        exit;
    }

    // 15. Device Access & Ban Management
    if ($postAction === 'toggle_device_ban') {
        $dId = trim($input['device_id'] ?? '');
        $state = isset($input['ban']) ? (bool)$input['ban'] : true;
        echo json_encode(AuthService::toggleDeviceBan($dId, $state));
        exit;
    }
    if ($postAction === 'approve_device') {
        $dId = trim($input['device_id'] ?? '');
        echo json_encode(AuthService::approveDevice($dId));
        exit;
    }
    if ($postAction === 'delete_device') {
        $dId = trim($input['device_id'] ?? '');
        echo json_encode(AuthService::deleteDevice($dId));
        exit;
    }
    if ($postAction === 'save_access_policy') {
        $settings = Storage::get('settings.json', []);
        $settings['access_policy'] = trim($input['access_policy'] ?? 'open');
        Storage::save('settings.json', $settings);
        echo json_encode(["status" => true, "message" => "تم حفظ سياسة الدخول"]);
        exit;
    }
}

// -------------------------------------------------------------------------
// 2. GET API CONTROLLER
// -------------------------------------------------------------------------

if ($action === 'upload' && $_SERVER['REQUEST_METHOD'] === 'POST') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(UploadService::handleUpload('image'));
    exit;
}

if ($action === 'device_auth' || $action === 'handshake') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(AuthService::authenticateDevice($_GET));
    exit;
}

if ($action === 'heartbeat') {
    header('Content-Type: application/json; charset=utf-8');
    $dId = trim($_GET['deviceId'] ?? $_GET['device_id'] ?? '');
    $dName = trim($_GET['deviceName'] ?? $_GET['device_name'] ?? '');
    $pName = trim($_GET['profileName'] ?? $_GET['profile_name'] ?? '');
    $stream = trim($_GET['activeStreamTitle'] ?? $_GET['active_stream'] ?? '');
    $ok = AuthService::updateHeartbeat($dId, $dName, $pName, $stream);
    echo json_encode(["status" => $ok, "timestamp" => time()]);
    exit;
}

if ($action === 'sessions' || $action === 'live_viewers') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "count" => count(AuthService::getActiveSessions()), "sessions" => AuthService::getActiveSessions()]);
    exit;
}

if ($action === 'channels') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "channels" => ChannelsService::getAll(true)], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'hero_banners' || $action === 'hero') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "hero_banners" => HeroService::getHeroBanners(true)], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'featured_weekly' || $action === 'featured') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "featured_weekly" => HeroService::getFeaturedWeekly(true)], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'slider') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "slider" => Storage::get('slider.json', [])], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'announcement') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(["status" => true, "announcement" => AnnouncementService::get()], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'logos' || $action === 'get_logos') {
    header('Content-Type: application/json; charset=utf-8');
    $q = trim($_GET['q'] ?? $_GET['query'] ?? '');
    $cat = trim($_GET['category'] ?? $_GET['cat'] ?? 'all');
    echo json_encode(["status" => true, "logos" => LogosLibrary::search($q, $cat)], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'match_details' || $action === 'details') {
    header('Content-Type: application/json; charset=utf-8');
    $eventId = trim($_GET['event_id'] ?? $_GET['id'] ?? '');
    echo json_encode(FootballApiService::getMatchDetails($eventId), JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'search_api_matches' || $action === 'api_matches') {
    header('Content-Type: application/json; charset=utf-8');
    $query = trim($_GET['q'] ?? $_GET['query'] ?? '');
    $date = trim($_GET['date'] ?? $targetDate);
    echo json_encode(FootballApiService::searchMatches($query, $date), JSON_UNESCAPED_UNICODE);
    exit;
}

if ($action === 'news') {
    header('Content-Type: application/json; charset=utf-8');
    $newsCache = M7_CACHE_DIR . "/sports_news.json";
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
    curl_close($ch);
    if (!empty($newsResp)) {
        @file_put_contents($newsCache, $newsResp);
        echo $newsResp;
        exit;
    }
    echo json_encode(["status" => true, "news" => []]);
    exit;
}

if ($action === 'clear_cache') {
    $cacheFile = M7_CACHE_DIR . "/matches_" . preg_replace('/[^0-9\-]/', '', $targetDate) . ".json";
    if (file_exists($cacheFile)) @unlink($cacheFile);
    $forceRefresh = true;
}

// Check if request is JSON API
$isJsonRequest = (isset($_SERVER['HTTP_ACCEPT']) && strpos($_SERVER['HTTP_ACCEPT'], 'application/json') !== false) ||
                 (isset($_SERVER['HTTP_USER_AGENT']) && (strpos($_SERVER['HTTP_USER_AGENT'], 'TOD') !== false || strpos($_SERVER['HTTP_USER_AGENT'], 'okhttp') !== false)) ||
                 (isset($_GET['format']) && $_GET['format'] === 'json') ||
                 ($action === 'matches');

if ($isJsonRequest) {
    header('Content-Type: application/json; charset=utf-8');
    $matches = MatchesService::getEnrichedMatches($targetDate, $forceRefresh, true);
    $responseArray = [
        "status" => true,
        "encryption" => [
            "enabled" => Security::isEncryptionEnabled(),
            "algorithm" => "AES-256-CBC",
            "key_alias" => "DEFAULT_MASTER_KEY"
        ],
        "date" => $targetDate,
        "count" => count($matches),
        "data" => $matches,
        "hero_banners" => HeroService::getHeroBanners(true),
        "featured_weekly" => HeroService::getFeaturedWeekly(true),
        "slider" => Storage::get('slider.json', []),
        "channels" => ChannelsService::getAll(true),
        "announcement" => AnnouncementService::get()
    ];

    if (isset($_GET['encrypt']) && ($_GET['encrypt'] == '1' || $_GET['encrypt'] == 'true')) {
        $rawJson = json_encode($responseArray, JSON_UNESCAPED_UNICODE);
        $encryptedPayload = Security::encrypt($rawJson);
        echo json_encode([
            "status" => true,
            "encrypted" => true,
            "encrypted_data" => $encryptedPayload
        ]);
        exit;
    }

    echo json_encode($responseArray, JSON_UNESCAPED_UNICODE);
    exit;
}

// If accessed from a web browser directly, route to the dashboard
require_once __DIR__ . '/index.php';
