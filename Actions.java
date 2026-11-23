package vn.astrax.menu;

import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/** Chạy các dòng hành động dạng "[loại] nội dung". */
public final class Actions {
    private Actions() {}

    public static void run(AstraxMenuPlugin pl, Player p, List<String> actions) {
        for (String raw : actions) {
            String line = Placeholders.apply(pl, p, raw).trim();
            if (line.isEmpty()) continue;
            String type = "";
            String arg = line;
            if (line.startsWith("[")) {
                int end = line.indexOf(']');
                if (end > 0) {
                    type = line.substring(1, end).trim().toLowerCase(Locale.ROOT);
                    arg = line.substring(end + 1).trim();
                }
            }
            switch (type) {
                case "player" -> p.performCommand(stripSlash(arg));
                case "console" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripSlash(arg));
                case "open" -> pl.menus().open(p, arg);
                case "rank" -> {
                    if (arg.isEmpty() || arg.equalsIgnoreCase("list")) {
                        pl.ranks().openList(p);
                    } else {
                        RankManager.Rank r = pl.ranks().find(arg);
                        if (r == null) pl.messages().send(p, "rank-not-found", "rank", arg);
                        else pl.ranks().openDetail(p, r);
                    }
                }
                case "book" -> {
                    if (!pl.books().open(p, arg)) pl.messages().send(p, "book-not-found", "book", arg);
                }
                case "close" -> p.closeInventory();
                case "message" -> p.sendMessage(Text.raw(arg));
                case "sound" -> Items.sound(p, arg);
                case "cost-points" -> {
                    if (!costPoints(pl, p, arg)) return; // không đủ poin: dừng, không chạy các dòng sau
                }
                case "link" -> {
                    String[] parts = arg.split("\\|", 2);
                    String url = parts[0].trim();
                    String label = parts.length > 1 ? parts[1].trim() : url;
                    p.sendMessage(Text.raw(label).clickEvent(ClickEvent.openUrl(url)));
                }
                default -> pl.getLogger().warning("Hành động không hiểu: " + raw);
            }
        }
    }

    /** Trừ poin của người chơi. Trả về false (và báo lỗi) nếu không trừ được. */
    private static boolean costPoints(AstraxMenuPlugin pl, Player p, String arg) {
        int need;
        try {
            need = Integer.parseInt(arg.trim());
        } catch (NumberFormatException e) {
            pl.getLogger().warning("[cost-points] giá không phải số: " + arg);
            return false;
        }
        if (!PointsHook.available()) {
            pl.messages().send(p, "shop-no-points");
            return false;
        }
        int have = PointsHook.look(p.getUniqueId());
        if (have < need) {
            pl.messages().send(p, "shop-not-enough", "need", String.valueOf(need), "have", String.valueOf(Math.max(0, have)));
            return false;
        }
        if (!PointsHook.take(p.getUniqueId(), need)) {
            pl.messages().send(p, "shop-not-enough", "need", String.valueOf(need), "have", String.valueOf(have));
            return false;
        }
        return true;
    }

    private static String stripSlash(String s) {
        return s.startsWith("/") ? s.substring(1) : s;
    }
}
