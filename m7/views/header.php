<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>لوحة تحكم HERO Cast & TOD M7 السحابية</title>
    <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
    <style>
        :root {
            --bg-dark: #07090e;
            --card-bg: #101422;
            --card-border: #1e263d;
            --gold: #ffb800;
            --gold-hover: #e0a300;
            --blue: #0a84ff;
            --green: #30d158;
            --red: #ff453a;
            --text-main: #ffffff;
            --text-muted: #8e9bae;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Cairo', sans-serif; }
        body { background-color: var(--bg-dark); color: var(--text-main); min-height: 100vh; padding-bottom: 60px; }
        .header-bar {
            background: linear-gradient(180deg, #161b2e 0%, #0d101a 100%);
            border-bottom: 1px solid var(--card-border);
            padding: 16px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        .header-title { display: flex; align-items: center; gap: 12px; }
        .header-title h1 { font-size: 20px; font-weight: 900; color: var(--gold); }
        .badge { background: rgba(255, 184, 0, 0.15); color: var(--gold); padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; border: 1px solid rgba(255, 184, 0, 0.3); }
        .btn {
            background: var(--gold);
            color: #000;
            font-weight: 700;
            border: none;
            padding: 8px 16px;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 13px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
        }
        .btn:hover { background: var(--gold-hover); transform: translateY(-1px); }
        .btn-blue { background: var(--blue); color: #fff; }
        .btn-blue:hover { background: #0070e0; }
        .btn-green { background: var(--green); color: #000; }
        .btn-red { background: var(--red); color: #fff; }
        .btn-outline { background: transparent; border: 1px solid var(--card-border); color: #fff; }
        .btn-outline:hover { background: rgba(255,255,255,0.05); }
        .container { max-width: 1380px; margin: 20px auto; padding: 0 16px; }
        .main-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
        .main-tab-btn {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--card-border);
            color: var(--text-muted);
            padding: 9px 16px;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
        }
        .main-tab-btn.active { background: var(--gold); color: #000; }
        .card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 14px; padding: 20px; margin-bottom: 20px; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        .form-control { width: 100%; background: #0b0e17; border: 1px solid var(--card-border); border-radius: 8px; padding: 10px 14px; color: #fff; font-size: 13px; outline: none; }
        .form-control:focus { border-color: var(--gold); }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; font-size: 12.5px; color: var(--text-muted); margin-bottom: 6px; font-weight: 600; }
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.8); z-index: 1000; align-items: center; justify-content: center; padding: 16px; }
        .modal.show { display: flex; }
        .modal-content { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 16px; padding: 24px; width: 100%; max-width: 650px; max-height: 90vh; overflow-y: auto; }
        .match-row { display: grid; grid-template-columns: 100px 1fr 120px 1fr 180px; align-items: center; gap: 12px; padding: 12px; border-bottom: 1px solid rgba(255,255,255,0.05); }
        .match-row:hover { background: rgba(255,255,255,0.02); }
        @keyframes pulse {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(48, 209, 88, 0.7); }
            70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(48, 209, 88, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(48, 209, 88, 0); }
        }
        @media(max-width: 900px) {
            .match-row { grid-template-columns: 1fr; text-align: center; gap: 8px; }
        }
    </style>
</head>
<body>

<header class="header-bar">
    <div class="header-title">
        <h1>⚽ لوحة تحكم السيرفر (M7)</h1>
        <span class="badge">v<?= M7_VERSION ?></span>
        <span class="badge" style="background: rgba(48,209,88,0.15); color: var(--green); border-color: var(--green);">🔒 AES-256 مشفر</span>
    </div>

    <div class="main-tabs">
        <button class="main-tab-btn active" onclick="switchMainTab('matches', this)">⚽ المباريات والبث</button>
        <button class="main-tab-btn" onclick="switchMainTab('hero', this)">🌟 بوستر الهيرو والواجهة</button>
        <button class="main-tab-btn" onclick="switchMainTab('featured', this)">⭐ أفضل مباريات الأسبوع</button>
        <button class="main-tab-btn" onclick="switchMainTab('channels', this)">📺 القنوات المباشرة</button>
        <button class="main-tab-btn" onclick="switchMainTab('slider', this)">🖼️ السلايدر</button>
        <button class="main-tab-btn" onclick="switchMainTab('devices', this)">👥 المتصلون والأجهزة (<?= count($activeSessions) ?>)</button>
        <button class="main-tab-btn" onclick="switchMainTab('notifications', this)">🔔 الإشعارات</button>
        <button class="main-tab-btn" onclick="switchMainTab('security', this)">🔒 أمان وتشفير البيانات</button>
        <button class="main-tab-btn" onclick="switchMainTab('player', this)">▶️ مشغل التجربة</button>
    </div>

    <div>
        <a href="?date=<?= $targetDate ?>&refresh=1" class="btn btn-blue">🔄 تحديث الكاش الفوري</a>
    </div>
</header>

<div class="container">
