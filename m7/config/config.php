<?php
/**
 * M7 Streaming Server - Central Configuration & Security Setup
 */

if (!defined('M7_ROOT')) {
    define('M7_ROOT', dirname(__DIR__));
}

// Timezone & Error Handling
date_default_timezone_set('Asia/Riyadh');
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
ini_set('display_errors', '0');

// Paths
define('M7_DATA_DIR', M7_ROOT . '/data');
define('M7_CACHE_DIR', sys_get_temp_dir() . '/m7_kora_cache');

if (!is_dir(M7_DATA_DIR)) {
    @mkdir(M7_DATA_DIR, 0777, true);
}
if (!is_dir(M7_CACHE_DIR)) {
    @mkdir(M7_CACHE_DIR, 0777, true);
}

// Security Default Master Key
define('DEFAULT_MASTER_KEY', 'TOD_SPORTS_STREAM_SECURE_KEY_2026_PRO');
define('M7_VERSION', '3.5.0-ENTERPRISE');

// Auto Loader for Core Classes
spl_autoload_register(function ($class) {
    $file = M7_ROOT . '/core/' . $class . '.php';
    if (file_exists($file)) {
        require_once $file;
    }
});
