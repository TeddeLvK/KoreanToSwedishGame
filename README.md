# Swemon — Ordmästarnas uppgång
**Quest for the Golden Meatball**

A cross-platform pixel-art RPG for learning Swedish (*Svenska*) from **English, Korean (한국어), Arabic (العربية), Chinese (中文), or Spanish (Español)**.

## Features
- **5 Selectable Languages:** Switch languages anytime from the bottom-right corner of the Main Menu (translates UI, glossary hover tooltips, vocabulary, exams, and mini games).
- **80 Pixel-Art Levels:** Explore Gamla Stan, Södermalm, KTH, and downtown Stockholm across 2 chapters, plus the Level 80 Royal Palace Grand Final Exam (*Slottets slutprov*).
- **3 Built-In Mini Games:**
  1. **Swedish Compound Words (*Sammansatta ord*):** Combine everyday words like `kaffe + kopp = kaffekopp`.
  2. **Vocabulary Drill (*Ordförråd*):** Match Swedish words with your chosen language.
  3. **Swedish Word Writing (*Skriva ord*):** Practice spelling with an in-app Swedish phone keyboard (includes **Å, Ä, Ö**).
- **Exams, Crowns & Badges:** Pass revision exams, earn Chapter Crowns, and score 100% on the Level 80 Grand Final to unlock the **Meatball God (*Köttbullegud*)** badge.
- **3 Independent Save Slots:** Automatically saved to `~/.swemon/`.

## Download & Install
Grab the latest build from the **[Releases](../../releases)** page:

- **macOS:** Open `Swemon-1.0.0.dmg` → drag **Swemon** to **Applications**. On first launch, if macOS blocks the app, go to **System Settings → Privacy & Security → Open Anyway**.
- **Windows:** Right-click `Swemon-Windows.zip` → **Extract All...** → open the extracted folder and double-click `Swemon.exe` (click **More info → Run anyway** on first launch).

## Controls
- **Move:** `WASD` or `Arrow Keys`
- **Interact / Talk / Exam:** `E`, `Space`, `Enter`, or **Click**
- **Level Guide:** `Tab` | **Level Select:** `L`
- **Main Menu:** `M` or `Esc` | **Fullscreen:** `F` or `F11`

## Build from Source
Requires JDK 17+:
```bash
make jar && java -jar Swemon.jar