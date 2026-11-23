package vn.astrax.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.HashMap;
import java.util.Map;

public final class Text {
    private static final LegacyComponentSerializer SER =
            LegacyComponentSerializer.builder().character('&').hexColors().build();
    private static Map<String, String> glyphs = new HashMap<>();

    private Text() {}

    public static void setGlyphs(Map<String, String> g) {
        glyphs = g;
    }

    /** Đổi {g:id} thành ký tự glyph (resource pack). */
    public static String glyphs(String s) {
        if (s == null) return "";
        if (s.indexOf("{g:") < 0) return s;
        for (Map.Entry<String, String> e : glyphs.entrySet()) {
            s = s.replace("{g:" + e.getKey() + "}", e.getValue());
        }
        return s;
    }

    /** Dùng cho tên/lore vật phẩm: tắt chữ nghiêng mặc định. */
    public static Component c(String s) {
        return SER.deserialize(glyphs(s)).decoration(TextDecoration.ITALIC, false);
    }

    /** Dùng cho chat, tiêu đề, trang sách. */
    public static Component raw(String s) {
        return SER.deserialize(glyphs(s));
    }

    public static String plain(String s) {
        return PlainTextComponentSerializer.plainText().serialize(SER.deserialize(glyphs(s)));
    }

    public static String fmtTime(long sec) {
        if (sec <= 0) return "0 giây";
        long d = sec / 86400, h = (sec % 86400) / 3600, m = (sec % 3600) / 60, s = sec % 60;
        StringBuilder b = new StringBuilder();
        if (d > 0) b.append(d).append(" ngày ");
        if (h > 0) b.append(h).append(" giờ ");
        if (m > 0) b.append(m).append(" phút ");
        if (s > 0 && d == 0 && h == 0) b.append(s).append(" giây");
        return b.toString().trim();
    }
}
