<?php
/**
 * M7 Atomic JSON Storage Layer
 */

class Storage {

    public static function getPath($filename) {
        return M7_DATA_DIR . '/' . ltrim($filename, '/');
    }

    public static function get($filename, $default = []) {
        $path = self::getPath($filename);
        if (!file_exists($path)) {
            return $default;
        }
        $content = @file_get_contents($path);
        if ($content === false || trim($content) === '') {
            return $default;
        }
        $decoded = json_decode($content, true);
        return is_array($decoded) ? $decoded : $default;
    }

    public static function save($filename, $data) {
        $path = self::getPath($filename);
        $json = json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        return @file_put_contents($path, $json, LOCK_EX) !== false;
    }

    public static function exists($filename) {
        return file_exists(self::getPath($filename));
    }

    public static function delete($filename) {
        $path = self::getPath($filename);
        if (file_exists($path)) {
            return @unlink($path);
        }
        return true;
    }
}
