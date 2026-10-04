<?php
/**
 * M7 Hero Banners & Featured Weekly Matches Service
 */

class HeroService {

    public static function getHeroBanners($encryptForApi = false) {
        $banners = Storage::get('hero_banners.json', []);
        if ($encryptForApi && Security::isEncryptionEnabled()) {
            $key = Security::getMasterKey();
            foreach ($banners as &$hb) {
                if (!empty($hb['servers'])) {
                    foreach ($hb['servers'] as &$s) {
                        if (!empty($s['url'])) $s['url'] = Security::encrypt($s['url'], $key);
                    }
                }
                if (!empty($hb['streamUrl'])) {
                    $hb['streamUrl'] = Security::encrypt($hb['streamUrl'], $key);
                }
            }
        }
        return $banners;
    }

    public static function saveHeroBanner($data) {
        $banners = Storage::get('hero_banners.json', []);
        $hId = trim($data['id'] ?? ('hero_' . time()));
        $newHero = [
            "id" => $hId,
            "title" => trim($data['title'] ?? 'مباراة القمة'),
            "subtitle" => trim($data['subtitle'] ?? ''),
            "tournament" => trim($data['tournament'] ?? 'دوري أبطال أوروبا'),
            "tournamentLogo" => trim($data['tournamentLogo'] ?? ''),
            "homeTeam" => trim($data['homeTeam'] ?? 'الفريق 1'),
            "homeLogo" => trim($data['homeLogo'] ?? ''),
            "awayTeam" => trim($data['awayTeam'] ?? 'الفريق 2'),
            "awayLogo" => trim($data['awayLogo'] ?? ''),
            "kickoffTime" => trim($data['kickoffTime'] ?? '22:00'),
            "kickoffDate" => trim($data['kickoffDate'] ?? 'اليوم'),
            "bannerUrl" => trim($data['bannerUrl'] ?? ''),
            "channelName" => trim($data['channelName'] ?? 'beIN SPORTS 1 HD'),
            "commentator" => trim($data['commentator'] ?? 'تعليق عربي'),
            "stadium" => trim($data['stadium'] ?? 'الملعب الرئيسي'),
            "isLive" => !empty($data['isLive']),
            "liveMinute" => trim($data['liveMinute'] ?? ''),
            "scoreHome" => isset($data['scoreHome']) && $data['scoreHome'] !== '' ? intval($data['scoreHome']) : null,
            "scoreAway" => isset($data['scoreAway']) && $data['scoreAway'] !== '' ? intval($data['scoreAway']) : null,
            "servers" => (isset($data['servers']) && is_array($data['servers'])) ? $data['servers'] : []
        ];

        if (empty($newHero['servers']) && !empty($data['streamUrl'])) {
            $newHero['servers'][] = [
                'id' => 's1',
                'name' => 'سيرفر البث الرئيسي HD',
                'url' => trim($data['streamUrl']),
                'quality' => '1080p FHD'
            ];
        }

        $exists = false;
        foreach ($banners as &$h) {
            if ($h['id'] === $hId) {
                $h = $newHero;
                $exists = true;
                break;
            }
        }
        if (!$exists) {
            $banners[] = $newHero;
        }
        Storage::save('hero_banners.json', $banners);
        return ["status" => true, "message" => "تم حفظ بوستر الهيرو بنجاح"];
    }

    public static function deleteHeroBanner($hId) {
        $banners = Storage::get('hero_banners.json', []);
        $filtered = array_values(array_filter($banners, function($b) use ($hId) {
            return $b['id'] !== $hId;
        }));
        Storage::save('hero_banners.json', $filtered);
        return ["status" => true, "message" => "تم حذف بوستر الهيرو"];
    }

    public static function getFeaturedWeekly($encryptForApi = false) {
        $featured = Storage::get('featured_weekly.json', []);
        if ($encryptForApi && Security::isEncryptionEnabled()) {
            $key = Security::getMasterKey();
            foreach ($featured as &$fw) {
                if (!empty($fw['servers'])) {
                    foreach ($fw['servers'] as &$s) {
                        if (!empty($s['url'])) $s['url'] = Security::encrypt($s['url'], $key);
                    }
                }
                if (!empty($fw['streamUrl'])) {
                    $fw['streamUrl'] = Security::encrypt($fw['streamUrl'], $key);
                }
            }
        }
        return $featured;
    }

    public static function saveFeaturedWeekly($data) {
        $featured = Storage::get('featured_weekly.json', []);
        $fId = trim($data['id'] ?? ('feat_' . time()));
        $newFeat = [
            "id" => $fId,
            "title" => trim($data['title'] ?? 'مباراة الأسبوع'),
            "tournament" => trim($data['tournament'] ?? 'الدوري الإسباني'),
            "tournamentLogo" => trim($data['tournamentLogo'] ?? ''),
            "homeTeam" => trim($data['homeTeam'] ?? 'الفريق 1'),
            "homeLogo" => trim($data['homeLogo'] ?? ''),
            "awayTeam" => trim($data['awayTeam'] ?? 'الفريق 2'),
            "awayLogo" => trim($data['awayLogo'] ?? ''),
            "kickoffTime" => trim($data['kickoffTime'] ?? '21:00'),
            "kickoffDate" => trim($data['kickoffDate'] ?? 'السبت'),
            "channelName" => trim($data['channelName'] ?? 'beIN SPORTS 1 HD'),
            "commentator" => trim($data['commentator'] ?? 'تعليق عربي'),
            "stadium" => trim($data['stadium'] ?? 'الملعب الرئيسي'),
            "isLive" => !empty($data['isLive']),
            "liveMinute" => trim($data['liveMinute'] ?? ''),
            "scoreHome" => isset($data['scoreHome']) && $data['scoreHome'] !== '' ? intval($data['scoreHome']) : null,
            "scoreAway" => isset($data['scoreAway']) && $data['scoreAway'] !== '' ? intval($data['scoreAway']) : null,
            "servers" => (isset($data['servers']) && is_array($data['servers'])) ? $data['servers'] : []
        ];

        if (empty($newFeat['servers']) && !empty($data['streamUrl'])) {
            $newFeat['servers'][] = [
                'id' => 's1',
                'name' => 'سيرفر البث HD',
                'url' => trim($data['streamUrl']),
                'quality' => 'HD'
            ];
        }

        $exists = false;
        foreach ($featured as &$f) {
            if ($f['id'] === $fId) {
                $f = $newFeat;
                $exists = true;
                break;
            }
        }
        if (!$exists) {
            $featured[] = $newFeat;
        }
        Storage::save('featured_weekly.json', $featured);
        return ["status" => true, "message" => "تم حفظ مباراة الأسبوع بنجاح"];
    }

    public static function deleteFeaturedWeekly($fId) {
        $featured = Storage::get('featured_weekly.json', []);
        $filtered = array_values(array_filter($featured, function($f) use ($fId) {
            return $f['id'] !== $fId;
        }));
        Storage::save('featured_weekly.json', $filtered);
        return ["status" => true, "message" => "تم حذف مباراة الأسبوع"];
    }
}
