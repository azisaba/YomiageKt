package net.azisaba.yomiagekt.data

enum class Characters(val characterName: String, val description: String, val speakerIndex: Int, val terms: String, val nsfwType: NsfwType, val credit: String = "VOICEVOX:$characterName") {
    METAN("四国めたん", "はっきりした芯のある声", 0, "https://zunko.jp/con_ongen_kiyaku.html", NsfwType.Plausible),
    ZUNDA("ずんだもん", "子供っぽい高めの声", 1, "https://zunko.jp/con_ongen_kiyaku.html", NsfwType.Plausible),
    TSUMUGI("春日部つむぎ", "元気な明るい声", 2, "https://tsumugi-official.studio.site/rule", NsfwType.Plausible),
    AMEHAU("雨晴はう", "優しく可愛い声", 3, "https://amehau.com/?page_id=225", NsfwType.Allowed),
    RITSU("波音リツ", "低めのクールな声", 4, "https://canon-voice.com/terms/", NsfwType.Allowed),
    VIRVOX1("玄野武宏", "爽やかな青年の声", 5, "https://virvoxproject.wixsite.com/official/voicevox%E3%81%AE%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Plausible),
    VIRVOX2("白上虎太郎", "声変わり直後の少年の声", 6, "https://virvoxproject.wixsite.com/official/voicevox%E3%81%AE%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Plausible),
    VIRVOX3("青山龍星", "重厚で低音な声", 7, "https://virvoxproject.wixsite.com/official/voicevox%E3%81%AE%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Plausible),
    HIMARI("冥鳴ひまり", "柔らかく温かい声", 8, "https://meimeihimari.wixsite.com/himari/terms-of-use", NsfwType.Plausible),
    SORA("九州そら", "気品のある大人な声", 9, "https://zunko.jp/con_ongen_kiyaku.html", NsfwType.Plausible),
    MOCHIKO("もち子さん", "明瞭で穏やかな声", 10, "https://vtubermochio.wixsite.com/mochizora/%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Plausible, "VOICEVOX もち子(cv明日葉よもぎ)"),
    FRONTIER1("剣崎雌雄", "安心感のある落ち着いた声", 11, "https://frontier.creatia.cc/fanclubs/413/posts/4507", NsfwType.Disallowed),
    WHITECUL("WhiteCUL", "聞き心地のよい率直な声", 12, "https://www.whitecul.com/guideline", NsfwType.Disallowed),
    TUINA("後鬼", "包容力のある奥ゆかしい声", 13, "https://ついなちゃん.com/voicevox_terms/", NsfwType.Plausible),
    NO_7("No.7", "しっかりした凛々しい声", 14, "https://voiceseven.com/", NsfwType.Disallowed),
    CHIBI_JII("ちび式じい", "親しみのある嗄れ声", 15, "https://docs.google.com/presentation/d/1AcD8zXkfzKFf2ertHwWRwJuQXjNnijMxhz7AJzEkaI4", NsfwType.Disallowed),
    MIKO("櫻歌ミコ", "かわいらしい少女の声", 16, "https://voicevox35miko.studio.site/rule (ニコニコ動画に動画を投稿する際は https://commons.nicovideo.jp/material/nc287264 を親作品として登録してください。)", NsfwType.Plausible),
    SAYO("小夜/SAYO", "和やかで温厚な声", 17, "https://316soramegu.wixsite.com/sayo-official/guideline", NsfwType.Plausible),
    ROBOT("ナースロボ＿タイプＴ", "冷静で慎み深い声", 18, "https://www.krnr.top/rules", NsfwType.Plausible),
    NC296132("†聖騎士 紅桜†", "快活でハキハキした声", 19, "https://commons.nicovideo.jp/material/nc296132", NsfwType.Disallowed),
    VIRVOX4("雀松朱司", "物静かで安定した声", 20, "https://virvoxproject.wixsite.com/official/voicevox%E3%81%AE%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Plausible),
    VIRVOX5("麒ヶ島宗麟", "渋いおじさん声", 21, "https://virvoxproject.wixsite.com/official/voicevox%E3%81%AE%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Plausible),
    NANA("春歌ナナ", "はつらつとした力強い声", 22, "https://nanahira.jp/haruka_nana/guideline.html", NsfwType.Disallowed),
    NEKOTUKA1("猫使アル", "厚みのある気さくな声", 23, "https://nekotukarb.wixsite.com/nekonohako/%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Disallowed),
    NEKOTUKA2("猫使ビィ", "ピュアであどけない声", 24, "https://nekotukarb.wixsite.com/nekonohako/%E5%88%A9%E7%94%A8%E8%A6%8F%E7%B4%84", NsfwType.Disallowed),
}

enum class NsfwType(val description: String) {
    Allowed(":o:"),
    Plausible(":warning: (他人に不快感を与えない程度にバレないようにこっそりやってください)"),
    Disallowed(":x: (禁止)"),
}
