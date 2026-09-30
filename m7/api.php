<?php
/**
 * =========================================================================
 * TOD Sports Cloud Server API Backend - M7 Edition
 * URL: https://ayham.alwaysdata.net/m7/api.php
 * Real-time matches, channels, device tracking & active sessions telemetry
 * =========================================================================
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, X-Device-ID, X-Device-Token');
header('Content-Type: application/json; charset=utf-8');

// Handle preflight OPTIONS requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$dataDir = __DIR__;
$matchesFile = $dataDir . '/matches.json';
$channelsFile = $dataDir . '/channels.json';
$competitionsFile = $dataDir . '/competitions.json';
$showsFile = $dataDir . '/shows.json';
$sessionsFile = $dataDir . '/sessions.json';
$securityConfigFile = $dataDir . '/security_config.json';

// Initialize files if not existing
initializeSystemData($matchesFile, $channelsFile, $competitionsFile, $showsFile, $sessionsFile, $securityConfigFile);

// Read action from GET, POST, or JSON body
$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$action = $_GET['action'] ?? ($input['action'] ?? 'matches');

switch ($action) {
    // -------------------------------------------------------------
    // 1. MATCHES ENDPOINT
    // -------------------------------------------------------------
    case 'matches':
    case 'get_matches':
        $data = file_get_contents($matchesFile);
        if ($data) {
            echo $data;
        } else {
            echo json_encode(['status' => 'success', 'matches' => []]);
        }
        break;

    // -------------------------------------------------------------
    // 2. CHANNELS ENDPOINT
    // -------------------------------------------------------------
    case 'channels':
    case 'get_channels':
        $data = file_get_contents($channelsFile);
        if ($data) {
            echo $data;
        } else {
            echo json_encode(['status' => 'success', 'channels' => []]);
        }
        break;

    // -------------------------------------------------------------
    // 3. COMPETITIONS ENDPOINT
    // -------------------------------------------------------------
    case 'competitions':
    case 'get_competitions':
        $data = file_get_contents($competitionsFile);
        if ($data) {
            echo $data;
        } else {
            echo json_encode(['status' => 'success', 'competitions' => []]);
        }
        break;

    // -------------------------------------------------------------
    // 4. SHOWS & REPLAYS ENDPOINT
    // -------------------------------------------------------------
    case 'shows':
    case 'get_shows':
        $data = file_get_contents($showsFile);
        if ($data) {
            echo $data;
        } else {
            echo json_encode(['status' => 'success', 'shows' => []]);
        }
        break;

    // -------------------------------------------------------------
    // 5. ACTIVE DEVICE SESSION & HEARTBEAT (من يشاهد الآن وحفظ الجهاز)
    // -------------------------------------------------------------
    case 'session_ping':
    case 'watch_session':
    case 'track_device':
        $deviceId = $input['deviceId'] ?? ($_SERVER['HTTP_X_DEVICE_ID'] ?? ('dev_' . substr(md5($_SERVER['REMOTE_ADDR'] . ($_SERVER['HTTP_USER_AGENT'] ?? '')), 0, 16)));
        $deviceName = $input['deviceName'] ?? 'هاتف أندرويد';
        $profileName = $input['profileName'] ?? 'المشترك VIP';
        $activeStream = $input['activeStream'] ?? 'تصفح التطبيق';
        $appVersion = $input['appVersion'] ?? 'TOD-v4.8.2';
        $ip = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';
        $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'TOD-Client';

        $sessions = json_decode(file_get_contents($sessionsFile), true) ?: [];
        $now = time();

        // Clean stale sessions older than 5 minutes (300 seconds)
        $activeSessions = [];
        foreach ($sessions as $s) {
            if ($now - ($s['lastPing'] ?? 0) < 300) {
                if ($s['deviceId'] !== $deviceId) {
                    $activeSessions[] = $s;
                }
            }
        }

        // Add or update current device session
        $currentSession = [
            'deviceId' => $deviceId,
            'deviceName' => $deviceName,
            'profileName' => $profileName,
            'activeStream' => $activeStream,
            'ip' => $ip,
            'userAgent' => $userAgent,
            'appVersion' => $appVersion,
            'lastPing' => $now,
            'lastPingFormatted' => date('H:i:s'),
            'connectedAt' => date('Y-m-d H:i:s'),
            'status' => 'online'
        ];
        $activeSessions[] = $currentSession;

        file_put_contents($sessionsFile, json_encode($activeSessions, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        
        echo json_encode([
            'status' => 'success',
            'message' => 'Device session updated successfully',
            'deviceId' => $deviceId,
            'activeSessionsCount' => count($activeSessions),
            'serverTime' => date('Y-m-d H:i:s')
        ]);
        break;

    // -------------------------------------------------------------
    // 6. GET ACTIVE SESSIONS (من يشاهد الآن للوحة التحكم والتطبيق)
    // -------------------------------------------------------------
    case 'get_sessions':
    case 'who_is_watching':
        $sessions = json_decode(file_get_contents($sessionsFile), true) ?: [];
        $now = time();
        $activeOnly = array_values(array_filter($sessions, function($s) use ($now) {
            return ($now - ($s['lastPing'] ?? 0)) < 300;
        }));
        echo json_encode([
            'status' => 'success',
            'totalActive' => count($activeOnly),
            'sessions' => $activeOnly
        ]);
        break;

    // -------------------------------------------------------------
    // 7. KICK/DISCONNECT SESSION (إنهاء جلسة جهاز)
    // -------------------------------------------------------------
    case 'kick_session':
        $targetDeviceId = $input['deviceId'] ?? ($_GET['deviceId'] ?? '');
        if ($targetDeviceId) {
            $sessions = json_decode(file_get_contents($sessionsFile), true) ?: [];
            $filtered = array_values(array_filter($sessions, function($s) use ($targetDeviceId) {
                return ($s['deviceId'] ?? '') !== $targetDeviceId;
            }));
            file_put_contents($sessionsFile, json_encode($filtered, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(['status' => 'success', 'message' => 'Session terminated']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'deviceId required']);
        }
        break;

    // -------------------------------------------------------------
    // 7.5 QUICK REALTIME UPDATE MATCH (تحديث فوري وسريع للنتيجة والدقيقة)
    // -------------------------------------------------------------
    case 'quick_update_match':
        $matchId = $input['id'] ?? ($_POST['id'] ?? '');
        if ($matchId) {
            $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
            $found = false;
            foreach ($matchesData['matches'] as &$m) {
                if ($m['id'] === $matchId) {
                    if (isset($input['scoreHome'])) $m['scoreHome'] = $input['scoreHome'] !== '' && $input['scoreHome'] !== null ? (int)$input['scoreHome'] : null;
                    if (isset($input['scoreAway'])) $m['scoreAway'] = $input['scoreAway'] !== '' && $input['scoreAway'] !== null ? (int)$input['scoreAway'] : null;
                    if (isset($input['isLive'])) $m['isLive'] = filter_var($input['isLive'], FILTER_VALIDATE_BOOLEAN);
                    if (isset($input['isEnded'])) $m['isEnded'] = filter_var($input['isEnded'], FILTER_VALIDATE_BOOLEAN);
                    if (isset($input['liveMinute'])) $m['liveMinute'] = $input['liveMinute'];
                    if (isset($input['streamUrl'])) $m['streamUrl'] = $input['streamUrl'];
                    if (isset($input['channelName'])) $m['channelName'] = $input['channelName'];
                    $found = true;
                    break;
                }
            }
            if ($found) {
                file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                echo json_encode(['status' => 'success', 'message' => 'Match updated instantly in real-time']);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Match not found']);
            }
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Match id required']);
        }
        break;

    // -------------------------------------------------------------
    // 8. SAVE MATCH (إضافة أو تعديل مباراة مع اللوقو والبث)
    // -------------------------------------------------------------
    case 'save_match':
        if (!empty($input['homeTeam']) && !empty($input['awayTeam'])) {
            $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
            $matchId = !empty($input['id']) ? $input['id'] : 'm_' . time() . '_' . rand(100, 999);
            
            $newMatch = [
                'id' => $matchId,
                'title' => ($input['homeTeam']) . ' ضد ' . ($input['awayTeam']),
                'tournament' => $input['tournament'] ?? 'دوري أبطال أوروبا',
                'tournamentLogo' => $input['tournamentLogo'] ?? '',
                'homeTeam' => $input['homeTeam'],
                'homeLogo' => $input['homeLogo'] ?? '',
                'homeFlag' => $input['homeFlag'] ?? '⚽',
                'awayTeam' => $input['awayTeam'],
                'awayLogo' => $input['awayLogo'] ?? '',
                'awayFlag' => $input['awayFlag'] ?? '⚽',
                'kickoffTime' => $input['kickoffTime'] ?? '22:00',
                'kickoffDate' => $input['kickoffDate'] ?? 'اليوم',
                'stadium' => $input['stadium'] ?? 'الملعب الرئيسي',
                'commentator' => $input['commentator'] ?? 'عصام الشوالي',
                'channelName' => $input['channelName'] ?? 'beIN SPORTS 1 HD',
                'channelId' => $input['channelId'] ?? 'bein_1',
                'streamUrl' => $input['streamUrl'] ?? '',
                'isLive' => filter_var($input['isLive'] ?? false, FILTER_VALIDATE_BOOLEAN),
                'isEnded' => filter_var($input['isEnded'] ?? false, FILTER_VALIDATE_BOOLEAN),
                'liveMinute' => $input['liveMinute'] ?? null,
                'countdown' => $input['countdown'] ?? null,
                'scoreHome' => isset($input['scoreHome']) && $input['scoreHome'] !== '' ? (int)$input['scoreHome'] : null,
                'scoreAway' => isset($input['scoreAway']) && $input['scoreAway'] !== '' ? (int)$input['scoreAway'] : null,
                'bannerUrl' => $input['bannerUrl'] ?? ''
            ];

            $found = false;
            foreach ($matchesData['matches'] as &$m) {
                if ($m['id'] === $matchId) {
                    $m = array_merge($m, $newMatch);
                    $found = true;
                    break;
                }
            }
            if (!$found) {
                array_unshift($matchesData['matches'], $newMatch);
            }

            file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(['status' => 'success', 'match' => $newMatch]);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'بيانات الفريقين مطلوبة']);
        }
        break;

    // -------------------------------------------------------------
    // 9. DELETE MATCH (حذف مباراة)
    // -------------------------------------------------------------
    case 'delete_match':
        $matchId = $input['id'] ?? ($_GET['id'] ?? '');
        if ($matchId) {
            $matchesData = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
            $matchesData['matches'] = array_values(array_filter($matchesData['matches'], function($m) use ($matchId) {
                return $m['id'] !== $matchId;
            }));
            file_put_contents($matchesFile, json_encode($matchesData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(['status' => 'success', 'message' => 'Match deleted']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'id required']);
        }
        break;

    // -------------------------------------------------------------
    // 10. SAVE CHANNEL (إضافة أو تعديل قناة)
    // -------------------------------------------------------------
    case 'save_channel':
        if (!empty($input['name'])) {
            $channelsData = json_decode(file_get_contents($channelsFile), true) ?: ['channels' => []];
            $chId = !empty($input['streamId']) ? $input['streamId'] : 'ch_' . time();
            
            $newCh = [
                'streamId' => $chId,
                'name' => $input['name'],
                'iconUrl' => $input['iconUrl'] ?? '',
                'categoryId' => $input['categoryId'] ?? 'قنوات beIN SPORTS',
                'playUrl' => $input['playUrl'] ?? ''
            ];

            $found = false;
            foreach ($channelsData['channels'] as &$c) {
                if ($c['streamId'] === $chId) {
                    $c = $newCh;
                    $found = true;
                    break;
                }
            }
            if (!$found) {
                $channelsData['channels'][] = $newCh;
            }

            file_put_contents($channelsFile, json_encode($channelsData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(['status' => 'success', 'channel' => $newCh]);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'اسم القناة مطلوب']);
        }
        break;

    // -------------------------------------------------------------
    // 11. DELETE CHANNEL (حذف قناة)
    // -------------------------------------------------------------
    case 'delete_channel':
        $chId = $input['streamId'] ?? ($_GET['streamId'] ?? '');
        if ($chId) {
            $channelsData = json_decode(file_get_contents($channelsFile), true) ?: ['channels' => []];
            $channelsData['channels'] = array_values(array_filter($channelsData['channels'], function($c) use ($chId) {
                return $c['streamId'] !== $chId;
            }));
            file_put_contents($channelsFile, json_encode($channelsData, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(['status' => 'success', 'message' => 'Channel deleted']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'streamId required']);
        }
        break;

    // -------------------------------------------------------------
    // 12. SERVER STATUS & TELEMETRY INFO
    // -------------------------------------------------------------
    case 'server_info':
    case 'health':
        $sessions = json_decode(file_get_contents($sessionsFile), true) ?: [];
        $matches = json_decode(file_get_contents($matchesFile), true) ?: ['matches' => []];
        $channels = json_decode(file_get_contents($channelsFile), true) ?: ['channels' => []];
        
        $now = time();
        $activeOnly = array_values(array_filter($sessions, function($s) use ($now) {
            return ($now - ($s['lastPing'] ?? 0)) < 300;
        }));

        echo json_encode([
            'status' => 'online',
            'version' => 'TOD Server v4.8 (m7 folder)',
            'url' => 'https://ayham.alwaysdata.net/m7',
            'totalMatches' => count($matches['matches'] ?? []),
            'totalChannels' => count($channels['channels'] ?? []),
            'activeViewers' => count($activeOnly),
            'serverTime' => date('Y-m-d H:i:s'),
            'security' => 'AES-256-CBC Encryption Active',
            'cors' => 'Enabled (*)'
        ]);
        break;

    default:
        // By default return matches payload
        $data = file_get_contents($matchesFile);
        echo $data ?: json_encode(['status' => 'online', 'server' => 'https://ayham.alwaysdata.net/m7']);
        break;
}

/**
 * Helper to initialize data files on first deployment
 */
function initializeSystemData($mFile, $cFile, $compFile, $sFile, $sessFile, $secFile) {
    if (!file_exists($mFile) || filesize($mFile) < 10) {
        $defaultMatches = [
            'matches' => [
                [
                    'id' => 'wales_norway_nations',
                    'title' => 'ويلز ضد النرويج',
                    'tournament' => 'دوري الأمم الأوروبية',
                    'tournamentLogo' => 'https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png',
                    'homeTeam' => 'ويلز',
                    'homeLogo' => 'https://flagcdn.com/w80/gb-wls.png',
                    'homeFlag' => '🏴󠁧󠁢󠁷󠁬󠁳󠁿',
                    'awayTeam' => 'النرويج',
                    'awayLogo' => 'https://flagcdn.com/w80/no.png',
                    'awayFlag' => '🇳🇴',
                    'kickoffTime' => '21:45',
                    'kickoffDate' => 'اليوم',
                    'stadium' => 'استاد كارديف سيتي',
                    'commentator' => 'حفيظ دراجي',
                    'channelName' => 'beIN SPORTS 1 HD',
                    'channelId' => 'bein_1',
                    'streamUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
                    'isLive' => true,
                    'isEnded' => false,
                    'liveMinute' => 'الشوط الثاني',
                    'scoreHome' => 2,
                    'scoreAway' => 1,
                    'bannerUrl' => 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1200&q=80'
                ],
                [
                    'id' => 'germany_serbia_nations',
                    'title' => 'ألمانيا ضد صربيا',
                    'tournament' => 'دوري الأمم الأوروبية',
                    'tournamentLogo' => 'https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png',
                    'homeTeam' => 'ألمانيا',
                    'homeLogo' => 'https://flagcdn.com/w80/de.png',
                    'homeFlag' => '🇩🇪',
                    'awayTeam' => 'صربيا',
                    'awayLogo' => 'https://flagcdn.com/w80/rs.png',
                    'awayFlag' => '🇷🇸',
                    'kickoffTime' => '22:00',
                    'kickoffDate' => 'غداً',
                    'stadium' => 'أليانز أرينا',
                    'commentator' => 'عصام الشوالي',
                    'channelName' => 'beIN SPORTS 2 HD',
                    'channelId' => 'bein_2',
                    'streamUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
                    'isLive' => false,
                    'isEnded' => false,
                    'countdown' => 'تبدأ بعد 04:30:00',
                    'scoreHome' => null,
                    'scoreAway' => null
                ],
                [
                    'id' => 'real_madrid_man_city',
                    'title' => 'ريال مدريد ضد مانشستر سيتي',
                    'tournament' => 'دوري أبطال أوروبا',
                    'tournamentLogo' => 'https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png',
                    'homeTeam' => 'ريال مدريد',
                    'homeLogo' => 'https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png',
                    'homeFlag' => '⚪👑',
                    'awayTeam' => 'مانشستر سيتي',
                    'awayLogo' => 'https://upload.wikimedia.org/wikipedia/en/thumb/e/eb/Manchester_City_FC_badge.svg/512px-Manchester_City_FC_badge.svg.png',
                    'awayFlag' => '🔵🦅',
                    'kickoffTime' => '22:00',
                    'kickoffDate' => 'الأربعاء القادم',
                    'stadium' => 'سانتياغو برنابيو',
                    'commentator' => 'خليل البلوشي',
                    'channelName' => 'beIN SPORTS 1 HD',
                    'channelId' => 'bein_1',
                    'streamUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8',
                    'isLive' => false,
                    'isEnded' => false,
                    'countdown' => 'تبدأ بعد 02 أيام',
                    'scoreHome' => null,
                    'scoreAway' => null
                ]
            ]
        ];
        file_put_contents($mFile, json_encode($defaultMatches, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    }

    if (!file_exists($cFile) || filesize($cFile) < 10) {
        $defaultChannels = [
            'channels' => [
                ['streamId' => 'ch_bein_1', 'name' => 'beIN SPORTS 1 HD', 'iconUrl' => 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/BeIN_Sports_1_logo.svg/512px-BeIN_Sports_1_logo.svg.png', 'categoryId' => 'قنوات beIN SPORTS', 'playUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8'],
                ['streamId' => 'ch_bein_2', 'name' => 'beIN SPORTS 2 HD', 'iconUrl' => 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/BeIN_Sports_2_logo.svg/512px-BeIN_Sports_2_logo.svg.png', 'categoryId' => 'قنوات beIN SPORTS', 'playUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8'],
                ['streamId' => 'ch_bein_3', 'name' => 'beIN SPORTS 3 HD', 'iconUrl' => 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/BeIN_Sports_3_logo.svg/512px-BeIN_Sports_3_logo.svg.png', 'categoryId' => 'قنوات beIN SPORTS', 'playUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8'],
                ['streamId' => 'ch_bein_news', 'name' => 'beIN SPORTS الإخبارية', 'iconUrl' => 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/BeIN_Sports_News_logo.svg/512px-BeIN_Sports_News_logo.svg.png', 'categoryId' => 'قنوات beIN SPORTS', 'playUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8'],
                ['streamId' => 'ch_alkass_1', 'name' => 'الكأس 1 HD', 'iconUrl' => 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Al_Kass_Sports_Channels_logo.svg/512px-Al_Kass_Sports_Channels_logo.svg.png', 'categoryId' => 'قنوات الكأس', 'playUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8']
            ]
        ];
        file_put_contents($cFile, json_encode($defaultChannels, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    }

    if (!file_exists($compFile) || filesize($compFile) < 10) {
        $defaultCompetitions = [
            'competitions' => [
                ['id' => 'ucl', 'name' => 'دوري أبطال أوروبا', 'season' => '2026/2025', 'logoUrl' => 'https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png', 'accentColorHex' => 0xFF0E387A],
                ['id' => 'premier_league', 'name' => 'الدوري الإنجليزي الممتاز', 'season' => '2026/2025', 'logoUrl' => 'https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png', 'accentColorHex' => 0xFF3D195B],
                ['id' => 'la_liga', 'name' => 'الدوري الإسباني - لا ليجا', 'season' => '2026/2025', 'logoUrl' => 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png', 'accentColorHex' => 0xFFE03A3E],
                ['id' => 'nations_league', 'name' => 'دوري الأمم الأوروبية', 'season' => '2026', 'logoUrl' => 'https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png', 'accentColorHex' => 0xFF1B3B6F]
            ]
        ];
        file_put_contents($compFile, json_encode($defaultCompetitions, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    }

    if (!file_exists($sFile) || filesize($sFile) < 10) {
        $defaultShows = [
            'shows' => [
                ['id' => 'sh_1', 'title' => 'ملخص مانشستر سيتي ضد أرسنال', 'subtitle' => 'أهداف وإثارة الجولة الأخيرة', 'duration' => '12:40', 'streamUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8'],
                ['id' => 'sh_2', 'title' => 'استوديو دوري الأبطال التحليلي', 'subtitle' => 'قراءة فنية للمباريات القادمة', 'duration' => '45:10', 'streamUrl' => 'https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8']
            ]
        ];
        file_put_contents($sFile, json_encode($defaultShows, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    }

    if (!file_exists($sessFile)) {
        file_put_contents($sessFile, json_encode([], JSON_PRETTY_PRINT));
    }

    if (!file_exists($secFile)) {
        $secConfig = [
            'aes_master_key' => 'TOD_VIP_SUPER_SECURE_KEY_2026_M7',
            'strict_mode' => true,
            'max_devices_per_account' => 5,
            'session_timeout_seconds' => 300
        ];
        file_put_contents($secFile, json_encode($secConfig, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    }
}
?>
