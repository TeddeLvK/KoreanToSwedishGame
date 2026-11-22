package com.swedishgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MiniGameOverlay {
    public static final String MODE_COMPOUNDS = "compounds";
    public static final String MODE_VOCAB = "vocab";
    public static final String MODE_WRITING = "writing";

    private static final int ROUND_SIZE = 10;

    private static final String[][] COMPOUND_WORDS = {
        {"kaffekopp", "kaffe + kopp", "coffee cup", "커피잔", "فنجان قهوة", "咖啡杯", "taza de café"},
        {"sjukhus", "sjuk + hus", "hospital (sick + house)", "병원 (아픈 + 집)", "مستشفى", "医院", "hospital"},
        {"köttbulle", "kött + bulle", "meatball (meat + bun)", "미트볼 (고기 + 둥근빵)", "كرة لحم", "肉丸", "albóndiga"},
        {"kanelbulle", "kanel + bulle", "cinnamon bun", "시나몬 번", "لفافة القرفة", "肉桂卷", "bollo de canela"},
        {"tågstation", "tåg + station", "train station", "기차역", "محطة قطار", "火车站", "estación de tren"},
        {"sommarstuga", "sommar + stuga", "summer cottage", "여름 별장", "كوخ صيفي", "避暑小屋", "cabaña de verano"},
        {"födelsedag", "födelse + dag", "birthday (birth + day)", "생일", "عيد ميلاد", "生日", "cumpleaños"},
        {"morgonkaffe", "morgon + kaffe", "morning coffee", "모닝 커피", "قهوة الصباح", "晨间咖啡", "café de la mañana"},
        {"solglasögon", "sol + glasögon", "sunglasses (sun + glasses)", "선글라스", "نظارة شمسية", "太阳镜", "gafas de sol"},
        {"tandborste", "tand + borste", "toothbrush (tooth + brush)", "칫솔", "فرشاة أسنان", "牙刷", "cepillo de dientes"},
        {"fotboll", "fot + boll", "football (foot + ball)", "축구", "كرة القدم", "足球", "fútbol"},
        {"hemläxa", "hem + läxa", "homework (home + lesson)", "숙제", "واجب منزلي", "家庭作业", "deberes"},
        {"skolbuss", "skola + buss", "school bus", "스쿨버스", "حافلة مدرسية", "校车", "autobús escolar"},
        {"flygplats", "flyg + plats", "airport (flight + place)", "공항", "مطار", "机场", "aeropuerto"},
        {"jordgubbe", "jord + gubbe", "strawberry (earth + old man)", "딸기", "فراولة", "草莓", "fresa"},
        {"snögubbe", "snö + gubbe", "snowman (snow + old man)", "눈사람", "رجل ثلج", "雪人", "muñeco de nieve"},
        {"regnjacka", "regn + jacka", "rain jacket", "비옷 / 레인재킷", "معطف مطر", "雨衣", "impermeable"},
        {"bokhandel", "bok + handel", "bookstore (book + trade)", "서점", "مكتبة بيع كتب", "书店", "librería"},
        {"sovrum", "sova + rum", "bedroom (sleep + room)", "침실", "غرفة نوم", "卧室", "dormitorio"},
        {"badrum", "bada + rum", "bathroom (bathe + room)", "욕실", "حمام", "浴室", "baño"},
        {"matbord", "mat + bord", "dining table (food + table)", "식탁", "طاولة طعام", "餐桌", "mesa de comedor"},
        {"husnyckel", "hus + nyckel", "house key", "집 열쇠", "مفتاح المنزل", "家门钥匙", "llave de casa"},
        {"vinterjacka", "vinter + jacka", "winter jacket", "겨울 재킷", "معطف شتوي", "冬衣", "chaqueta de invierno"},
        {"kaffepaus", "kaffe + paus", "coffee break", "커피 휴식", "استراحة قهوة", "咖啡休息", "pausa de café"},
        {"handduk", "hand + duk", "towel (hand + cloth)", "수건", "منشفة", "毛巾", "toalla"},
        {"glasögon", "glas + ögon", "eyeglasses (glass + eyes)", "안경", "نظارات", "眼镜", "gafas"},
        {"veckodag", "vecka + dag", "weekday (week + day)", "평일 / 요일", "يوم الأسبوع", "工作日", "día de la semana"},
        {"cykelväg", "cykel + väg", "bike path (bike + road)", "자전거 도로", "مسار دراجات", "自行车道", "carril bici"},
        {"fredagsmys", "fredag + mys", "cozy Friday evening", "금요일 힐링 타임", "أمسية جمعة مريحة", "周五温馨夜", "viernes acogedor"},
        {"frukostbord", "frukost + bord", "breakfast table", "아침 식탁", "طاولة الإفطار", "早餐桌", "mesa de desayuno"}
    };

    private static final char[] ROW1_LOWER = {'q','w','e','r','t','y','u','i','o','p','å'};
    private static final char[] ROW1_UPPER = {'Q','W','E','R','T','Y','U','I','O','P','Å'};
    private static final char[] ROW2_LOWER = {'a','s','d','f','g','h','j','k','l','ö','ä'};
    private static final char[] ROW2_UPPER = {'A','S','D','F','G','H','J','K','L','Ö','Ä'};
    private static final char[] ROW3_LOWER = {'z','x','c','v','b','n','m'};
    private static final char[] ROW3_UPPER = {'Z','X','C','V','B','N','M'};

    private static class Question {
        final String prompt;
        final String targetSwedish;
        final String targetMeaning;
        final List<String> options;
        final String answer;

        Question(String prompt, String targetSwedish, String targetMeaning, List<String> options, String answer) {
            this.prompt = prompt;
            this.targetSwedish = targetSwedish;
            this.targetMeaning = targetMeaning;
            this.options = options;
            this.answer = answer;
        }
    }

    private final String mode;
    private final AssetLoader assets;
    private final List<Level> levels;
    private final Glossary glossary;
    private final List<Question> questions = new ArrayList<>();

    private int index = 0;
    private int score = 0;
    private boolean answered = false;
    private boolean lastCorrect = false;
    private String lastFeedback = "";
    private boolean finished = false;
    private boolean hintShown = false;

    private final StringBuilder typedText = new StringBuilder();
    private boolean shiftActive = false;

    private final List<Rectangle> optionRects = new ArrayList<>();
    private final List<Rectangle> keyRects = new ArrayList<>();
    private final List<Character> keyChars = new ArrayList<>();

    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 96, 40, 36, 36);
    private final Rectangle nextBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 95, UIUtils.SCREEN_HEIGHT - 86, 190, 42);
    private final Rectangle replayBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 190, UIUtils.SCREEN_HEIGHT - 120, 175, 44);
    private final Rectangle menuBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 + 15, UIUtils.SCREEN_HEIGHT - 120, 175, 44);

    private Rectangle shiftRect = new Rectangle();
    private Rectangle backRect = new Rectangle();
    private Rectangle clearRect = new Rectangle();
    private Rectangle spaceRect = new Rectangle();
    private Rectangle hintRect = new Rectangle();
    private Rectangle submitRect = new Rectangle();

    public MiniGameOverlay(String mode, List<Level> levels, Glossary glossary, AssetLoader assets) {
        this.mode = mode;
        this.levels = levels;
        this.glossary = glossary;
        this.assets = assets;
        buildRound();
    }

    public int getScore() {
        return score;
    }

    private String compoundMeaning(String[] entry) {
        int idx = 2 + Lang.current().index;
        return idx < entry.length ? entry[idx] : entry[2];
    }

    private void buildRound() {
        questions.clear();
        index = 0;
        score = 0;
        answered = false;
        finished = false;
        hintShown = false;
        shiftActive = false;
        typedText.setLength(0);

        Random rng = new Random();
        if (MODE_COMPOUNDS.equals(mode)) {
            buildCompoundRound(rng);
        } else if (MODE_VOCAB.equals(mode)) {
            buildVocabRound(rng);
        } else {
            buildWritingRound(rng);
        }
    }

    private void buildCompoundRound(Random rng) {
        List<String[]> pool = new ArrayList<>();
        Collections.addAll(pool, COMPOUND_WORDS);
        Collections.shuffle(pool, rng);
        int count = Math.min(ROUND_SIZE, pool.size());
        for (int i = 0; i < count; i++) {
            String[] item = pool.get(i);
            String word = item[0];
            String parts = item[1];
            String meaning = compoundMeaning(item);
            boolean askMeaning = (i % 2 == 0);
            String correct = askMeaning ? meaning : word;
            List<String> opts = new ArrayList<>();
            opts.add(correct);
            List<String[]> distractors = new ArrayList<>(pool);
            Collections.shuffle(distractors, rng);
            for (String[] d : distractors) {
                if (opts.size() >= 4) break;
                String val = askMeaning ? compoundMeaning(d) : d[0];
                if (!opts.contains(val)) opts.add(val);
            }
            Collections.shuffle(opts, rng);
            String prompt = askMeaning
                ? Lang.tr(
                    "Compound word:  " + word + "  (" + parts + ")  ->  What does it mean?",
                    "합성어:  " + word + "  (" + parts + ")  ->  무슨 뜻인가요?",
                    "كلمة مركبة:  " + word + "  (" + parts + ")  ->  ما معناها؟",
                    "复合词:  " + word + "  (" + parts + ")  ->  是什么意思？",
                    "Palabra compuesta:  " + word + "  (" + parts + ")  ->  ¿Qué significa?"
                )
                : Lang.tr(
                    "Combine (" + parts + ") for "" + meaning + "":",
                    "(" + parts + ")을(를) 합쳐 "" + meaning + ""을(를) 만들면?",
                    "ادمج (" + parts + ") لمعنى "" + meaning + "":",
                    "组合 (" + parts + ") 表示 "" + meaning + "":",
                    "Combina (" + parts + ") para "" + meaning + "":"
                );
            questions.add(new Question(prompt, word, meaning + " (" + parts + ")", opts, correct));
        }
    }

    private List<String[]> collectVocab(boolean writingOnly) {
        List<String[]> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (glossary != null) {
            for (Map.Entry<String, String> e : glossary.getAllEntries().entrySet()) {
                if (isValidWord(e.getKey(), writingOnly) && seen.add(e.getKey())) {
                    out.add(new String[]{e.getKey(), e.getValue()});
                }
            }
        }
        return out;
    }

    private boolean isValidWord(String sv, boolean writingOnly) {
        if (sv == null || sv.isEmpty()) return false;
        if (!writingOnly) return true;
        if (sv.length() > 12 || sv.contains(" ")) return false;
        for (int i = 0; i < sv.length(); i++) {
            if (!Character.isLetter(sv.charAt(i))) return false;
        }
        return true;
    }

    private void buildVocabRound(Random rng) {
        List<String[]> pool = collectVocab(false);
        Collections.shuffle(pool, rng);
        int count = Math.min(ROUND_SIZE, pool.size());
        for (int i = 0; i < count; i++) {
            String[] pair = pool.get(i);
            boolean svToNative = (i % 2 == 0);
            String correct = svToNative ? pair[1] : pair[0];
            List<String> opts = new ArrayList<>();
            opts.add(correct);
            List<String[]> copy = new ArrayList<>(pool);
            Collections.shuffle(copy, rng);
            for (String[] c : copy) {
                if (opts.size() >= 4) break;
                String val = svToNative ? c[1] : c[0];
                if (!opts.contains(val)) opts.add(val);
            }
            Collections.shuffle(opts, rng);
            String prompt = svToNative
                ? Lang.tr("What does  "" + pair[0] + ""  mean?", """ + pair[0] + ""의 뜻은 무엇인가요?", "ما معنى  "" + pair[0] + ""؟", """ + pair[0] + "" 是什么意思？", "¿Qué significa  "" + pair[0] + ""?")
                : Lang.tr("Which Swedish word means  "" + pair[1] + ""?", """ + pair[1] + ""을(를) 뜻하는 스웨덴어는?", "أي كلمة سويدية تعني  "" + pair[1] + ""؟", "哪个瑞典语词表示  "" + pair[1] + ""？", "¿Qué palabra sueca significa  "" + pair[1] + ""?");
            questions.add(new Question(prompt, pair[0], pair[1], opts, correct));
        }
    }

    private void buildWritingRound(Random rng) {
        List<String[]> pool = collectVocab(true);
        Collections.shuffle(pool, rng);
        int count = Math.min(ROUND_SIZE, pool.size());
        for (int i = 0; i < count; i++) {
            String[] pair = pool.get(i);
            String prompt = Lang.tr(
                "Type in Swedish:  "" + pair[1] + """,
                "스웨덴어로 입력:  "" + pair[1] + """,
                "اكتب بالسويدية:  "" + pair[1] + """,
                "用瑞典语拼写:  "" + pair[1] + """,
                "Escribe en sueco:  "" + pair[1] + """
            );
            questions.add(new Question(prompt, pair[0], pair[1], Collections.emptyList(), pair[0]));
        }
    }

    private Question current() {
        return questions.get(index);
    }

    private void submitChoice(String chosen) {
        Question q = current();
        answered = true;
        lastCorrect = chosen.equals(q.answer);
        if (lastCorrect) {
            score++;
            lastFeedback = Lang.tr("Correct!", "정답!", "صحيح!", "正确!", "¡Correcto!") + "  " + q.targetSwedish + "  =  " + q.targetMeaning;
        } else {
            lastFeedback = Lang.tr("Correct answer: ", "정답: ", "الإجابة الصحيحة: ", "正确答案: ", "Respuesta correcta: ") + q.answer + "  (" + q.targetSwedish + " = " + q.targetMeaning + ")";
        }
    }

    private void submitWriting() {
        String typed = typedText.toString().trim();
        if (typed.isEmpty()) return;
        Question q = current();
        answered = true;
        lastCorrect = typed.equalsIgnoreCase(q.answer.trim());
        if (lastCorrect) {
            score++;
            lastFeedback = Lang.tr("Perfect!", "완벽해요!", "ممتاز!", "太棒了!", "¡Perfecto!") + "  " + q.answer + "  =  " + q.targetMeaning;
        } else {
            lastFeedback = typed + "   |   " + Lang.tr("Correct: ", "정답: ", "الصحيح: ", "正确: ", "Correcto: ") + q.answer;
        }
    }

    private void advance() {
        index++;
        answered = false;
        hintShown = false;
        shiftActive = false;
        typedText.setLength(0);
        if (index >= questions.size()) {
            finished = true;
        }
    }

    public String onKeyPressed(int keyCode, char keyChar) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            return "close";
        }
        if (finished) {
            if (keyCode == KeyEvent.VK_ENTER) return "close";
            return null;
        }
        if (answered) {
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                advance();
            }
            return null;
        }
        if (MODE_WRITING.equals(mode)) {
            if (keyCode == KeyEvent.VK_BACK_SPACE) {
                if (typedText.length() > 0) {
                    typedText.deleteCharAt(typedText.length() - 1);
                }
            } else if (keyCode == KeyEvent.VK_SPACE) {
                typedText.append(' ');
            } else if (keyCode == KeyEvent.VK_ENTER) {
                submitWriting();
            } else if (Character.isLetter(keyChar)) {
                typedText.append(keyChar);
            }
            return null;
        }
        int optIdx = -1;
        if (keyCode == KeyEvent.VK_1) optIdx = 0;
        else if (keyCode == KeyEvent.VK_2) optIdx = 1;
        else if (keyCode == KeyEvent.VK_3) optIdx = 2;
        else if (keyCode == KeyEvent.VK_4) optIdx = 3;
        if (optIdx >= 0 && optIdx < current().options.size()) {
            submitChoice(current().options.get(optIdx));
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (closeBtn.contains(p)) {
            return "close";
        }
        if (finished) {
            if (replayBtn.contains(p)) {
                buildRound();
                return null;
            }
            if (menuBtn.contains(p)) {
                return "close";
            }
            return null;
        }
        if (answered) {
            if (nextBtn.contains(p)) {
                advance();
            }
            return null;
        }
        if (MODE_WRITING.equals(mode)) {
            for (int i = 0; i < keyRects.size(); i++) {
                if (keyRects.get(i).contains(p)) {
                    typedText.append(keyChars.get(i));
                    shiftActive = false;
                    return null;
                }
            }
            if (shiftRect.contains(p)) {
                shiftActive = !shiftActive;
                return null;
            }
            if (backRect.contains(p)) {
                if (typedText.length() > 0) {
                    typedText.deleteCharAt(typedText.length() - 1);
                }
                return null;
            }
            if (clearRect.contains(p)) {
                typedText.setLength(0);
                return null;
            }
            if (spaceRect.contains(p)) {
                typedText.append(' ');
                return null;
            }
            if (hintRect.contains(p)) {
                hintShown = true;
                return null;
            }
            if (submitRect.contains(p)) {
                submitWriting();
                return null;
            }
            return null;
        }

        for (int i = 0; i < optionRects.size(); i++) {
            if (optionRects.get(i).contains(p)) {
                submitChoice(current().options.get(i));
                return null;
            }
        }
        return null;
    }

    private String getTitle() {
        if (MODE_COMPOUNDS.equals(mode)) {
            return Lang.tr(
                "Mini Game 1: Swedish Compound Words (Sammansatta ord)",
                "미니게임 1: 스웨덴어 합성어 (Sammansatta ord)",
                "لعبة 1: الكلمات المركبة السويدية (Sammansatta ord)",
                "小游戏 1: 瑞典语复合词 (Sammansatta ord)",
                "Minijuego 1: Palabras compuestas (Sammansatta ord)"
            );
        }
        if (MODE_VOCAB.equals(mode)) {
            return Lang.tr(
                "Mini Game 2: Vocabulary Drill (Ordförråd)",
                "미니게임 2: 단어 연습 (Ordförråd)",
                "لعبة 2: تدريب المفردات (Ordförråd)",
                "小游戏 2: 词汇练习 (Ordförråd)",
                "Minijuego 2: Vocabulario (Ordförråd)"
            );
        }
        return Lang.tr(
            "Mini Game 3: Swedish Word Writing (Skriva ord)",
            "미니게임 3: 스웨덴어 단어 쓰기 (Skriva ord)",
            "لعبة 3: كتابة الكلمات السويدية (Skriva ord)",
            "小游戏 3: 瑞典语拼写 (Skriva ord)",
            "Minijuego 3: Escritura sueca (Skriva ord)"
        );
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(UIUtils.DIM);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        Rectangle panel = new Rectangle(48, 28, UIUtils.SCREEN_WIDTH - 96, UIUtils.SCREEN_HEIGHT - 56);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(19, true);
        Font fSub = assets.getFont(13, false);
        Font fPrompt = assets.getFont(19, true);
        Font fOpt = assets.getFont(15, true);
        Font fKey = assets.getFont(18, true);
        Font fBtn = assets.getFont(14, true);

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString(getTitle(), panel.x + 24, panel.y + 38);

        UIUtils.drawButton(g, closeBtn, "X", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);

        if (finished) {
            drawSummary(g, panel, mouse, fPrompt, fBtn);
            return;
        }

        g.setFont(fSub);
        g.setColor(UIUtils.MUTED);
        g.drawString("Question " + (index + 1) + " / " + questions.size() + "   •   Score: " + score, panel.x + 24, panel.y + 62);

        Rectangle barBg = new Rectangle(panel.x + 24, panel.y + 72, panel.width - 48, 8);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(barBg.x, barBg.y, barBg.width, barBg.height, 6, 6);
        int fillW = (int) Math.round(barBg.width * ((double) index / Math.max(1, questions.size())));
        g.setColor(UIUtils.ACCENT);
        g.fillRoundRect(barBg.x, barBg.y, Math.max(8, fillW), barBg.height, 6, 6);

        Question q = current();
        if (MODE_WRITING.equals(mode)) {
            drawWritingMode(g, panel, q, mouse, fPrompt, fKey, fBtn, fSub);
        } else {
            drawChoiceMode(g, panel, q, mouse, fPrompt, fOpt, fBtn);
        }
    }

    private void drawChoiceMode(Graphics2D g, Rectangle panel, Question q, Point mouse, Font fPrompt, Font fOpt, Font fBtn) {
        g.setFont(fPrompt);
        g.setColor(UIUtils.TEXT);
        int y = panel.y + 130;
        for (String line : UIUtils.wrapText(q.prompt, g.getFontMetrics(), panel.width - 48)) {
            g.drawString(line, panel.x + 24, y);
            y += 30;
        }

        y += 20;
        optionRects.clear();
        int colW = (panel.width - 68) / 2;
        for (int i = 0; i < q.options.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            Rectangle r = new Rectangle(panel.x + 24 + col * (colW + 20), y + row * 78, colW, 62);
            optionRects.add(r);
            Color bg = UIUtils.PANEL_LIGHT;
            Color hover = UIUtils.ACCENT;
            if (answered) {
                if (q.options.get(i).equals(q.answer)) {
                    bg = UIUtils.SUCCESS;
                    hover = UIUtils.SUCCESS;
                } else {
                    bg = UIUtils.PANEL_DARK;
                    hover = UIUtils.PANEL_DARK;
                }
            }
            UIUtils.drawButton(g, r, (i + 1) + ".  " + q.options.get(i), fOpt, bg, hover, answered ? null : mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }

        if (answered) {
            Rectangle fb = new Rectangle(panel.x + 24, panel.y + panel.height - 140, panel.width - 48, 52);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setColor(lastCorrect ? UIUtils.SUCCESS : UIUtils.ERROR);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setFont(assets.getFont(14, true));
            g.drawString(lastFeedback, fb.x + 16, fb.y + 32);
            UIUtils.drawButton(g, nextBtn, index + 1 < questions.size() ? Lang.tr("Next [Enter]", "다음 [Enter]", "التالي [Enter]", "下一题 [Enter]", "Siguiente [Enter]") : Lang.tr("Results", "결과 보기", "النتائج", "查看结果", "Resultados"), fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
        }
    }

    private void drawWritingMode(Graphics2D g, Rectangle panel, Question q, Point mouse, Font fPrompt, Font fKey, Font fBtn, Font fSub) {
        g.setFont(fPrompt);
        g.setColor(UIUtils.TEXT);
        g.drawString(q.prompt, panel.x + 24, panel.y + 122);

        if (hintShown && !q.answer.isEmpty()) {
            g.setFont(fSub);
            g.setColor(UIUtils.GOLD);
            g.drawString("Hint: "" + q.answer.substring(0, Math.min(2, q.answer.length())) + "..."  (" + q.answer.length() + " letters)", panel.x + 24, panel.y + 148);
        }

        Rectangle inputBox = new Rectangle(panel.x + 24, panel.y + 162, panel.width - 48, 58);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(inputBox.x, inputBox.y, inputBox.width, inputBox.height, 14, 14);
        g.setColor(answered ? (lastCorrect ? UIUtils.SUCCESS : UIUtils.ERROR) : UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(inputBox.x, inputBox.y, inputBox.width, inputBox.height, 14, 14);

        String typed = typedText.toString();
        Font fInput = assets.getFont(24, true);
        g.setFont(fInput);
        if (typed.isEmpty() && !answered) {
            g.setFont(fSub);
            g.setColor(UIUtils.MUTED);
            g.drawString(Lang.tr("Tap the Swedish phone keyboard below (includes Å, Ä, Ö)...", "아래 스웨덴어 키보드를 눌러 입력하세요 (Å, Ä, Ö 포함)...", "اضغط على لوحة المفاتيح السويدية أدناه (Å, Ä, Ö)...", "点击下方瑞典语键盘输入 (含 Å, Ä, Ö)...", "Toca el teclado sueco abajo (incluye Å, Ä, Ö)..."), inputBox.x + 18, inputBox.y + 35);
        } else {
            g.setColor(UIUtils.TEXT);
            g.drawString(answered ? typed : typed + "|", inputBox.x + 18, inputBox.y + 38);
        }

        if (answered) {
            Rectangle fb = new Rectangle(panel.x + 24, panel.y + 240, panel.width - 48, 60);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setColor(lastCorrect ? UIUtils.SUCCESS : UIUtils.ERROR);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setFont(assets.getFont(15, true));
            g.drawString(lastFeedback, fb.x + 18, fb.y + 36);
            UIUtils.drawButton(g, nextBtn, index + 1 < questions.size() ? "Next [Enter]" : "Results", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
            return;
        }

        keyRects.clear();
        keyChars.clear();
        int kw = 60;
        int kh = 54;
        int gap = 5;
        int kbY = panel.y + 244;

        char[] r1 = shiftActive ? ROW1_UPPER : ROW1_LOWER;
        int r1StartX = panel.x + (panel.width - (r1.length * kw + (r1.length - 1) * gap)) / 2;
        for (int i = 0; i < r1.length; i++) {
            Rectangle r = new Rectangle(r1StartX + i * (kw + gap), kbY, kw, kh);
            keyRects.add(r);
            keyChars.add(r1[i]);
            boolean special = (i == 10);
            UIUtils.drawButton(g, r, String.valueOf(r1[i]), fKey, special ? new Color(56, 68, 98) : UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, special ? UIUtils.GOLD : UIUtils.TEXT, UIUtils.HUD_BORDER);
        }

        int r2Y = kbY + kh + 10;
        char[] r2 = shiftActive ? ROW2_UPPER : ROW2_LOWER;
        int r2StartX = panel.x + (panel.width - (r2.length * kw + (r2.length - 1) * gap)) / 2;
        for (int i = 0; i < r2.length; i++) {
            Rectangle r = new Rectangle(r2StartX + i * (kw + gap), r2Y, kw, kh);
            keyRects.add(r);
            keyChars.add(r2[i]);
            boolean special = (i >= 9);
            UIUtils.drawButton(g, r, String.valueOf(r2[i]), fKey, special ? new Color(56, 68, 98) : UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, special ? UIUtils.GOLD : UIUtils.TEXT, UIUtils.HUD_BORDER);
        }

        int r3Y = r2Y + kh + 10;
        char[] r3 = shiftActive ? ROW3_UPPER : ROW3_LOWER;
        int specW = 92;
        int r3TotalW = specW * 2 + r3.length * kw + (r3.length + 1) * gap;
        int r3X = panel.x + (panel.width - r3TotalW) / 2;
        shiftRect = new Rectangle(r3X, r3Y, specW, kh);
        UIUtils.drawButton(g, shiftRect, "SHIFT", fBtn, shiftActive ? UIUtils.GOLD : UIUtils.PANEL_DARK, UIUtils.GOLD, mouse, shiftActive ? new Color(18, 22, 30) : UIUtils.TEXT, UIUtils.HUD_BORDER);
        int curX = r3X + specW + gap;
        for (int i = 0; i < r3.length; i++) {
            Rectangle r = new Rectangle(curX, r3Y, kw, kh);
            keyRects.add(r);
            keyChars.add(r3[i]);
            UIUtils.drawButton(g, r, String.valueOf(r3[i]), fKey, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
            curX += kw + gap;
        }
        backRect = new Rectangle(curX, r3Y, specW, kh);
        UIUtils.drawButton(g, backRect, "DEL", fBtn, UIUtils.PANEL_DARK, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);

        int r4Y = r3Y + kh + 12;
        int r4TotalW = 115 + 220 + 115 + 160 + 30;
        int r4X = panel.x + (panel.width - r4TotalW) / 2;
        clearRect = new Rectangle(r4X, r4Y, 115, 48);
        spaceRect = new Rectangle(r4X + 125, r4Y, 220, 48);
        hintRect = new Rectangle(r4X + 355, r4Y, 115, 48);
        submitRect = new Rectangle(r4X + 480, r4Y, 160, 48);

        UIUtils.drawButton(g, clearRect, "Clear", fBtn, UIUtils.PANEL_DARK, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, spaceRect, "Space", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, hintRect, "Hint", fBtn, UIUtils.PANEL_DARK, UIUtils.GOLD, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, submitRect, "Submit", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
    }

    private void drawSummary(Graphics2D g, Rectangle panel, Point mouse, Font fPrompt, Font fBtn) {
        int pct = questions.isEmpty() ? 100 : (int) Math.round(score * 100.0 / questions.size());
        int xpEarned = score * 15;

        g.setFont(assets.getFont(28, true));
        g.setColor(UIUtils.GOLD);
        String done = Lang.tr("Mini Game Complete!", "미니게임 완료!", "اكتملت اللعبة!", "小游戏完成！", "¡Minijuego completado!");
        FontMetrics fm = g.getFontMetrics();
        g.drawString(done, panel.x + (panel.width - fm.stringWidth(done)) / 2, panel.y + 180);

        g.setFont(fPrompt);
        g.setColor(UIUtils.TEXT);
        String sc = "Score: " + score + " / " + questions.size() + "  (" + pct + "%)";
        fm = g.getFontMetrics();
        g.drawString(sc, panel.x + (panel.width - fm.stringWidth(sc)) / 2, panel.y + 240);

        g.setFont(assets.getFont(16, true));
        g.setColor(UIUtils.SUCCESS);
        String xpTxt = "+" + xpEarned + " XP!";
        fm = g.getFontMetrics();
        g.drawString(xpTxt, panel.x + (panel.width - fm.stringWidth(xpTxt)) / 2, panel.y + 286);

        UIUtils.drawButton(g, replayBtn, Lang.tr("Play Again", "다시 하기", "العب مجدداً", "再玩一次", "Jugar de nuevo"), fBtn, UIUtils.ACCENT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
        UIUtils.drawButton(g, menuBtn, Lang.tr("Main Menu", "메인 메뉴", "القائمة الرئيسية", "主菜单", "Menú principal"), fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
    }
}
