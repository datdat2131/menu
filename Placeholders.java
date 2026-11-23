package vn.astrax.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Placeholders {
    private Placeholders() {}

    /**
     * Biến: {player} {online} {max} {rank} {rank_id} {rank_label} {rank_model}
     * và PlaceholderAPI (%...%) nếu có cài.
     */
    public static String apply(AstraxMenuPlugin pl, Player p, String s) {
        if (s == null) return "";
        s = s.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
             .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
        if (p == null) return s;
        RankManager rm = pl.ranks();
        RankManager.Rank r = rm.highest(p);
        s = s.replace("{player}", p.getName())
             .replace("{rank}", rm.display(r))
             .replace("{rank_id}", r == null ? "" : r.id())
             .replace("{rank_label}", r == null ? rm.defaultName() : r.label())
             .replace("{rank_model}", r == null ? "" : r.model());
        if (s.indexOf('%') >= 0 && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            s = Papi.set(p, s);
        }
        return s;
    }

    /** Tách riêng để không nạp lớp PlaceholderAPI khi server không có plugin đó. */
    private static final class Papi {
        static String set(Player p, String s) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, s);
        }
    }
}
