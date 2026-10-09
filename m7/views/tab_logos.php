<!-- 10. TAB: LOGOS & CRESTS LIBRARY (مكتبة الشعارات الجاهزة عالية الدقة) -->
<div id="tab-logos" class="tab-content">
    <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
            <h2 style="font-size: 18px; color: var(--gold); font-weight: 900;">🛡️ مكتبة الشعارات الرياضية الرسمية فائقة الدقة (HD Crests & Logos)</h2>
            <p style="font-size: 12.5px; color: var(--text-muted);">شعارات رسمية جاهزة لأشهر أندية أوروبا والعالم، أندية السعودية والخليج، المنتخبات الوطنية، الدوريات والبطولات، وقنوات beIN و SSC</p>
        </div>
        <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <input type="text" id="logoSearchInput" placeholder="🔍 ابحث عن فريق، منتخب، بطولة، أو قناة..." class="form-control" style="width: 280px;" onkeyup="filterLogosLibrary()">
            <button class="btn btn-outline" onclick="selectLogoCategory('all')">الكل</button>
            <button class="btn btn-outline" onclick="selectLogoCategory('clubs')">الأندية</button>
            <button class="btn btn-outline" onclick="selectLogoCategory('national_teams')">المنتخبات</button>
            <button class="btn btn-outline" onclick="selectLogoCategory('tournaments')">البطولات</button>
            <button class="btn btn-outline" onclick="selectLogoCategory('channels')">القنوات</button>
        </div>
    </div>

    <!-- Logos Grid Container -->
    <div id="logosGrid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 14px;">
        <!-- Populated via JavaScript -->
    </div>
</div>

<!-- Quick Logo Picker Modal for Inputs -->
<div class="modal" id="logoPickerModal">
    <div class="modal-content" style="max-width: 750px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
            <h3 style="color: var(--gold); font-size: 16px; font-weight: 900;">🛡️ اختر شعاراً من المكتبة الجاهزة</h3>
            <button class="btn btn-outline" onclick="closeModal('logoPickerModal')">✖</button>
        </div>
        <div style="display: flex; gap: 8px; margin-bottom: 14px;">
            <input type="text" id="pickerSearchInput" placeholder="بحث سريع عن الشعار (مثال: ريال مدريد، الهلال، مصر)..." class="form-control" onkeyup="filterPickerLogos()">
        </div>
        <div id="pickerGrid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); gap: 10px; max-height: 420px; overflow-y: auto;">
            <!-- Rendered by JS -->
        </div>
    </div>
</div>
