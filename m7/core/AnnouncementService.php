<?php
/**
 * M7 Push Notifications & Announcements Service
 */

class AnnouncementService {

    public static function get() {
        return Storage::get('announcement.json', [
            "enabled" => false,
            "id" => "notif_0",
            "title" => "",
            "message" => "",
            "target_type" => "none",
            "target_id" => ""
        ]);
    }

    public static function save($data) {
        $notif = [
            "enabled" => isset($data['enabled']) ? (bool)$data['enabled'] : true,
            "id" => "notif_" . time(),
            "title" => trim($data['title'] ?? 'تنبيه مباشر'),
            "message" => trim($data['message'] ?? ''),
            "target_type" => trim($data['target_type'] ?? 'none'),
            "target_id" => trim($data['target_id'] ?? '')
        ];
        Storage::save('announcement.json', $notif);
        return ["status" => true, "message" => "تم نشر الإشعار لجميع مستخدمي التطبيق بنجاح", "data" => $notif];
    }

    public static function clear() {
        $notif = [
            "enabled" => false,
            "id" => "notif_" . time(),
            "title" => "",
            "message" => "",
            "target_type" => "none",
            "target_id" => ""
        ];
        Storage::save('announcement.json', $notif);
        return ["status" => true, "message" => "تم تعطيل الإشعار"];
    }
}
