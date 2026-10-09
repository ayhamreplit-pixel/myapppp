<?php
/**
 * M7 Curated High-Definition Logos & Crests Library
 * Contains top clubs, national teams, channels, and tournaments with high-resolution vector/PNG logos.
 */

class LogosLibrary {

    public static function getAllLogos() {
        return [
            "clubs" => self::getClubs(),
            "national_teams" => self::getNationalTeams(),
            "tournaments" => self::getTournaments(),
            "channels" => self::getChannels()
        ];
    }

    public static function search($query, $category = 'all') {
        $q = mb_strtolower(trim($query), 'UTF-8');
        $all = self::getAllLogos();
        $results = [];

        foreach ($all as $cat => $items) {
            if ($category !== 'all' && $category !== $cat) continue;
            foreach ($items as $item) {
                $name = mb_strtolower($item['name'], 'UTF-8');
                $enName = mb_strtolower($item['en_name'] ?? '', 'UTF-8');
                $tags = mb_strtolower($item['tags'] ?? '', 'UTF-8');

                if (empty($q) || strpos($name, $q) !== false || strpos($enName, $q) !== false || strpos($tags, $q) !== false) {
                    $item['category'] = $cat;
                    $results[] = $item;
                }
            }
        }

        return $results;
    }

    public static function getClubs() {
        return [
            // الدوري الإسباني
            ["name" => "ريال مدريد", "en_name" => "Real Madrid", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Real_Madrid_CF.svg/512px-Real_Madrid_CF.svg.png", "tags" => "إسبانيا الملكي مدريد"],
            ["name" => "برشلونة", "en_name" => "FC Barcelona", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/512px-FC_Barcelona_%28crest%29.svg.png", "tags" => "إسبانيا برشا كاتالونيا"],
            ["name" => "أتلتيكو مدريد", "en_name" => "Atletico Madrid", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/f/f4/Atletico_Madrid_2017_logo.svg/512px-Atletico_Madrid_2017_logo.svg.png", "tags" => "إسبانيا الروخي بلانكوس"],
            ["name" => "إشبيلية", "en_name" => "Sevilla FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/Sevilla_FC_logo.svg/512px-Sevilla_FC_logo.svg.png", "tags" => "إسبانيا أندلس"],
            ["name" => "فالنسيا", "en_name" => "Valencia CF", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/c/ce/Valenciacf.svg/512px-Valenciacf.svg.png", "tags" => "إسبانيا الخفافيش"],
            ["name" => "ريال بيتيس", "en_name" => "Real Betis", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/1/13/Real_betis_logo.svg/512px-Real_betis_logo.svg.png", "tags" => "إسبانيا بيتيس"],
            ["name" => "فياريال", "en_name" => "Villarreal CF", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/7/70/Villarreal_CF_logo.svg/512px-Villarreal_CF_logo.svg.png", "tags" => "الغواصات الصفراء"],
            ["name" => "أتلتيك بيلباو", "en_name" => "Athletic Bilbao", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/9/98/Club_Athletic_Bilbao_logo.svg/512px-Club_Athletic_Bilbao_logo.svg.png", "tags" => "إسبانيا الباسك"],

            // الدوري الإنجليزي
            ["name" => "مانشستر سيتي", "en_name" => "Manchester City", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/e/eb/Manchester_City_FC_badge.svg/512px-Manchester_City_FC_badge.svg.png", "tags" => "إنجلترا سيتي السيتيزنز"],
            ["name" => "ليفربول", "en_name" => "Liverpool FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/0/0c/Liverpool_FC.svg/512px-Liverpool_FC.svg.png", "tags" => "إنجلترا الريدز محمد صلاح"],
            ["name" => "آرسنال", "en_name" => "Arsenal FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/5/53/Arsenal_FC.svg/512px-Arsenal_FC.svg.png", "tags" => "إنجلتra الجانرز المدفعجية"],
            ["name" => "مانشستر يونايتد", "en_name" => "Manchester United", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/7/7a/Manchester_United_FC_crest.svg/512px-Manchester_United_FC_crest.svg.png", "tags" => "إنجلترا الشياطين الحمر يونايتد"],
            ["name" => "تشيلسي", "en_name" => "Chelsea FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/c/cc/Chelsea_FC.svg/512px-Chelsea_FC.svg.png", "tags" => "إنجلترا البلوز"],
            ["name" => "توتنهام هوتسبير", "en_name" => "Tottenham Hotspur", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/b/b4/Tottenham_Hotspur.svg/512px-Tottenham_Hotspur.svg.png", "tags" => "إنجلترا السبيرز"],
            ["name" => "نيوكاسل يونايتد", "en_name" => "Newcastle United", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/Newcastle_United_Logo.svg/512px-Newcastle_United_Logo.svg.png", "tags" => "إنجلترا نيوكاسل الماكبايس"],
            ["name" => "أستون فيلا", "en_name" => "Aston Villa", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/f/f9/Aston_Villa_FC_crest_%282016%29.svg/512px-Aston_Villa_FC_crest_%282016%29.svg.png", "tags" => "إنجلترا فيلا"],

            // الدوري الإيطالي
            ["name" => "يوفنتوس", "en_name" => "Juventus FC", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Juventus_FC_2017_icon_%28black%29.svg/512px-Juventus_FC_2017_icon_%28black%29.svg.png", "tags" => "إيطاليا اليوفي السيدة العجوز"],
            ["name" => "إنتر ميلان", "en_name" => "Inter Milan", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/FC_Internazionale_Milano_2021.svg/512px-FC_Internazionale_Milano_2021.svg.png", "tags" => "إيطاليا النيراتزوري إنتر"],
            ["name" => "إيه سي ميلان", "en_name" => "AC Milan", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Logo_of_AC_Milan.svg/512px-Logo_of_AC_Milan.svg.png", "tags" => "إيطاليا الروسونيري ميلان"],
            ["name" => "نابولي", "en_name" => "SSC Napoli", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/SSC_Napoli_2024_%28deep_blue_navy%29.svg/512px-SSC_Napoli_2024_%28deep_blue_navy%29.svg.png", "tags" => "إيطاليا نابولي بارتينوبي"],
            ["name" => "روما", "en_name" => "AS Roma", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/f/f7/AS_Roma_logo_%282017%29.svg/512px-AS_Roma_logo_%282017%29.svg.png", "tags" => "إيطاليا الذئاب"],
            ["name" => "لاتسيو", "en_name" => "SS Lazio", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/c/ce/S.S._Lazio_badge.svg/512px-S.S._Lazio_badge.svg.png", "tags" => "إيطاليا النسور"],
            ["name" => "أتالانتا", "en_name" => "Atalanta BC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/6/66/AtalantaBC.svg/512px-AtalantaBC.svg.png", "tags" => "إيطاليا أتالانتا"],

            // الدوري الألماني
            ["name" => "بايرن ميونخ", "en_name" => "Bayern Munich", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/FC_Bayern_M%C3%BCnchen_logo_%282017%29.svg/512px-FC_Bayern_M%C3%BCnchen_logo_%282017%29.svg.png", "tags" => "ألمانيا البافاري ميونخ"],
            ["name" => "بوروسيا دورتموند", "en_name" => "Borussia Dortmund", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Borussia_Dortmund_logo.svg/512px-Borussia_Dortmund_logo.svg.png", "tags" => "ألمانيا دورتموند أسود الفيستفاليا"],
            ["name" => "باير ليفركوزن", "en_name" => "Bayer Leverkusen", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/5/59/Bayer_04_Leverkusen_logo.svg/512px-Bayer_04_Leverkusen_logo.svg.png", "tags" => "ألمانيا ليفركوزن بطل البوندسليغا"],
            ["name" => "آر بي لايبزيغ", "en_name" => "RB Leipzig", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/0/04/RB_Leipzig_2014_logo.svg/512px-RB_Leipzig_2014_logo.svg.png", "tags" => "ألمانيا لايبزيغ"],

            // الدوري الفرنسي وبقية أوروبا
            ["name" => "باريس سان جيرمان", "en_name" => "Paris Saint-Germain", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/a/a7/Paris_Saint-Germain_F.C..svg/512px-Paris_Saint-Germain_F.C..svg.png", "tags" => "فرنسا باريس بي إس جي"],
            ["name" => "أولمبيك مارسيليا", "en_name" => "Marseille", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/8/86/Olympique_Marseille_logo.svg/512px-Olympique_Marseille_logo.svg.png", "tags" => "فرنسا مارسيليا"],
            ["name" => "بنفيكا", "en_name" => "SL Benfica", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/a/a2/SL_Benfica_logo.svg/512px-SL_Benfica_logo.svg.png", "tags" => "البرتغال بنفيكا النسور"],
            ["name" => "بورتو", "en_name" => "FC Porto", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/f/f1/FC_Porto.svg/512px-FC_Porto.svg.png", "tags" => "البرتغال بورتو التنانين"],
            ["name" => "سبورتينغ لشبونة", "en_name" => "Sporting CP", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/e/e1/Sporting_Clube_de_Portugal_%28Logo%29.svg/512px-Sporting_Clube_de_Portugal_%28Logo%29.svg.png", "tags" => "البرتغال سبورتينغ"],

            // الأندية العربية والسعودية
            ["name" => "الهلال السعودي", "en_name" => "Al Hilal SFC", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Al_Hilal_SFC_Logo_%282022%29.svg/512px-Al_Hilal_SFC_Logo_%282022%29.svg.png", "tags" => "السعودية الزعيم الهلال دوري روشن"],
            ["name" => "النصر السعودي", "en_name" => "Al Nassr FC", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Al-Nassr_FC_logo.svg/512px-Al-Nassr_FC_logo.svg.png", "tags" => "السعودية العالمي النصر كريستيانو رونالدو"],
            ["name" => "الاتحاد السعودي", "en_name" => "Al Ittihad Club", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Al-Ittihad_Club_logo.svg/512px-Al-Ittihad_Club_logo.svg.png", "tags" => "السعودية العميد النمور كريم بنزيما"],
            ["name" => "الأهلي السعودي", "en_name" => "Al Ahli Saudi FC", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Al-Ahli_Saudi_FC_logo.svg/512px-Al-Ahli_Saudi_FC_logo.svg.png", "tags" => "السعودية الراقي الأهلي رياض محرز"],
            ["name" => "الشباب السعودي", "en_name" => "Al Shabab FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/0/09/Al-Shabab_FC_%28Riyadh%29_logo.svg/512px-Al-Shabab_FC_%28Riyadh%29_logo.svg.png", "tags" => "السعودية الليث الشباب"],
            ["name" => "الاتفاق السعودي", "en_name" => "Al Ettifaq FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/6/66/Al-Ettifaq_FC_logo.svg/512px-Al-Ettifaq_FC_logo.svg.png", "tags" => "السعودية الاتفاق فارس الدهناء"],

            // مصر وإفريقيا
            ["name" => "الأهلي المصري", "en_name" => "Al Ahly SC", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Al_Ahly_SC_logo.svg/512px-Al_Ahly_SC_logo.svg.png", "tags" => "مصر نادي القرن المارد الأحمر الأهلي"],
            ["name" => "الزمالك المصري", "en_name" => "Zamalek SC", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/ZamalekSC.png/512px-ZamalekSC.png", "tags" => "مصر الفارس الأبيض مدرسة الفن والهندسة"],
            ["name" => "بيراميدز", "en_name" => "Pyramids FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/3/30/Pyramids_FC_logo.png/512px-Pyramids_FC_logo.png", "tags" => "مصر بيراميدز"],
            ["name" => "الوداد البيضاوي", "en_name" => "Wydad AC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/c/cf/Wydad_Athletic_Club_logo.svg/512px-Wydad_Athletic_Club_logo.svg.png", "tags" => "المغرب وداد الأمة الحمراء"],
            ["name" => "الرجاء البيضاوي", "en_name" => "Raja CA", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/Raja_Club_Athletic_logo.svg/512px-Raja_Club_Athletic_logo.svg.png", "tags" => "المغرب الرجاء العالمي الخضراء"],
            ["name" => "الترجي الرياضي التونسي", "en_name" => "Esperance de Tunis", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Esperance_Sportive_de_Tunis.svg/512px-Esperance_Sportive_de_Tunis.svg.png", "tags" => "تونس المكشخة الترجي شيخ الأندية"],
            ["name" => "النجم الساحلي", "en_name" => "Etoile du Sahel", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/b/b3/%C3%89toile_du_Sahel.svg/512px-%C3%89toile_du_Sahel.svg.png", "tags" => "تونس ليتوال النجم"],
            ["name" => "النادي الإفريقي", "en_name" => "Club Africain", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/7/7b/Club_Africain_logo.svg/512px-Club_Africain_logo.svg.png", "tags" => "تونس الإفريقي"],
            ["name" => "مولودية الجزائر", "en_name" => "MC Alger", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/1/1a/Mouloudia_Club_d%27Alger_logo.svg/512px-Mouloudia_Club_d%27Alger_logo.svg.png", "tags" => "الجزائر العميد المولودية الشناوة"],
            ["name" => "شباب بلوزداد", "en_name" => "CR Belouizdad", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/f/f6/CR_Belouizdad.svg/512px-CR_Belouizdad.svg.png", "tags" => "الجزائر السياربي بلوزداد"],
            ["name" => "اتحاد الجزائر", "en_name" => "USM Alger", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/3/3e/USM_Alger_logo.svg/512px-USM_Alger_logo.svg.png", "tags" => "الجزائر سوسطارة الاتحاد"],
            ["name" => "شبيبة القبائل", "en_name" => "JS Kabylie", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/c/c5/JS_Kabylie_logo.svg/512px-JS_Kabylie_logo.svg.png", "tags" => "الجزائر الكناري الشبيبة"],

            // العراق والخليج
            ["name" => "الزوراء العراقي", "en_name" => "Al-Zawraa SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/4/49/Al-Zawraa_SC_logo.png/512px-Al-Zawraa_SC_logo.png", "tags" => "العراق النوارس الزوراء"],
            ["name" => "القوة الجوية العراقي", "en_name" => "Al-Quwa Al-Jawiya", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Al-Quwa_Al-Jawiya_logo.png/512px-Al-Quwa_Al-Jawiya_logo.png", "tags" => "العراق الصقور الجوية"],
            ["name" => "الشرطة العراقي", "en_name" => "Al-Shorta SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/9/9c/Al-Shorta_SC_logo.png/512px-Al-Shorta_SC_logo.png", "tags" => "العراق القيثارة الخضراء الشرطة"],
            ["name" => "الطلبة العراقي", "en_name" => "Al-Talaba SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/d/d7/Al-Talaba_SC_logo.png/512px-Al-Talaba_SC_logo.png", "tags" => "العراق الأنيق الطلبة"],
            ["name" => "أربيل العراقي", "en_name" => "Erbil SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/7/7b/Erbil_SC_logo.png/512px-Erbil_SC_logo.png", "tags" => "العراق قلعة أربيل"],
            ["name" => "السد القطري", "en_name" => "Al Sadd SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/9/91/Al_Sadd_SC_logo.svg/512px-Al_Sadd_SC_logo.svg.png", "tags" => "قطر الزعيم السد"],
            ["name" => "الدحيل القطري", "en_name" => "Al Duhail SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/1/1a/Al-Duhail_SC_logo.svg/512px-Al-Duhail_SC_logo.svg.png", "tags" => "قطر الدحيل الطوفان"],
            ["name" => "الريان القطري", "en_name" => "Al Rayyan SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/5/5a/Al_Rayyan_SC_logo.svg/512px-Al_Rayyan_SC_logo.svg.png", "tags" => "قطر الرهيب الريان"],
            ["name" => "العين الإماراتي", "en_name" => "Al Ain FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/7/77/Al_Ain_FC_logo.svg/512px-Al_Ain_FC_logo.svg.png", "tags" => "الإمارات الزعيم العيناوي بطل آسيا"],
            ["name" => "شباب الأهلي الإماراتي", "en_name" => "Shabab Al Ahli", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/9/92/Shabab_Al-Ahli_Dubai_FC_logo.svg/512px-Shabab_Al-Ahli_Dubai_FC_logo.svg.png", "tags" => "الإمارات الفرسان دبي"],
            ["name" => "الوحدة الإماراتي", "en_name" => "Al Wahda FC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/b/b5/Al_Wahda_FC_logo.svg/512px-Al_Wahda_FC_logo.svg.png", "tags" => "الإمارات أصحاب السعادة الوحدة"],
            ["name" => "الكويت الكويتي", "en_name" => "Kuwait SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/9/96/Kuwait_SC_logo.svg/512px-Kuwait_SC_logo.svg.png", "tags" => "الكويت العميد الأبيض"],
            ["name" => "القادسية الكويتي", "en_name" => "Qadsia SC", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/6/6f/Qadsia_SC_logo.svg/512px-Qadsia_SC_logo.svg.png", "tags" => "الكويت الملكي الأصفر"]
        ];
    }

    public static function getNationalTeams() {
        return [
            // المنتخبات العربية
            ["name" => "منتخب السعودية", "en_name" => "Saudi Arabia", "logo" => "https://flagcdn.com/w160/sa.png", "tags" => "الأخضر الصقور الخضر"],
            ["name" => "منتخب مصر", "en_name" => "Egypt", "logo" => "https://flagcdn.com/w160/eg.png", "tags" => "الفراعنة منتخب الساجدين"],
            ["name" => "منتخب المغرب", "en_name" => "Morocco", "logo" => "https://flagcdn.com/w160/ma.png", "tags" => "أسود الأطلس"],
            ["name" => "منتخب الجزائر", "en_name" => "Algeria", "logo" => "https://flagcdn.com/w160/dz.png", "tags" => "محاربو الصحراء الخضر"],
            ["name" => "منتخب العراق", "en_name" => "Iraq", "logo" => "https://flagcdn.com/w160/iq.png", "tags" => "أسود الرافدين"],
            ["name" => "منتخب تونس", "en_name" => "Tunisia", "logo" => "https://flagcdn.com/w160/tn.png", "tags" => "نسور قرطاج"],
            ["name" => "منتخب قطر", "en_name" => "Qatar", "logo" => "https://flagcdn.com/w160/qa.png", "tags" => "العنابي بطل آسيا"],
            ["name" => "منتخب الإمارات", "en_name" => "UAE", "logo" => "https://flagcdn.com/w160/ae.png", "tags" => "الأبيض الإماراتي"],
            ["name" => "منتخب الأردن", "en_name" => "Jordan", "logo" => "https://flagcdn.com/w160/jo.png", "tags" => "النشامى"],
            ["name" => "منتخب عمان", "en_name" => "Oman", "logo" => "https://flagcdn.com/w160/om.png", "tags" => "الأحمر العماني"],
            ["name" => "منتخب الكويت", "en_name" => "Kuwait", "logo" => "https://flagcdn.com/w160/kw.png", "tags" => "الأزرق الكويتي"],
            ["name" => "منتخب البحرين", "en_name" => "Bahrain", "logo" => "https://flagcdn.com/w160/bh.png", "tags" => "الأحمر البحريني"],
            ["name" => "منتخب سوريا", "en_name" => "Syria", "logo" => "https://flagcdn.com/w160/sy.png", "tags" => "نسور قاسيون"],
            ["name" => "منتخب فلسطين", "en_name" => "Palestine", "logo" => "https://flagcdn.com/w160/ps.png", "tags" => "الفدائي"],
            ["name" => "منتخب لبنان", "en_name" => "Lebanon", "logo" => "https://flagcdn.com/w160/lb.png", "tags" => "رجال الأرز"],
            ["name" => "منتخب السودان", "en_name" => "Sudan", "logo" => "https://flagcdn.com/w160/sd.png", "tags" => "صقور الجديان"],
            ["name" => "منتخب ليبيا", "en_name" => "Libya", "logo" => "https://flagcdn.com/w160/ly.png", "tags" => "فرسان المتوسط"],
            ["name" => "منتخب اليمن", "en_name" => "Yemen", "logo" => "https://flagcdn.com/w160/ye.png", "tags" => "اليمن السعيد"],
            ["name" => "منتخب موريتانيا", "en_name" => "Mauritania", "logo" => "https://flagcdn.com/w160/mr.png", "tags" => "المرابطون"],

            // المنتخبات العالمية الكبرى
            ["name" => "منتخب البرازيل", "en_name" => "Brazil", "logo" => "https://flagcdn.com/w160/br.png", "tags" => "السامبا السيليساو 5 كؤوس عالم"],
            ["name" => "منتخب الأرجنتين", "en_name" => "Argentina", "logo" => "https://flagcdn.com/w160/ar.png", "tags" => "التانغو ليو ميسي بطل العالم"],
            ["name" => "منتخب فرنسا", "en_name" => "France", "logo" => "https://flagcdn.com/w160/fr.png", "tags" => "الديوك مبابي"],
            ["name" => "منتخب ألمانيا", "en_name" => "Germany", "logo" => "https://flagcdn.com/w160/de.png", "tags" => "المانشافت الماكينات"],
            ["name" => "منتخب إسبانيا", "en_name" => "Spain", "logo" => "https://flagcdn.com/w160/es.png", "tags" => "الماتادور لا روخا بطل أوروبا"],
            ["name" => "منتخب إنجلترا", "en_name" => "England", "logo" => "https://flagcdn.com/w160/gb-eng.png", "tags" => "الأسود الثلاثة"],
            ["name" => "منتخب إيطاليا", "en_name" => "Italy", "logo" => "https://flagcdn.com/w160/it.png", "tags" => "الآتزوري الأزوري"],
            ["name" => "منتخب البرتغال", "en_name" => "Portugal", "logo" => "https://flagcdn.com/w160/pt.png", "tags" => "برازيل أوروبا كريستيانو رونالدو"],
            ["name" => "منتخب هولندا", "en_name" => "Netherlands", "logo" => "https://flagcdn.com/w160/nl.png", "tags" => "الطواحين الهولندية أورانيي"],
            ["name" => "منتخب كرواتيا", "en_name" => "Croatia", "logo" => "https://flagcdn.com/w160/hr.png", "tags" => "الناريون مودريتش"],
            ["name" => "منتخب بلجيكا", "en_name" => "Belgium", "logo" => "https://flagcdn.com/w160/be.png", "tags" => "الشياطين الحمر دي بروين"],
            ["name" => "منتخب أوروغواي", "en_name" => "Uruguay", "logo" => "https://flagcdn.com/w160/uy.png", "tags" => "السيليستي لا سيليستي"],
            ["name" => "منتخب اليابان", "en_name" => "Japan", "logo" => "https://flagcdn.com/w160/jp.png", "tags" => "الساموراي الأزرق"],
            ["name" => "منتخب كوريا الجنوبية", "en_name" => "South Korea", "logo" => "https://flagcdn.com/w160/kr.png", "tags" => "محاربو التايجوك سون"],
            ["name" => "منتخب السنغال", "en_name" => "Senegal", "logo" => "https://flagcdn.com/w160/sn.png", "tags" => "أسود التيرانجا ساديو ماني"],
            ["name" => "منتخب كوت ديفوار", "en_name" => "Ivory Coast", "logo" => "https://flagcdn.com/w160/ci.png", "tags" => "الأفيال ساحل العاج بطل إفريقيا"],
            ["name" => "منتخب نيجيريا", "en_name" => "Nigeria", "logo" => "https://flagcdn.com/w160/ng.png", "tags" => "النسور الممتازة أوسيمين"]
        ];
    }

    public static function getTournaments() {
        return [
            ["name" => "دوري أبطال أوروبا", "en_name" => "UEFA Champions League", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/UEFA_Champions_League_logo_2.svg/512px-UEFA_Champions_League_logo_2.svg.png", "tags" => "التشامبيونزليغ ذات الأذنين قارة أوروبا"],
            ["name" => "الدوري الإنجليزي الممتاز", "en_name" => "Premier League", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Premier_League_Logo.svg/512px-Premier_League_Logo.svg.png", "tags" => "البريميرليغ الدوري الإنجليزي إنجلترا"],
            ["name" => "الدوري الإسباني (لا ليغا)", "en_name" => "La Liga", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/LaLiga_logo_2023.svg/512px-LaLiga_logo_2023.svg.png", "tags" => "الليغا الدوري الإسباني إسبانيا"],
            ["name" => "الدوري الإيطالي (سيريا أ)", "en_name" => "Serie A", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/e/e1/Serie_A_logo_%282019%29.svg/512px-Serie_A_logo_%282019%29.svg.png", "tags" => "الكالتشيو إيطاليا سيريا آي"],
            ["name" => "الدوري الألماني (بوندسليغا)", "en_name" => "Bundesliga", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/d/df/Bundesliga_logo_%282017%29.svg/512px-Bundesliga_logo_%282017%29.svg.png", "tags" => "البوندسليغا ألمانيا"],
            ["name" => "الدوري الفرنسي (ليغ 1)", "en_name" => "Ligue 1", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Ligue_1_McDonald%27s_logo.svg/512px-Ligue_1_McDonald%27s_logo.svg.png", "tags" => "فرنسا ليغ 1 باريس"],
            ["name" => "دوري روشن السعودي", "en_name" => "Saudi Pro League", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/4/4e/Saudi_Pro_League_logo_%282023%29.svg/512px-Saudi_Pro_League_logo_%282023%29.svg.png", "tags" => "السعودية دوري روشن الهلال النصر الاتحاد الأهلي"],
            ["name" => "دوري أبطال آسيا للنخبة", "en_name" => "AFC Champions League Elite", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/5/5a/AFC_Champions_League_Elite_logo.svg/512px-AFC_Champions_League_Elite_logo.svg.png", "tags" => "آسيا النخبة دوري الأبطال"],
            ["name" => "دوري أبطال إفريقيا", "en_name" => "CAF Champions League", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/a/ad/CAF_Champions_League_logo.svg/512px-CAF_Champions_League_logo.svg.png", "tags" => "إفريقيا الأهلي الوداد الترجي صن داونز"],
            ["name" => "كأس العالم FIFA", "en_name" => "FIFA World Cup", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/e/e3/2026_FIFA_World_Cup.svg/512px-2026_FIFA_World_Cup.svg.png", "tags" => "المونديال كأس العالم 2026 أمريكا كندا المكسيك"],
            ["name" => "كأس أمم أوروبا (يورو)", "en_name" => "UEFA EURO", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/0/03/UEFA_Nations_League_logo.svg/512px-UEFA_Nations_League_logo.svg.png", "tags" => "اليورو كأس أوروبا دوري الأمم الأوروبية"],
            ["name" => "كأس أمم إفريقيا (كان)", "en_name" => "Africa Cup of Nations", "logo" => "https://upload.wikimedia.org/wikipedia/en/thumb/6/6f/CAF_logo.svg/512px-CAF_logo.svg.png", "tags" => "أمم إفريقيا الكان المغرب 2025"]
        ];
    }

    public static function getChannels() {
        return [
            ["name" => "beIN SPORTS NEWS", "en_name" => "beIN SPORTS News", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/BeIN_Sports_News_logo.png/512px-BeIN_Sports_News_logo.png", "tags" => "بي ان سبورتس الإخبارية مجاناً"],
            ["name" => "beIN SPORTS 1 HD", "en_name" => "beIN SPORTS 1 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/BeIN_Sports_1_logo.png/512px-BeIN_Sports_1_logo.png", "tags" => "بي ان 1 دوري أبطال أوروبا الدوري الإنجليزي"],
            ["name" => "beIN SPORTS 2 HD", "en_name" => "beIN SPORTS 2 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/BeIN_Sports_2_logo.png/512px-BeIN_Sports_2_logo.png", "tags" => "بي ان 2 الدوري الإسباني"],
            ["name" => "beIN SPORTS 3 HD", "en_name" => "beIN SPORTS 3 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/BeIN_Sports_3_logo.png/512px-BeIN_Sports_3_logo.png", "tags" => "بي ان 3 الدوري الإيطالي الفرنسي"],
            ["name" => "beIN SPORTS 4 HD", "en_name" => "beIN SPORTS 4 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png", "tags" => "بي ان 4"],
            ["name" => "beIN SPORTS 4K", "en_name" => "beIN SPORTS 4K UHD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png", "tags" => "بي ان 4K قمة الأسبوع فور كي"],
            ["name" => "SSC 1 HD", "en_name" => "SSC 1 HD Sports", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/SSC_Channel_Logo.png/512px-SSC_Channel_Logo.png", "tags" => "السعودية دوري روشن SSC 1"],
            ["name" => "SSC EXTRA 1", "en_name" => "SSC Extra 1 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/SSC_Channel_Logo.png/512px-SSC_Channel_Logo.png", "tags" => "السعودية إكسترا دوري أبطال آسيا"],
            ["name" => "قناة الكأس 1 HD", "en_name" => "Alkass 1 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Alkass_Digital_Logo.png/512px-Alkass_Digital_Logo.png", "tags" => "الكأس قطر تصفيات كأس العالم"],
            ["name" => "أبوظبي الرياضية 1", "en_name" => "AD Sports 1 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Abu_Dhabi_Sports_logo_2020.svg/512px-Abu_Dhabi_Sports_logo_2020.svg.png", "tags" => "أبوظبي الدوري الإماراتي كأس إيطاليا"],
            ["name" => "دبي الرياضية 1", "en_name" => "Dubai Sports 1 HD", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/7/70/Dubai_Sports_logo.svg/512px-Dubai_Sports_logo.svg.png", "tags" => "دبي الرياضية"],
            ["name" => "أون تايم سبورت 1", "en_name" => "On Time Sports 1", "logo" => "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/ON_Time_Sports_Logo.png/512px-ON_Time_Sports_Logo.png", "tags" => "مصر أون تايم الدوري المصري الأهلي والزمالك"]
        ];
    }
}
