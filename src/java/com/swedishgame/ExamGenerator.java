package com.swedishgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class ExamGenerator {
    private static class ConvEntry {
        final int minLevel;
        final int maxLevel;
        final String prompt;
        final List<String> options;
        final String answer;
        final String explanation;

        ConvEntry(int minLevel, int maxLevel, String prompt, List<String> options, String answer, String explanation) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.prompt = prompt;
            this.options = options;
            this.answer = answer;
            this.explanation = explanation;
        }
    }

    private static final List<ConvEntry> DAILY_CONVERSATIONS = Arrays.asList(
        new ConvEntry(1, 15,
            "Astrid: Vad är det här? (Holding an apple)  /  Jag: Det är ___ äpple.",
            Arrays.asList("ett", "en", "den", "de"), "ett",
            "äpple is an ett-word -> ett äpple."),
        new ConvEntry(1, 15,
            "Erik: Har du med dig något att läsa?  /  Jag: Ja, jag har ___ bok i väskan.",
            Arrays.asList("en", "ett", "det", "de"), "en",
            "bok is an en-word -> en bok."),
        new ConvEntry(1, 15,
            "Freja: Vad är det där på bordet? (An umbrella)  /  Jag: Det är ___ paraply.",
            Arrays.asList("ett", "en", "den", "mina"), "ett",
            "paraply is an ett-word -> ett paraply."),
        new ConvEntry(1, 15,
            "Barista: Hej! Vad ___ du? (Asking your name)  /  Jag: Hej, jag heter Erik.",
            Arrays.asList("heter", "bor", "har", "går"), "heter",
            "Vad heter du? asks someone's name."),
        new ConvEntry(1, 15,
            "Linnéa: Hur ___ du idag? (Asking how you are)  /  Jag: Tack, bara bra!",
            Arrays.asList("mår", "heter", "bor", "ser"), "mår",
            "Hur mår du? means 'How are you?'."),
        new ConvEntry(2, 18,
            "Turist: Ursäkta, ___ ligger stationen? (Where is it?)  /  Jag: Rakt fram till höger.",
            Arrays.asList("var", "vart", "vem", "när"), "var",
            "Static location uses 'var'."),
        new ConvEntry(3, 20,
            "Oskar: Var är kaffet?  /  Jag: ___ står på bordet. (The coffee)",
            Arrays.asList("Kaffet", "Kaffen", "Kaffena", "Kaffor"), "Kaffet",
            "ett kaffe -> definite singular is Kaffet."),
        new ConvEntry(3, 20,
            "Astrid: Gillar du huset?  /  Jag: Ja, ___ är jättefint! (The house)",
            Arrays.asList("huset", "husen", "husar", "hust"), "huset",
            "ett hus -> definite singular is huset."),
        new ConvEntry(4, 22,
            "Lärare: Talar ni svenska? (Yes, we do)  /  Jag: Ja, ___ talar lite svenska.",
            Arrays.asList("vi", "ni", "de", "hon"), "vi",
            "vi = we."),
        new ConvEntry(5, 25,
            "Barista: Vad dricker du på morgonen? [dricka]  /  Jag: Jag ___ alltid kaffe.",
            Arrays.asList("dricker", "drickar", "drick", "drack"), "dricker",
            "Present tense of dricka is dricker."),
        new ConvEntry(5, 25,
            "Kock: Vad äter du till lunch? [äta]  /  Jag: Jag ___ köttbullar idag!",
            Arrays.asList("äter", "ätar", "åt", "ätit"), "äter",
            "Present tense of äta is äter."),
        new ConvEntry(6, 30,
            "Erik: Vad gör du idag? (V2 word order!)  /  Jag: Idag ___ jag svenska.",
            Arrays.asList("pluggar", "jag pluggar", "plugga", "pluggade"), "pluggar",
            "V2 rule: the verb stays in position 2 -> Idag pluggar jag."),
        new ConvEntry(7, 30,
            "Barista: Vill du ha mjölk i kaffet? (No, I don't drink milk)  /  Jag: Nej, jag dricker ___ mjölk.",
            Arrays.asList("inte", "nej", "aldrig inte", "ingen"), "inte",
            "In main clauses, 'inte' goes directly after the verb."),
        new ConvEntry(8, 30,
            "Vän: ___ du i Stockholm? (Do you live in Stockholm?) [bo]  /  Jag: Ja, på Södermalm!",
            Arrays.asList("Bor", "Bo", "Bodde", "Gör"), "Bor",
            "Yes/No questions start with the finite verb: Bor du...?"),
        new ConvEntry(9, 35,
            "Passagerare: ___ går nästa tåg? (When?)  /  Guide: Om fem minuter.",
            Arrays.asList("När", "Vem", "Var", "Vad"), "När",
            "När asks 'when'."),
        new ConvEntry(10, 35,
            "Kund: Hur mycket ___ en kanelbulle? [kosta]  /  Bagare: Den kostar trettio kronor.",
            Arrays.asList("kostar", "koster", "kosta", "kostade"), "kostar",
            "kosta -> kostar (present tense)."),
        new ConvEntry(11, 40,
            "Astrid: Hur var äpplet? (It was tasty [god])  /  Jag: Det var jätte___!",
            Arrays.asList("gott", "god", "goda", "bättre"), "gott",
            "ett äpple / det takes the ett-form of god -> gott."),
        new ConvEntry(12, 40,
            "Vän: Hur många böcker har du? (Two books [bok])  /  Jag: Jag har två ___.",
            Arrays.asList("böcker", "bokar", "boker", "boken"), "böcker",
            "Irregular plural of en bok is böcker."),
        new ConvEntry(14, 45,
            "Oskar: Är det där din väska? (Yes, my bag [en väska])  /  Jag: Ja, det är ___ väska.",
            Arrays.asList("min", "mitt", "mina", "mig"), "min",
            "en väska uses the en-possessive: min väska."),
        new ConvEntry(14, 45,
            "Freja: Är det där ditt hus? (Yes, my house [ett hus])  /  Jag: Ja, det är ___ hus.",
            Arrays.asList("mitt", "min", "mina", "mig"), "mitt",
            "ett hus uses the ett-possessive: mitt hus."),
        new ConvEntry(15, 50,
            "Erik: Kan du hjälpa ___ med läxan? (me)  /  Jag: Självklart!",
            Arrays.asList("mig", "jag", "min", "mitt"), "mig",
            "Object pronoun for jag is mig."),
        new ConvEntry(16, 55,
            "Linnéa: Vad vill du göra nu? (I want to have fika)  /  Jag: Jag vill ___ fika!",
            Arrays.asList("ta", "tar", "att ta", "tog"), "ta",
            "Modal verb vill takes the bare infinitive without 'att' -> vill ta."),
        new ConvEntry(17, 55,
            "Vän: Hur blir vädret imorgon? (Prediction: it will rain)  /  Jag: Det ___ att regna.",
            Arrays.asList("kommer", "ska", "vill", "måste"), "kommer",
            "Weather predictions use 'kommer att' + infinitive."),
        new ConvEntry(18, 60,
            "Kollega: Vad gjorde du igår kväll? [laga - Group 1]  /  Jag: Igår ___ jag köttbullar.",
            Arrays.asList("lagade", "lagar", "laga", "lagat"), "lagade",
            "Group 1 past tense ends in -ade -> lagade."),
        new ConvEntry(19, 60,
            "Astrid: Köpte du boken? [köpa - Group 2]  /  Jag: Ja, jag ___ den igår.",
            Arrays.asList("köpte", "köpde", "köper", "köpt"), "köpte",
            "Voiceless stem consonant p takes -te -> köpte."),
        new ConvEntry(20, 65,
            "Erik: Vad åt du till middag igår? [äta - irregular]  /  Jag: Jag ___ fisk och potatis.",
            Arrays.asList("åt", "äter", "ätit", "ätade"), "åt",
            "Irregular past tense of äta is åt."),
        new ConvEntry(21, 65,
            "Guide: Har du ___ i Stockholm förut? [vara]  /  Jag: Ja, jag var här i somras!",
            Arrays.asList("varit", "var", "är", "vara"), "varit",
            "Perfect tense uses har + supinum -> har varit."),
        new ConvEntry(22, 70,
            "Vän: Var är du just nu? (At work)  /  Jag: Jag är ___ jobbet.",
            Arrays.asList("på", "i", "till", "från"), "på",
            "'At work' in Swedish is 'på jobbet'."),
        new ConvEntry(23, 70,
            "Kollega: När börjar mötet? (In ten minutes)  /  Jag: Det börjar ___ tio minuter.",
            Arrays.asList("om", "i", "på", "sedan"), "om",
            "Future 'in [duration]' uses 'om'."),
        new ConvEntry(25, 79,
            "Erik: Varför stannar du hemma? (BIFF subclause!)  /  Jag: Eftersom jag ___ mår bra.",
            Arrays.asList("inte", "mår inte", "nej", "aldrig inte"), "inte",
            "BIFF rule: in a subclause, 'inte' comes BEFORE the first verb."),
        new ConvEntry(31, 79,
            "Lärare: Vems bok läser Erik? (His OWN book)  /  Jag: Erik läser ___ bok.",
            Arrays.asList("sin", "hans", "sitt", "sina"), "sin",
            "sin/sitt/sina refers back to the subject Erik; en bok -> sin bok."),
        new ConvEntry(32, 79,
            "Vän: Är tåget snabbt? (Faster than the bus)  /  Jag: Ja, tåget är snabbare ___ bussen.",
            Arrays.asList("än", "som", "och", "men"), "än",
            "Comparisons use 'än' ('than') -> snabbare än."),
        new ConvEntry(34, 79,
            "Astrid: Vad ___ du om filmen? (Asking your opinion)  /  Jag: Jag tyckte den var jättebra!",
            Arrays.asList("tyckte", "trodde", "tänkte", "visste"), "tyckte",
            "tycka is used for personal opinions."),
        new ConvEntry(35, 79,
            "Turist: ___ det ett bra kafé här? (Is there...?)  /  Jag: Ja, runt hörnet!",
            Arrays.asList("Finns", "Har", "Är", "Blir"), "Finns",
            "'There is / Is there' uses 'Det finns / Finns det'."),
        new ConvEntry(38, 79,
            "Mamma: När kommer du ___? (Coming home - motion)  /  Jag: Jag kommer klockan sex!",
            Arrays.asList("hem", "hemma", "här", "där"), "hem",
            "Motion toward home uses 'hem' (static is 'hemma')."),
        new ConvEntry(41, 79,
            "Kund: När ___ butiken? (When is the shop opened?) [öppna]  /  Vakt: Klockan tio.",
            Arrays.asList("öppnas", "öppnar", "öppnade", "öppna"), "öppnas",
            "Swedish s-passive attaches -s -> öppnas."),
        new ConvEntry(46, 79,
            "Kund: ___ jag kunna få en kaffe, tack? (Polite request)  /  Barista: Absolut!",
            Arrays.asList("Skulle", "Måste", "Har", "Är"), "Skulle",
            "'Skulle jag kunna få...?' is the classic polite Swedish request."),
        new ConvEntry(51, 79,
            "Vän: Hur var kaffet? (Just right!)  /  Jag: Det var helt ___ varmt.",
            Arrays.asList("lagom", "mycket", "inte", "aldrig"), "lagom",
            "'lagom' means 'just the right amount'."),
        new ConvEntry(51, 79,
            "Kollega: Ska vi ta en ___? (Coffee break)  /  Jag: Ja, gärna med kanelbulle!",
            Arrays.asList("fika", "läxa", "karta", "biljett"), "fika",
            "'ta en fika' means to have a Swedish coffee and pastry break.")
    );

    private ExamGenerator() {}

    public static List<Task> generate(List<Level> allLevels, Glossary glossary, ExamSpec spec) {
        Random rng = new Random();
        List<Level> range = new ArrayList<>();
        for (Level lv : allLevels) {
            if (lv.id >= spec.startLevel && lv.id <= spec.endLevel && lv.id <= 79) {
                range.add(lv);
            }
        }
        if (range.isEmpty()) {
            for (Level lv : allLevels) {
                if (lv.id <= 79) {
                    range.add(lv);
                }
            }
        }

        List<Task> buildPool = new ArrayList<>();
        List<Task> convPool = new ArrayList<>();
        Set<String> seenConvPrompts = new HashSet<>();
        List<String[]> vocabPool = new ArrayList<>();
        Set<String> seenVocabKeys = new HashSet<>();

        for (ConvEntry c : DAILY_CONVERSATIONS) {
            if (c.minLevel <= spec.endLevel && c.maxLevel >= spec.startLevel && seenConvPrompts.add(c.prompt)) {
                convPool.add(new Task(
                    "fill",
                    "[" + Lang.tr("Conversation", "대화", "محادثة", "对话", "Conversación") + "] " + c.prompt,
                    c.options,
                    c.answer,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    c.explanation
                ));
            }
        }

        if (convPool.size() < 5) {
            for (ConvEntry c : DAILY_CONVERSATIONS) {
                if (c.minLevel <= spec.endLevel && seenConvPrompts.add(c.prompt)) {
                    convPool.add(new Task(
                        "fill",
                        "[" + Lang.tr("Conversation", "대화", "محادثة", "对话", "Conversación") + "] " + c.prompt,
                        c.options,
                        c.answer,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        c.explanation
                    ));
                }
            }
        }

        for (Level lv : range) {
            for (String[] kv : lv.vocab) {
                if (kv.length == 2 && seenVocabKeys.add(kv[0])) {
                    String tr = glossary != null ? glossary.translate(kv[0]) : null;
                    vocabPool.add(new String[]{kv[0], tr != null ? tr : kv[1]});
                }
            }
            for (Entity e : lv.entities) {
                for (Task t : e.tasks) {
                    if ("build".equals(t.type)) {
                        buildPool.add(new Task(
                            "build",
                            "[" + Lang.tr("Sentence Build", "문장 만들기", "تكوين جملة", "组句", "Crear frase") + "] " + t.prompt,
                            t.options,
                            t.answer,
                            t.tokens,
                            t.distractors,
                            t.explanation
                        ));
                    }
                }
            }
        }

        List<String[]> globalVocab = new ArrayList<>();
        Set<String> globalKeys = new HashSet<>();
        if (glossary != null) {
            for (Map.Entry<String, String> entry : glossary.getAllEntries().entrySet()) {
                if (globalKeys.add(entry.getKey())) {
                    globalVocab.add(new String[]{entry.getKey(), entry.getValue()});
                }
            }
        }

        if (vocabPool.size() < 6) {
            List<String[]> extra = new ArrayList<>(globalVocab);
            Collections.shuffle(extra, rng);
            for (String[] kv : extra) {
                if (seenVocabKeys.add(kv[0])) {
                    vocabPool.add(kv);
                }
                if (vocabPool.size() >= 12) {
                    break;
                }
            }
        }

        List<Task> vocabTasks = new ArrayList<>();
        List<String[]> shuffledVocab = new ArrayList<>(vocabPool);
        Collections.shuffle(shuffledVocab, rng);
        for (int i = 0; i < shuffledVocab.size(); i++) {
            String[] pair = shuffledVocab.get(i);
            boolean svToNative = (i % 2 == 0);
            vocabTasks.add(makeVocabQuestion(pair, globalVocab, svToNative, rng));
            if (spec.questionCount > 20 || vocabTasks.size() < 6) {
                vocabTasks.add(makeVocabQuestion(pair, globalVocab, !svToNative, rng));
            }
        }

        Collections.shuffle(buildPool, rng);
        Collections.shuffle(convPool, rng);
        Collections.shuffle(vocabTasks, rng);

        int total = spec.questionCount;
        int wantBuild = total / 3;
        int wantConv = total / 3;
        int wantVocab = total - wantBuild - wantConv;

        List<Task> result = new ArrayList<>();
        addUpTo(result, buildPool, wantBuild, rng);
        addUpTo(result, convPool, wantConv, rng);
        addUpTo(result, vocabTasks, wantVocab, rng);

        List<Task> fallback = new ArrayList<>();
        fallback.addAll(convPool);
        fallback.addAll(buildPool);
        fallback.addAll(vocabTasks);
        Collections.shuffle(fallback, rng);
        int fi = 0;
        while (result.size() < total && !fallback.isEmpty()) {
            result.add(fallback.get(fi % fallback.size()));
            fi++;
        }

        Collections.shuffle(result, rng);
        return result;
    }

    private static void addUpTo(List<Task> dest, List<Task> pool, int count, Random rng) {
        if (pool.isEmpty() || count <= 0) {
            return;
        }
        int added = 0;
        int idx = 0;
        while (added < count) {
            if (idx >= pool.size()) {
                Collections.shuffle(pool, rng);
                idx = 0;
            }
            dest.add(pool.get(idx));
            idx++;
            added++;
        }
    }

    private static Task makeVocabQuestion(String[] target, List<String[]> allVocab, boolean svToNative, Random rng) {
        String sv = target[0];
        String nativeWord = target[1];
        List<String> opts = new ArrayList<>();
        String correct = svToNative ? nativeWord : sv;
        opts.add(correct);

        List<String[]> copy = new ArrayList<>(allVocab);
        Collections.shuffle(copy, rng);
        for (String[] cand : copy) {
            if (opts.size() >= 4) {
                break;
            }
            String val = svToNative ? cand[1] : cand[0];
            if (!opts.contains(val)) {
                opts.add(val);
            }
        }
        while (opts.size() < 4) {
            opts.add(svToNative ? "other" : "annat");
        }

        String vTag = "[" + Lang.tr("Vocabulary", "단어", "مفردات", "词汇", "Vocabulario") + "] ";
        String prompt = svToNative
            ? vTag + Lang.tr("What does '" + sv + "' mean?", "'" + sv + "'의 뜻은 무엇인가요?", "ما معنى '" + sv + "'؟", "'" + sv + "' 是什么意思？", "¿Qué significa '" + sv + "'?")
            : vTag + Lang.tr("Which Swedish word means '" + nativeWord + "'?", "'" + nativeWord + "'을(를) 뜻하는 스웨덴어 단어는?", "أي كلمة سويدية تعني '" + nativeWord + "'؟", "哪个瑞典语词表示 '" + nativeWord + "'？", "¿Qué palabra sueca significa '" + nativeWord + "'?");
        String exp = "'" + sv + "' = '" + nativeWord + "'.";

        return new Task("choice", prompt, opts, correct, Collections.emptyList(), Collections.emptyList(), exp);
    }
}
