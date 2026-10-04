<?php
/**
 * M7 Live TV Channels Service
 */

class ChannelsService {

    public static function getAll($encryptForApi = false) {
        $channels = Storage::get('channels.json', []);
        if ($encryptForApi && Security::isEncryptionEnabled()) {
            $key = Security::getMasterKey();
            foreach ($channels as &$ch) {
                if (!empty($ch['playUrl'])) {
                    $ch['playUrl'] = Security::encrypt($ch['playUrl'], $key);
                }
            }
        }
        return $channels;
    }

    public static function save($data) {
        $channels = Storage::get('channels.json', []);
        $chId = trim($data['streamId'] ?? ('ch_' . time()));
        $chName = trim($data['name'] ?? '');
        $chPlayUrl = trim($data['playUrl'] ?? '');
        $chIcon = trim($data['iconUrl'] ?? '');
        $chCategory = trim($data['categoryId'] ?? 'قنوات beIN SPORTS');
        $chQuality = trim($data['quality'] ?? '1080p FHD');

        if (empty($chName) || empty($chPlayUrl)) {
            return ["status" => false, "message" => "يرجى إدخال اسم القناة ورابط البث"];
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

        Storage::save('channels.json', $channels);
        return ["status" => true, "message" => "تم حفظ القناة بنجاح"];
    }

    public static function delete($chId) {
        $channels = Storage::get('channels.json', []);
        $filtered = array_values(array_filter($channels, function($c) use ($chId) {
            return $c['streamId'] !== $chId;
        }));
        Storage::save('channels.json', $filtered);
        return ["status" => true, "message" => "تم حذف القناة بنجاح"];
    }
}
