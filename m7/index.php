<?php
/**
 * M7 Web Admin Control Panel - Main Entry Point
 */

if (!defined('M7_ROOT')) {
    define('M7_ROOT', __DIR__);
}
require_once __DIR__ . '/config/config.php';

// Date Routing
$todayDate = date('Y-m-d');
$dateParam = isset($_GET['date']) ? trim($_GET['date']) : '';
if ($dateParam === 'yesterday') {
    $targetDate = date('Y-m-d', strtotime('-1 day'));
} elseif ($dateParam === 'tomorrow') {
    $targetDate = date('Y-m-d', strtotime('+1 day'));
} elseif (preg_match('/^\d{4}-\d{2}-\d{2}$/', $dateParam)) {
    $targetDate = $dateParam;
} else {
    $targetDate = $todayDate;
}

$forceRefresh = isset($_GET['refresh']) && ($_GET['refresh'] == '1' || $_GET['refresh'] == 'true');

// Load Data for Dashboard (without encrypting in admin view so admin sees real URLs)
$enrichedMatches = MatchesService::getEnrichedMatches($targetDate, $forceRefresh, false);
$currentChannels = ChannelsService::getAll(false);
$currentHero = HeroService::getHeroBanners(false);
$currentFeatured = HeroService::getFeaturedWeekly(false);
$currentSlider = Storage::get('slider.json', []);
$currentAnnounce = AnnouncementService::get();
$activeSessions = AuthService::getActiveSessions();
$registeredDevices = Storage::get('devices.json', []);
$currentSettings = Storage::get('settings.json', [
    'master_key' => DEFAULT_MASTER_KEY,
    'encrypt_streams' => true,
    'access_policy' => 'open'
]);

// Render Views
require_once __DIR__ . '/views/header.php';
require_once __DIR__ . '/views/tab_matches.php';
require_once __DIR__ . '/views/tab_hero.php';
require_once __DIR__ . '/views/tab_channels.php';
require_once __DIR__ . '/views/tab_slider.php';
require_once __DIR__ . '/views/tab_devices.php';
require_once __DIR__ . '/views/tab_football_api.php';
require_once __DIR__ . '/views/tab_logos.php';
require_once __DIR__ . '/views/tab_notifications.php';
require_once __DIR__ . '/views/tab_security.php';
require_once __DIR__ . '/views/tab_player.php';
require_once __DIR__ . '/views/footer.php';
