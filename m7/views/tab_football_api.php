<!-- TAB: FOOTBALL API & LIVE STATS (جلب التشكيلة والإحصائيات الحديثة) -->
<div id="tab-football-api" class="tab-content">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">🌐 مستكشف ومحرك الـ API الرياضي العالمي (ESPN Live Football Data)</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">جلب فوري لتفاصيل المباريات العالمية، التشكيلة الرسمية (Lineups)، الإحصائيات الكاملة (الاستحواذ، التسديدات، التمريرات)، والأحداث المباشرة بدون أي وسيط</p>
        </div>
        <div style="display: flex; gap: 8px;">
            <input type="text" id="apiSearchQuery" placeholder="ابحث عن مباراة أو فريق (مثال: Real Madrid, Arsenal)..." class="form-control" style="width: 280px;" onkeypress="if(event.key==='Enter') searchApiMatches()">
            <button class="btn btn-blue" onclick="searchApiMatches()">🔍 بحث في الـ API</button>
            <button class="btn btn-outline" onclick="loadTodayApiMatches()">📅 مباريات اليوم في الـ API</button>
        </div>
    </div>

    <!-- API Match Details Explorer Modal/Viewer -->
    <div id="apiDetailsCard" class="card" style="display: none; border-color: var(--gold);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3 id="apiDetailsTitle" style="color: var(--gold); font-size: 16px; font-weight: 900;">تفاصيل اللقاء والتشكيلة</h3>
            <div style="display: flex; gap: 8px;">
                <button class="btn btn-green" id="apiAddAsCustomMatchBtn">➕ استيراد وإضافة للمباريات يدوياً</button>
                <button class="btn btn-outline" onclick="document.getElementById('apiDetailsCard').style.display='none'">✖ إغلاق</button>
            </div>
        </div>
        <div id="apiDetailsContent">
            <!-- Populated via Javascript -->
        </div>
    </div>

    <!-- API Matches Result Table -->
    <div class="card" style="padding: 0; overflow: hidden;">
        <div style="padding: 12px 16px; background: rgba(255,255,255,0.02); border-bottom: 1px solid var(--card-border); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 13px; font-weight: 700; color: var(--gold);" id="apiResultsCount">نتائج الـ API المتوفرة</span>
            <span style="font-size: 12px; color: var(--text-muted);">يدعم جميع الدوريات الكبرى (إنجليزي، إسباني، أبطال أوروبا، دوري روشن، إيطالي، ألماني، فرنسي)</span>
        </div>
        <div id="apiMatchesList" style="padding: 20px; text-align: center; color: var(--text-muted);">
            <p>انقر على "مباريات اليوم في الـ API" أو أدخل اسم أي فريق أو بطولة لجلب الإحصائيات والتشكيلة مباشرة.</p>
        </div>
    </div>
</div>
