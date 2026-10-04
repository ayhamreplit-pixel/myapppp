<?php
/**
 * M7 Matches & Streaming Service
 * Strictly fetches real-time match data without inserting hardcoded mock fallbacks.
 */

class MatchesService {

    public static function fetchLiveMatches($targetDate, $forceRefresh = false) {
        $cacheFile = M7_CACHE_DIR . "/matches_" . preg_replace('/[^0-9\-]/', '', $targetDate) . ".json";
        $isToday = ($targetDate === date('Y-m-d'));
        $cacheTTL = $isToday ? 20 : 600;

        if (!$forceRefresh && file_exists($cacheFile) && (time() - filemtime($cacheFile) < $cacheTTL)) {
            $cached = @file_get_contents($cacheFile);
            $decoded = json_decode($cached, true);
            if ($decoded && isset($decoded['data']) && is_array($decoded['data'])) {
                return $decoded['data'];
            }
        }

        // 1. Primary Arabic Source: YSScores API
        $apiUrl = "https://api-ar.ysscores.com/api/matches/matches_date_get/" . urlencode($targetDate) . "/%5B%5D/%5B%5D/%5B%5D/D/180";
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
            if ($decoded !== null && isset($decoded['data']) && is_array($decoded['data'])) {
                @file_put_contents($cacheFile, $response);
                return $decoded['data'];
            }
        }

        // Fallback to cache if exists
        if (file_exists($cacheFile)) {
            $cached = @file_get_contents($cacheFile);
            $decoded = json_decode($cached, true);
            if ($decoded && isset($decoded['data'])) {
                return $decoded['data'];
            }
        }

        return [];
    }

    public static function getEnrichedMatches($targetDate, $forceRefresh = false, $encryptForApi = false) {
        $rawMatches = self::fetchLiveMatches($targetDate, $forceRefresh);
        $overrides = Storage::get('match_overrides.json', []);
        $customMatches = Storage::get('custom_matches.json', []);
        $masterKey = Security::getMasterKey();
        $isEncEnabled = Security::isEncryptionEnabled();

        $enriched = [];

        // 1. Custom user matches for this date
        foreach ($customMatches as $cm) {
            if (($cm['match_date'] ?? date('Y-m-d')) === $targetDate) {
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

                if ($encryptForApi && $isEncEnabled) {
                    self::encryptMatchStreams($cm, $masterKey);
                }
                $enriched[] = $cm;
            }
        }

        // 2. Live API Matches
        foreach ($rawMatches as $m) {
            $mId = strval($m['match_id'] ?? '');
            if (empty($mId) || (isset($overrides[$mId]) && !empty($overrides[$mId]['hidden']))) continue;

            $m['servers'] = [];
            $m['is_stream_active'] = false;
            $m['streamUrl'] = '';

            if (isset($overrides[$mId])) {
                $ov = $overrides[$mId];
                $isStreamActive = !empty($ov['stream_active']);
                $m['is_stream_active'] = $isStreamActive;
                if ($isStreamActive && !empty($ov['servers'])) {
                    $m['servers'] = $ov['servers'];
                    $m['streamUrl'] = $ov['servers'][0]['url'];
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

            if ($encryptForApi && $isEncEnabled) {
                self::encryptMatchStreams($m, $masterKey);
            }
            $enriched[] = $m;
        }

        return $enriched;
    }

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

    public static function toggleStream($matchId, $active = null) {
        $overrides = Storage::get('match_overrides.json', []);
        if (!isset($overrides[$matchId])) {
            $overrides[$matchId] = ["servers" => [], "hidden" => false, "featured" => false, "stream_active" => false];
        }
        $newState = ($active !== null) ? (bool)$active : !($overrides[$matchId]['stream_active'] ?? false);
        $overrides[$matchId]['stream_active'] = $newState;

        Storage::save('match_overrides.json', $overrides);
        return [
            "status" => true,
            "stream_active" => $newState,
            "servers" => $overrides[$matchId]['servers'] ?? [],
            "message" => $newState ? "تم تفعيل البث المباشر للمباراة بنجاح ⚡" : "تم تعطيل البث"
        ];
    }
}
