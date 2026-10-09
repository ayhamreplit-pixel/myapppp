<?php
/**
 * M7 Matches & Streaming Service
 * Strictly manages matches created manually by the administrator (No external scraping API).
 */

class MatchesService {

    public static function getMatchesFile() {
        return 'custom_matches.json';
    }

    /**
     * Retrieve all manually added matches, optionally filtered by date
     */
    public static function getEnrichedMatches($targetDate = null, $forceRefresh = false, $encryptForApi = false) {
        $customMatches = Storage::get(self::getMatchesFile(), []);
        $masterKey = Security::getMasterKey();
        $isEncEnabled = Security::isEncryptionEnabled();

        $filtered = [];
        foreach ($customMatches as $cm) {
            // If targetDate specified, filter by date, otherwise return all
            // If date is 'all' or matches date, include it. If no matches exist on selected date in custom matches, fallback allows showing available scheduled matches
            if (!empty($targetDate) && $targetDate !== 'all') {
                $mDate = $cm['match_date'] ?? date('Y-m-d');
                if ($mDate !== $targetDate && $mDate !== 'اليوم' && $targetDate !== 'today') {
                    // Check if match is explicitly for another date
                    if (preg_match('/^\d{4}-\d{2}-\d{2}$/', $targetDate) && preg_match('/^\d{4}-\d{2}-\d{2}$/', $mDate) && $mDate !== $targetDate) {
                        continue;
                    }
                }
            }

            // Normalization
            $cm['match_id'] = strval($cm['match_id'] ?? ('m_' . time()));
            if (!isset($cm['servers']) || !is_array($cm['servers'])) {
                $cm['servers'] = [];
            }

            // Ensure stream active flag
            $hasServers = !empty($cm['servers']);
            $isStreamActive = !empty($cm['stream_active']) || !empty($cm['is_stream_active']);
            $cm['stream_active'] = $isStreamActive ? 1 : 0;
            $cm['is_stream_active'] = $isStreamActive;

            // In user app API mode, only active/enabled matches are served
            if ($encryptForApi && !$isStreamActive) {
                continue;
            }

            // Stream URL fallback
            if (empty($cm['streamUrl']) && $hasServers) {
                $cm['streamUrl'] = $cm['servers'][0]['url'] ?? '';
            }

            if ($encryptForApi && $isEncEnabled) {
                self::encryptMatchStreams($cm, $masterKey);
            }

            $filtered[] = $cm;
        }

        // Sort: Live first, then by match_time ascending
        usort($filtered, function($a, $b) {
            $aLive = in_array(intval($a['status'] ?? 0), [2, 3]) ? 1 : 0;
            $bLive = in_array(intval($b['status'] ?? 0), [2, 3]) ? 1 : 0;
            if ($aLive !== $bLive) return $bLive - $aLive;
            return strcmp($a['match_time'] ?? '', $b['match_time'] ?? '');
        });

        return $filtered;
    }

    /**
     * Add or update a match with complete attributes
     */
    public static function saveMatch($input) {
        $matches = Storage::get(self::getMatchesFile(), []);
        $mId = trim($input['match_id'] ?? '');
        if (empty($mId)) {
            $mId = 'm_' . time() . '_' . rand(100, 999);
        }

        $status = intval($input['status'] ?? 1);
        $isLive = in_array($status, [2, 3]) ? 1 : intval($input['live'] ?? 0);

        // Prepare servers list
        $servers = [];
        if (isset($input['servers']) && is_array($input['servers'])) {
            $servers = $input['servers'];
        } elseif (!empty($input['stream_url'])) {
            $servers[] = [
                'id' => 'srv_1',
                'name' => trim($input['server_name'] ?? 'سيرفر رئيسي FHD'),
                'url' => trim($input['stream_url']),
                'quality' => trim($input['quality'] ?? '1080p FHD')
            ];
        }

        $streamUrl = !empty($input['stream_url']) ? trim($input['stream_url']) : (!empty($servers[0]['url']) ? $servers[0]['url'] : '');
        $isStreamActive = isset($input['stream_active']) ? (bool)$input['stream_active'] : (!empty($servers) || !empty($streamUrl));

        $newMatch = [
            'match_id' => $mId,
            'championship' => [
                'title' => trim($input['championship_title'] ?? $input['championship'] ?? 'بطولة خاصة'),
                'image' => trim($input['championship_image'] ?? '')
            ],
            'home_team' => [
                'title' => trim($input['home_team_title'] ?? $input['home_team'] ?? 'الفريق الأول'),
                'image' => trim($input['home_team_image'] ?? '')
            ],
            'away_team' => [
                'title' => trim($input['away_team_title'] ?? $input['away_team'] ?? 'الفريق الثاني'),
                'image' => trim($input['away_team_image'] ?? '')
            ],
            'home_scores' => (isset($input['home_scores']) && $input['home_scores'] !== '') ? intval($input['home_scores']) : null,
            'away_scores' => (isset($input['away_scores']) && $input['away_scores'] !== '') ? intval($input['away_scores']) : null,
            'match_date' => trim($input['match_date'] ?? date('Y-m-d')),
            'match_time' => trim($input['match_time'] ?? '21:00:00'),
            'status' => $status,
            'live' => $isLive,
            'score_time' => trim($input['score_time'] ?? ''),
            'channel_name' => trim($input['channel_name'] ?? 'beIN SPORTS 1 HD'),
            'commentator' => trim($input['commentator'] ?? 'تعليق عربي'),
            'stadium' => trim($input['stadium'] ?? 'الملعب الرئيسي'),
            'referee' => trim($input['referee'] ?? ''),
            'stream_active' => $isStreamActive ? 1 : 0,
            'is_stream_active' => $isStreamActive,
            'streamUrl' => $streamUrl,
            'servers' => $servers,
            'events' => isset($input['events']) && is_array($input['events']) ? $input['events'] : [],
            'lineup' => isset($input['lineup']) && is_array($input['lineup']) ? $input['lineup'] : null
        ];

        $exists = false;
        foreach ($matches as &$m) {
            if (strval($m['match_id'] ?? '') === $mId) {
                // If preserve existing servers when not passed in simple edit
                if (empty($servers) && !empty($m['servers'])) {
                    $newMatch['servers'] = $m['servers'];
                    if (empty($newMatch['streamUrl'])) $newMatch['streamUrl'] = $m['servers'][0]['url'] ?? '';
                    $newMatch['stream_active'] = 1;
                    $newMatch['is_stream_active'] = true;
                }
                $m = $newMatch;
                $exists = true;
                break;
            }
        }

        if (!$exists) {
            $matches[] = $newMatch;
        }

        Storage::save(self::getMatchesFile(), $matches);
        return [
            "status" => true,
            "message" => "تم حفظ وتحديث بيانات المباراة بنجاح ⚽",
            "match" => $newMatch
        ];
    }

    /**
     * Delete a match
     */
    public static function deleteMatch($matchId) {
        $matches = Storage::get(self::getMatchesFile(), []);
        $filtered = array_values(array_filter($matches, function($m) use ($matchId) {
            return strval($m['match_id'] ?? '') !== strval($matchId);
        }));
        Storage::save(self::getMatchesFile(), $filtered);
        return ["status" => true, "message" => "تم حذف المباراة بنجاح 🗑️"];
    }

    /**
     * Quick Score and Status update
     */
    public static function quickUpdateScore($matchId, $homeScores, $awayScores, $status, $scoreTime = '') {
        $matches = Storage::get(self::getMatchesFile(), []);
        $found = false;
        foreach ($matches as &$m) {
            if (strval($m['match_id'] ?? '') === strval($matchId)) {
                if ($homeScores !== null && $homeScores !== '') $m['home_scores'] = intval($homeScores);
                if ($awayScores !== null && $awayScores !== '') $m['away_scores'] = intval($awayScores);
                if ($status !== null && $status !== '') {
                    $st = intval($status);
                    $m['status'] = $st;
                    $m['live'] = in_array($st, [2, 3]) ? 1 : 0;
                }
                if ($scoreTime !== null) $m['score_time'] = trim($scoreTime);
                $found = true;
                break;
            }
        }
        if ($found) {
            Storage::save(self::getMatchesFile(), $matches);
            return ["status" => true, "message" => "تم تحديث النتيجة والدقيقة فورياً! ⚡"];
        }
        return ["status" => false, "message" => "المباراة غير موجودة"];
    }

    /**
     * Toggle stream active
     */
    public static function toggleStream($matchId, $active = null) {
        $matches = Storage::get(self::getMatchesFile(), []);
        $newState = false;
        $found = false;
        foreach ($matches as &$m) {
            if (strval($m['match_id'] ?? '') === strval($matchId)) {
                $newState = ($active !== null) ? (bool)$active : empty($m['is_stream_active']);
                $m['stream_active'] = $newState ? 1 : 0;
                $m['is_stream_active'] = $newState;
                $found = true;
                break;
            }
        }

        if ($found) {
            Storage::save(self::getMatchesFile(), $matches);
            return [
                "status" => true,
                "stream_active" => $newState,
                "message" => $newState ? "تم تفعيل البث المباشر للمباراة بنجاح ⚡" : "تم تعطيل البث للمباراة"
            ];
        }
        return ["status" => false, "message" => "المباراة غير موجودة"];
    }

    /**
     * Duplicate a match
     */
    public static function cloneMatch($matchId) {
        $matches = Storage::get(self::getMatchesFile(), []);
        foreach ($matches as $m) {
            if (strval($m['match_id'] ?? '') === strval($matchId)) {
                $cloned = $m;
                $cloned['match_id'] = 'm_' . time() . '_' . rand(100, 999);
                $cloned['home_scores'] = null;
                $cloned['away_scores'] = null;
                $cloned['status'] = 1;
                $cloned['live'] = 0;
                $cloned['score_time'] = '';
                $matches[] = $cloned;
                Storage::save(self::getMatchesFile(), $matches);
                return ["status" => true, "message" => "تم نسخ المباراة بنجاح 📋", "new_id" => $cloned['match_id']];
            }
        }
        return ["status" => false, "message" => "المباراة غير موجودة"];
    }

    /**
     * Encrypt stream URLs for Android client
     */
    private static function encryptMatchStreams(&$match, $key) {
        if (!empty($match['servers'])) {
            foreach ($match['servers'] as &$srv) {
                if (!empty($srv['url'])) $srv['url'] = Security::encrypt($srv['url'], $key);
            }
        }
        if (!empty($match['streamUrl'])) {
            $match['streamUrl'] = Security::encrypt($match['streamUrl'], $key);
        }
    }
}
