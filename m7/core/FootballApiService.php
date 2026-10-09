<?php
/**
 * M7 Modern Football Data & Statistics API Service
 * Fetches real-time match details, official lineups, substitutions, formations,
 * and comprehensive match statistics (possession, shots, corners, fouls, passes)
 * using the modern ESPN Global Soccer API + TheSportsDB (Open & Free).
 */

class FootballApiService {

    /**
     * Search or match fixtures from ESPN global soccer API
     */
    public static function searchMatches($query = '', $date = null) {
        $dateStr = !empty($date) ? preg_replace('/[^0-9]/', '', $date) : date('Ymd');
        $url = "https://site.api.espn.com/apis/site/v2/sports/soccer/all/scoreboard?dates=" . $dateStr;

        $response = self::httpGet($url);
        if (empty($response)) {
            return ["status" => false, "message" => "تعذر الاتصال بـ API مباريات كرة القدم", "events" => []];
        }

        $json = json_decode($response, true);
        $events = $json['events'] ?? [];
        $result = [];

        $qLower = mb_strtolower(trim($query), 'UTF-8');

        foreach ($events as $ev) {
            $eId = strval($ev['id'] ?? '');
            $name = $ev['name'] ?? '';
            $date = $ev['date'] ?? '';
            $statusType = $ev['status']['type']['name'] ?? 'STATUS_SCHEDULED';
            $statusDesc = $ev['status']['type']['description'] ?? '';
            $clock = $ev['status']['displayClock'] ?? '';

            $competition = $ev['competitions'][0] ?? [];
            $leagueName = $ev['season']['slug'] ?? ($competition['type']['text'] ?? 'كرة القدم');
            
            $homeTeam = [];
            $awayTeam = [];
            $homeScore = null;
            $awayScore = null;

            foreach ($competition['competitors'] ?? [] as $c) {
                $t = [
                    'id' => $c['id'] ?? '',
                    'name' => $c['team']['displayName'] ?? ($c['team']['name'] ?? ''),
                    'short_name' => $c['team']['shortDisplayName'] ?? '',
                    'logo' => $c['team']['logo'] ?? '',
                    'color' => $c['team']['color'] ?? '0A84FF'
                ];
                if (($c['homeAway'] ?? '') === 'home') {
                    $homeTeam = $t;
                    $homeScore = isset($c['score']) ? intval($c['score']) : null;
                } else {
                    $awayTeam = $t;
                    $awayScore = isset($c['score']) ? intval($c['score']) : null;
                }
            }

            // Filter if query is provided
            if (!empty($qLower)) {
                $hName = mb_strtolower($homeTeam['name'] ?? '', 'UTF-8');
                $aName = mb_strtolower($awayTeam['name'] ?? '', 'UTF-8');
                $lName = mb_strtolower($leagueName, 'UTF-8');
                if (strpos($hName, $qLower) === false && strpos($aName, $qLower) === false && strpos($lName, $qLower) === false) {
                    continue;
                }
            }

            $isLive = in_array($statusType, ['STATUS_IN_PROGRESS', 'STATUS_HALFTIME', 'STATUS_FIRST_HALF', 'STATUS_SECOND_HALF']);
            $isEnded = in_array($statusType, ['STATUS_FINAL', 'STATUS_FULL_TIME']);

            $result[] = [
                'api_id' => $eId,
                'name' => $name,
                'league' => $leagueName,
                'date' => substr($date, 0, 10),
                'time' => substr($date, 11, 5),
                'is_live' => $isLive,
                'is_ended' => $isEnded,
                'status_text' => $statusDesc,
                'clock' => $clock,
                'home_team' => $homeTeam,
                'away_team' => $awayTeam,
                'home_score' => $homeScore,
                'away_score' => $awayScore,
            ];
        }

        return [
            "status" => true,
            "count" => count($result),
            "date" => $dateStr,
            "events" => $result
        ];
    }

    /**
     * Fetch match detailed statistics, lineup, roster, formations and events
     */
    public static function getMatchDetails($eventId) {
        $eId = preg_replace('/[^0-9]/', '', $eventId);
        if (empty($eId)) {
            return ["status" => false, "message" => "معرف المباراة غير صالح"];
        }

        $url = "https://site.api.espn.com/apis/site/v2/sports/soccer/all/summary?event=" . $eId;
        $response = self::httpGet($url);

        if (empty($response)) {
            return ["status" => false, "message" => "فشل استرداد تفاصيل المباراة من الـ API"];
        }

        $data = json_decode($response, true);
        if (empty($data)) {
            return ["status" => false, "message" => "بيانات غير صالحة من الـ API"];
        }

        // 1. Teams & Score
        $header = $data['header'] ?? [];
        $comp = $header['competitions'][0] ?? [];
        $homeTeam = [];
        $awayTeam = [];
        $homeScore = null;
        $awayScore = null;

        foreach ($comp['competitors'] ?? [] as $c) {
            $t = [
                'name' => $c['team']['displayName'] ?? '',
                'logo' => $c['team']['logos'][0]['href'] ?? ($c['team']['logo'] ?? ''),
                'color' => '#' . ($c['team']['color'] ?? '0A84FF')
            ];
            if (($c['homeAway'] ?? '') === 'home') {
                $homeTeam = $t;
                $homeScore = $c['score'] ?? null;
            } else {
                $awayTeam = $t;
                $awayScore = $c['score'] ?? null;
            }
        }

        // 2. Statistics Parsing
        $statsList = [];
        $boxscore = $data['boxscore'] ?? [];
        $boxTeams = $boxscore['teams'] ?? [];

        $homeStatsMap = [];
        $awayStatsMap = [];

        foreach ($boxTeams as $bt) {
            $isHome = ($bt['homeAway'] ?? '') === 'home';
            foreach ($bt['statistics'] ?? [] as $st) {
                $k = $st['name'] ?? '';
                $val = $st['displayValue'] ?? '0';
                if ($isHome) {
                    $homeStatsMap[$k] = $val;
                } else {
                    $awayStatsMap[$k] = $val;
                }
            }
        }

        // Standardized Clean Stats
        $statKeys = [
            'possessionPct' => 'نسبة الاستحواذ (%)',
            'totalShots' => 'إجمالي التسديدات',
            'shotsOnTarget' => 'التسديدات على المرمى',
            'wonCorners' => 'الضربات الركنية',
            'foulsCommitted' => 'الأخطاء المرتكبة',
            'yellowCards' => 'البطاقات الصفراء',
            'redCards' => 'البطاقات الحمراء',
            'totalPasses' => 'إجمالي التمريرات',
            'accuratePasses' => 'التمريرات الناجحة',
            'passPct' => 'دقة التمرير (%)',
            'saves' => 'التصديات',
            'offsides' => 'حالات التسلل'
        ];

        foreach ($statKeys as $key => $arabicLabel) {
            $hVal = $homeStatsMap[$key] ?? '0';
            $aVal = $awayStatsMap[$key] ?? '0';
            // Clean percentages
            if (strpos($key, 'Pct') !== false) {
                $hNum = floatval($hVal);
                $aNum = floatval($aVal);
                if ($hNum <= 1.0 && $aNum <= 1.0 && ($hNum + $aNum) > 0) {
                    $hVal = strval(round($hNum * 100)) . '%';
                    $aVal = strval(round($aNum * 100)) . '%';
                } else {
                    $hVal = strval(round($hNum)) . '%';
                    $aVal = strval(round($aNum)) . '%';
                }
            }
            $statsList[] = [
                'name' => $key,
                'label' => $arabicLabel,
                'home' => $hVal,
                'away' => $aVal
            ];
        }

        // 3. Lineups & Formations (الروستر والتشكيلة)
        $lineups = [
            'home' => ['formation' => '4-3-3', 'starters' => [], 'subs' => []],
            'away' => ['formation' => '4-3-3', 'starters' => [], 'subs' => []]
        ];

        foreach ($data['rosters'] ?? [] as $r) {
            $side = ($r['homeAway'] ?? '') === 'home' ? 'home' : 'away';
            $formation = $r['formation'] ?? '4-3-3';
            $lineups[$side]['formation'] = $formation;

            foreach ($r['roster'] ?? [] as $p) {
                $athlete = $p['athlete'] ?? [];
                $playerItem = [
                    'id' => $athlete['id'] ?? '',
                    'name' => $athlete['displayName'] ?? '',
                    'short_name' => $athlete['shortName'] ?? '',
                    'jersey' => $p['jersey'] ?? '',
                    'position' => $p['position']['abbreviation'] ?? ($p['position']['name'] ?? 'MID'),
                    'starter' => !empty($p['starter']),
                    'subbed_in' => !empty($p['subbedIn']),
                    'photo' => $athlete['headshot']['href'] ?? ''
                ];

                if (!empty($p['starter'])) {
                    $lineups[$side]['starters'][] = $playerItem;
                } else {
                    $lineups[$side]['subs'][] = $playerItem;
                }
            }
        }

        // 4. Timeline / Key Events
        $eventsList = [];
        foreach ($data['keyEvents'] ?? [] as $ke) {
            $clock = $ke['clock']['displayValue'] ?? '';
            $text = $ke['text'] ?? '';
            $type = $ke['type']['text'] ?? 'Event';
            $team = $ke['team']['displayName'] ?? '';
            $eventsList[] = [
                'time' => $clock,
                'type' => $type,
                'text' => $text,
                'team' => $team
            ];
        }

        return [
            "status" => true,
            "event_id" => $eId,
            "home_team" => $homeTeam,
            "away_team" => $awayTeam,
            "home_score" => $homeScore,
            "away_score" => $awayScore,
            "statistics" => $statsList,
            "lineups" => $lineups,
            "events" => $eventsList
        ];
    }

    private static function httpGet($url) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, 8);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept: application/json"
        ]);
        $res = curl_exec($ch);
        curl_close($ch);
        return $res;
    }
}
