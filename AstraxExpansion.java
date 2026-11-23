package vn.astrax.menu;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/** %astraxmenu_rank% %astraxmenu_rank_id% %astraxmenu_rank_label% %astraxmenu_rank_glyph% %astraxmenu_rank_model% */
public final class AstraxExpansion extends PlaceholderExpansion {
    private final AstraxMenuPlugin pl;

    public AstraxExpansion(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    @Override
    public String getIdentifier() {
        return "astraxmenu";
    }

    @Override
    public String getAuthor() {
        return "Astrax";
    }

    @Override
    public String getVersion() {
        return pl.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offline, String params) {
        if (offline == null) return "";
        Player p = offline.getPlayer();
        if (p == null) return "";
        RankManager rm = pl.ranks();
        RankManager.Rank r = rm.highest(p);
        switch (params.toLowerCase()) {
            case "rank":
                return Text.glyphs(rm.display(r));
            case "rank_id":
                return r == null ? "" : r.id();
            case "rank_label":
                return r == null ? Text.plain(rm.defaultName()) : Text.plain(r.label());
            case "rank_glyph":
                return r == null ? "" : Text.glyphs("{g:" + r.id() + "}");
            case "rank_model":
                return r == null ? "" : r.model();
            default:
                return null;
        }
    }
}
