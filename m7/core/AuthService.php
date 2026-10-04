<?php
/**
 * M7 Enterprise Device Authentication, Sessions & Security Handshake
 */

class AuthService {

    /**
     * Authenticates and registers an Android device connection
     */
    public static function authenticateDevice($data) {
        $deviceId = trim($data['deviceId'] ?? $data['device_id'] ?? '');
        $deviceName = trim($data['deviceName'] ?? $data['device_name'] ?? 'Android Device');
        $appVersion = trim($data['appVersion'] ?? $data['app_version'] ?? '3.5.0');
        $androidVersion = trim($data['androidVersion'] ?? $data['android_version'] ?? '14');
        $profileName = trim($data['profileName'] ?? $data['profile_name'] ?? 'Default');
        $ip = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';

        if (empty($deviceId)) {
            return ["status" => false, "message" => "معرف الجهاز مفقود (Device ID required)"];
        }

        $devices = Storage::get('devices.json', []);
        $settings = Storage::get('settings.json', [
            'access_policy' => 'open', // 'open', 'whitelist', 'require_code'
            'master_key' => DEFAULT_MASTER_KEY
        ]);

        // 1. Check if device is banned
        if (isset($devices[$deviceId]) && !empty($devices[$deviceId]['banned'])) {
            return [
                "status" => false,
                "banned" => true,
                "message" => "تم حظر هذا الجهاز من الاتصال بالسيرفر. يرجى التواصل مع الإدارة."
            ];
        }

        // 2. Check access policy
        $policy = $settings['access_policy'] ?? 'open';
        $isApproved = true;
        if ($policy === 'whitelist') {
            $isApproved = isset($devices[$deviceId]) && !empty($devices[$deviceId]['approved']);
            if (!$isApproved) {
                // Register as pending
                if (!isset($devices[$deviceId])) {
                    $devices[$deviceId] = [
                        'device_id' => $deviceId,
                        'device_name' => $deviceName,
                        'app_version' => $appVersion,
                        'android_version' => $androidVersion,
                        'profile_name' => $profileName,
                        'ip' => $ip,
                        'registered_at' => date('Y-m-d H:i:s'),
                        'last_active' => date('Y-m-d H:i:s'),
                        'approved' => false,
                        'banned' => false
                    ];
                    Storage::save('devices.json', $devices);
                }
                return [
                    "status" => false,
                    "pending" => true,
                    "message" => "جهازك قيد انتظار الموافقة من لوحة التحكم."
                ];
            }
        }

        // 3. Update device registration
        $devices[$deviceId] = [
            'device_id' => $deviceId,
            'device_name' => $deviceName,
            'app_version' => $appVersion,
            'android_version' => $androidVersion,
            'profile_name' => $profileName,
            'ip' => $ip,
            'registered_at' => $devices[$deviceId]['registered_at'] ?? date('Y-m-d H:i:s'),
            'last_active' => date('Y-m-d H:i:s'),
            'approved' => true,
            'banned' => false
        ];
        Storage::save('devices.json', $devices);

        // 4. Generate dynamic Session Token
        $masterKey = Security::getMasterKey();
        $tokenPayload = json_encode([
            'id' => $deviceId,
            'name' => $deviceName,
            'time' => time(),
            'rand' => bin2hex(random_bytes(8))
        ]);
        $sessionToken = Security::encrypt($tokenPayload, $masterKey);

        // 5. Update session in active sessions
        self::updateHeartbeat($deviceId, $deviceName, $profileName, "الاتصال بالسيرفر", $ip);

        return [
            "status" => true,
            "session_token" => $sessionToken,
            "server_time" => time(),
            "master_key_alias" => "M7_MASTER_2026",
            "message" => "تم توثيق الجهاز بنجاح 🔒"
        ];
    }

    /**
     * Heartbeat tracker for live active viewers
     */
    public static function updateHeartbeat($deviceId, $deviceName, $profileName, $activeStreamTitle = "", $ip = null) {
        if (empty($deviceId)) return false;
        $ip = $ip ?: ($_SERVER['REMOTE_ADDR'] ?? '127.0.0.1');

        $sessions = Storage::get('sessions.json', []);
        $now = time();

        $sessions[$deviceId] = [
            'device_id' => $deviceId,
            'device_name' => $deviceName ?: 'Android Device',
            'profile_name' => $profileName ?: 'User',
            'active_stream' => $activeStreamTitle ?: 'تصفح القنوات والمباريات',
            'ip' => $ip,
            'last_ping' => $now,
            'last_ping_formatted' => date('Y-m-d H:i:s')
        ];

        // Clean up sessions older than 90 seconds
        foreach ($sessions as $dId => $sess) {
            if (($now - ($sess['last_ping'] ?? 0)) > 90) {
                unset($sessions[$dId]);
            }
        }

        Storage::save('sessions.json', $sessions);
        return true;
    }

    /**
     * Returns currently active viewers (connected in last 60 seconds)
     */
    public static function getActiveSessions() {
        $sessions = Storage::get('sessions.json', []);
        $now = time();
        $active = [];
        foreach ($sessions as $dId => $s) {
            if (($now - ($s['last_ping'] ?? 0)) <= 60) {
                $active[] = $s;
            }
        }
        return $active;
    }

    /**
     * Device Ban / Unban
     */
    public static function toggleDeviceBan($deviceId, $banState = true) {
        $devices = Storage::get('devices.json', []);
        if (!isset($devices[$deviceId])) {
            $devices[$deviceId] = ['device_id' => $deviceId, 'registered_at' => date('Y-m-d H:i:s')];
        }
        $devices[$deviceId]['banned'] = (bool)$banState;
        Storage::save('devices.json', $devices);

        // If banning, immediately kick from active sessions
        if ($banState) {
            $sessions = Storage::get('sessions.json', []);
            if (isset($sessions[$deviceId])) {
                unset($sessions[$deviceId]);
                Storage::save('sessions.json', $sessions);
            }
        }

        return ["status" => true, "message" => $banState ? "تم حظر الجهاز بنجاح 🚫" : "تم إلغاء حظر الجهاز بنجاح ✅"];
    }

    /**
     * Approve Device (Whitelist)
     */
    public static function approveDevice($deviceId) {
        $devices = Storage::get('devices.json', []);
        if (isset($devices[$deviceId])) {
            $devices[$deviceId]['approved'] = true;
            Storage::save('devices.json', $devices);
            return ["status" => true, "message" => "تم اعتماد وتفعيل الجهاز بنجاح ✅"];
        }
        return ["status" => false, "message" => "الجهاز غير موجود"];
    }

    /**
     * Delete Device
     */
    public static function deleteDevice($deviceId) {
        $devices = Storage::get('devices.json', []);
        if (isset($devices[$deviceId])) {
            unset($devices[$deviceId]);
            Storage::save('devices.json', $devices);
        }
        $sessions = Storage::get('sessions.json', []);
        if (isset($sessions[$deviceId])) {
            unset($sessions[$deviceId]);
            Storage::save('sessions.json', $sessions);
        }
        return ["status" => true, "message" => "تم حذف الجهاز"];
    }
}
