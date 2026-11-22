package com.swedishgame;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class AssetLoader {
    private final Path assetsRoot;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private final Set<String> missingImages = new HashSet<>();
    private final Map<Lang, String> fontMap = new EnumMap<>(Lang.class);

    public AssetLoader(Path assetsRoot) {
        this.assetsRoot = assetsRoot;
        Set<String> available = new HashSet<>();
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            available.add(name);
        }
        fontMap.put(Lang.EN, pickFirst(available, new String[]{"Helvetica Neue", "Segoe UI", "Arial", "SansSerif"}));
        fontMap.put(Lang.ES, pickFirst(available, new String[]{"Helvetica Neue", "Segoe UI", "Arial", "SansSerif"}));
        fontMap.put(Lang.KO, pickFirst(available, new String[]{"Apple SD Gothic Neo", "Malgun Gothic", "NanumGothic", "Noto Sans CJK KR", "SansSerif"}));
        fontMap.put(Lang.ZH, pickFirst(available, new String[]{"PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", "Noto Sans CJK SC", "SansSerif"}));
        fontMap.put(Lang.AR, pickFirst(available, new String[]{"Geeza Pro", "Segoe UI", "Tahoma", "Arial", "Noto Naskh Arabic", "SansSerif"}));
    }

    private String pickFirst(Set<String> available, String[] preferred) {
        for (String p : preferred) {
            if (available.contains(p)) {
                return p;
            }
        }
        return Font.SANS_SERIF;
    }

    public Font getFont(int size, boolean bold) {
        String family = fontMap.getOrDefault(Lang.current(), Font.SANS_SERIF);
        return new Font(family, bold ? Font.BOLD : Font.PLAIN, size);
    }

    public Font getFontForLang(Lang lang, int size, boolean bold) {
        String family = fontMap.getOrDefault(lang, Font.SANS_SERIF);
        return new Font(family, bold ? Font.BOLD : Font.PLAIN, size);
    }

    public BufferedImage getImage(String relKey) {
        if (missingImages.contains(relKey)) {
            return null;
        }
        BufferedImage cached = imageCache.get(relKey);
        if (cached != null) {
            return cached;
        }
        File file = assetsRoot.resolve(relKey + ".png").toFile();
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    imageCache.put(relKey, img);
                    return img;
                }
            } catch (IOException ignored) {
            }
        }
        try (InputStream in = AssetLoader.class.getResourceAsStream("/Assets/" + relKey + ".png")) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    imageCache.put(relKey, img);
                    return img;
                }
            }
        } catch (IOException ignored) {
        }
        missingImages.add(relKey);
        return null;
    }
}
