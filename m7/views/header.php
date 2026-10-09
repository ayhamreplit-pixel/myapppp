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
            background: linear-gradient(180deg, #131726 0%, #0a0d16 100%);
            border-bottom: 1px solid rgba(255, 255, 255, 0.08);
            padding: 16px 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            position: sticky;
            top: 0;
            z-index: 100;
            box-shadow: 0 4px 20px rgba(0,0,0,0.4);
        }
        .header-title { display: flex; align-items: center; gap: 14px; }
        .header-title h1 { font-size: 21px; font-weight: 900; color: var(--gold); letter-spacing: -0.5px; }
        .badge { background: rgba(255, 184, 0, 0.15); color: var(--gold); padding: 5px 12px; border-radius: 20px; font-size: 11.5px; font-weight: 700; border: 1px solid rgba(255, 184, 0, 0.3); }
        .btn {
            background: var(--gold);
            color: #000;
            font-weight: 700;
            border: none;
            padding: 9px 18px;
            border-radius: 9px;
            cursor: pointer;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            font-size: 13px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            text-decoration: none;
            box-shadow: 0 2px 6px rgba(0,0,0,0.2);
        }
        .btn:hover { background: var(--gold-hover); transform: translateY(-1px); box-shadow: 0 4px 12px rgba(255,184,0,0.3); }
        .btn-blue { background: var(--blue); color: #fff; }
        .btn-blue:hover { background: #0070e0; box-shadow: 0 4px 12px rgba(10,132,255,0.3); }
        .btn-green { background: var(--green); color: #000; }
        .btn-red { background: var(--red); color: #fff; }
        .btn-outline { background: rgba(255,255,255,0.04); border: 1px solid var(--card-border); color: #e1e7f0; }
        .btn-outline:hover { background: rgba(255,255,255,0.08); border-color: rgba(255,255,255,0.2); }
        .container { max-width: 1440px; margin: 24px auto; padding: 0 20px; }
        .main-tabs-wrapper {
            background: #0d111d;
            border: 1px solid var(--card-border);
            border-radius: 14px;
            padding: 8px 12px;
            margin-bottom: 24px;
            overflow-x: auto;
            white-space: nowrap;
        }
        .main-tabs { display: flex; gap: 6px; }
        .main-tab-btn {
            background: transparent;
            border: 1px solid transparent;
            color: var(--text-muted);
            padding: 10px 18px;
            border-radius: 10px;
            font-size: 13.5px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        .main-tab-btn:hover { color: #fff; background: rgba(255,255,255,0.04); }
        .main-tab-btn.active { background: var(--gold); color: #000; box-shadow: 0 2px 10px rgba(255,184,0,0.3); }
        .card {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 22px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.2);
        }
        .tab-content { display: none; }
        .tab-content.active { display: block; animation: fadeIn 0.25s ease-in-out; }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(4px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .form-control { width: 100%; background: #080b13; border: 1px solid var(--card-border); border-radius: 9px; padding: 11px 15px; color: #fff; font-size: 13.5px; outline: none; transition: border-color 0.2s; }
        .form-control:focus { border-color: var(--gold); box-shadow: 0 0 0 3px rgba(255,184,0,0.15); }
        .form-group { margin-bottom: 16px; }
        .form-group label { display: block; font-size: 13px; color: #a4b3cb; margin-bottom: 7px; font-weight: 700; }
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.85); backdrop-filter: blur(4px); z-index: 1000; align-items: center; justify-content: center; padding: 16px; }
        .modal.show { display: flex; }
        .modal-content { background: #111524; border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 26px; width: 100%; max-width: 680px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 50px rgba(0,0,0,0.6); }
        .match-row {
            display: grid;
            grid-template-columns: 140px 1fr 140px 1fr 220px;
            align-items: center;
            gap: 16px;
            padding: 16px 20px;
            border-bottom: 1px solid rgba(255,255,255,0.05);
            transition: background 0.2s;
        }
        .match-row:hover { background: rgba(255,255,255,0.03); }
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
        <h1>⚽ لوحة تحكم سيرفر TOD & HERO M7</h1>
        <span class="badge">v<?= M7_VERSION ?></span>
        <span class="badge" style="background: rgba(48,209,88,0.15); color: var(--green); border-color: var(--green);">🔒 AES-256 مشفر</span>
    </div>

    <div style="display: flex; gap: 10px; align-items: center;">
        <span class="badge" style="background: rgba(10,132,255,0.15); color: var(--blue); border-color: var(--blue);">📱 <?= count($activeSessions) ?> متصل</span>
        <a href="?date=<?= $targetDate ?>&refresh=1" class="btn btn-blue">🔄 تحديث الكاش الفوري</a>
    </div>
</header>

<div class="container">

<div class="main-tabs-wrapper">
    <div class="main-tabs">
        <button class="main-tab-btn active" onclick="switchMainTab('matches', this)">⚽ المباريات والبث</button>
        <button class="main-tab-btn" onclick="switchMainTab('hero', this)">🌟 بوستر الهيرو والواجهة</button>
        <button class="main-tab-btn" onclick="switchMainTab('featured', this)">⭐ أفضل مباريات الأسبوع</button>
        <button class="main-tab-btn" onclick="switchMainTab('channels', this)">📺 القنوات المباشرة</button>
        <button class="main-tab-btn" onclick="switchMainTab('slider', this)">🖼️ السلايدر</button>
        <button class="main-tab-btn" onclick="switchMainTab('devices', this)">👥 المتصلون والأجهزة (<?= count($activeSessions) ?>)</button>
        <button class="main-tab-btn" onclick="switchMainTab('football-api', this)">🌐 مستكشف الـ API الرياضي</button>
        <button class="main-tab-btn" onclick="switchMainTab('logos', this)">🛡️ مكتبة الشعارات</button>
        <button class="main-tab-btn" onclick="switchMainTab('notifications', this)">🔔 الإشعارات</button>
        <button class="main-tab-btn" onclick="switchMainTab('security', this)">🔒 أمان وتشفير البيانات</button>
        <button class="main-tab-btn" onclick="switchMainTab('player', this)">▶️ مشغل التجربة</button>
    </div>
</div>
