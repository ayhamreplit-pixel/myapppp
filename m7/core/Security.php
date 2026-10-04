<?php
/**
 * M7 Enterprise Security & Cryptography Layer
 * Full AES-256-CBC Encryption/Decryption compatible with StreamSecurityManager
 */

class Security {
    
    public static function getMasterKey() {
        $settings = Storage::get('settings.json', [
            'master_key' => DEFAULT_MASTER_KEY,
            'encrypt_streams' => true
        ]);
        return !empty($settings['master_key']) ? $settings['master_key'] : DEFAULT_MASTER_KEY;
    }

    public static function isEncryptionEnabled() {
        $settings = Storage::get('settings.json', ['encrypt_streams' => true]);
        return !isset($settings['encrypt_streams']) || $settings['encrypt_streams'] !== false;
    }

    /**
     * Encrypts plaintext string using AES-256-CBC
     */
    public static function encrypt($plaintext, $secretKey = null) {
        $cleanText = trim($plaintext ?? '');
        if (empty($cleanText)) return '';
        if (strpos($cleanText, 'enc:') === 0 || strpos($cleanText, 'aes:') === 0 || strpos($cleanText, 'm7enc:') === 0) {
            return $cleanText;
        }

        $keyStr = $secretKey ?: self::getMasterKey();
        try {
            $key = hash('sha256', $keyStr, true);
            $iv = md5($keyStr . '_iv_salt', true);
            $encrypted = openssl_encrypt($cleanText, 'AES-256-CBC', $key, OPENSSL_RAW_DATA, $iv);
            if ($encrypted === false) return $cleanText;
            return 'enc:' . base64_encode($encrypted);
        } catch (Exception $e) {
            return $cleanText;
        }
    }

    /**
     * Decrypts AES-256-CBC ciphertext
     */
    public static function decrypt($ciphertext, $secretKey = null) {
        $cleanCipher = trim($ciphertext ?? '');
        if (empty($cleanCipher)) return '';

        if (strpos($cleanCipher, 'enc:') === 0) {
            $cleanCipher = substr($cleanCipher, 4);
        } elseif (strpos($cleanCipher, 'aes:') === 0) {
            $cleanCipher = substr($cleanCipher, 4);
        } elseif (strpos($cleanCipher, 'm7enc:') === 0) {
            $cleanCipher = substr($cleanCipher, 6);
        } elseif (strpos($cleanCipher, 'm7:') === 0) {
            $cleanCipher = substr($cleanCipher, 3);
        }

        if (strpos($cleanCipher, 'http://') === 0 || strpos($cleanCipher, 'https://') === 0 || strpos($cleanCipher, 'rtmp://') === 0) {
            return $cleanCipher;
        }

        $keyStr = $secretKey ?: self::getMasterKey();
        try {
            $key = hash('sha256', $keyStr, true);
            $iv = md5($keyStr . '_iv_salt', true);
            $decoded = base64_decode($cleanCipher);
            if ($decoded === false) return $ciphertext;
            $decrypted = openssl_decrypt($decoded, 'AES-256-CBC', $key, OPENSSL_RAW_DATA, $iv);
            return ($decrypted !== false) ? trim($decrypted) : $ciphertext;
        } catch (Exception $e) {
            return $ciphertext;
        }
    }

    public static function setCorsHeaders() {
        header('Access-Control-Allow-Origin: *');
        header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
        header('Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization, X-Requested-With, X-Secret-Key');
        if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
            http_response_code(200);
            exit;
        }
    }

    public static function generateCsrfToken() {
        if (session_status() === PHP_SESSION_NONE) {
            @session_start();
        }
        if (empty($_SESSION['m7_csrf_token'])) {
            $_SESSION['m7_csrf_token'] = bin2hex(random_bytes(24));
        }
        return $_SESSION['m7_csrf_token'];
    }

    public static function verifyCsrfToken($token) {
        if (session_status() === PHP_SESSION_NONE) {
            @session_start();
        }
        return !empty($token) && !empty($_SESSION['m7_csrf_token']) && hash_equals($_SESSION['m7_csrf_token'], $token);
    }
}
