package vn.astrax.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public final class RankManager {
    public record Rank(String id, String label, String name, String material, String oraxen, String model,
                       String kit, String permission, String price,
                       List<String> description, List<String> perks) {}

    private final AstraxMenuPlugin pl;
    private final KitReader kits;
    private YamlConfiguration cfg = new YamlConfiguration();
    private final LinkedHashMap<String, Rank> ranks = new LinkedHashMap<>();

    public RankManager(AstraxMenuPlugin pl) {
        this.pl = pl;
        this.kits = new KitReader(pl);
    }

    public void load() {
        cfg = YamlConfiguration.loadConfiguration(new File(pl.getDataFolder(), "ranks.yml"));
        ranks.clear();
        kits.setFolder(cfg.getString("settings.kits-folder", "PlayerKits2/kits"));
        kits.clear();
        ConfigurationSection root = cfg.getConfigurationSection("ranks");
        if (root == null) {
            pl.getLogger().warning("ranks.yml không có mục 'ranks'.");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            String lid = id.toLowerCase(Locale.ROOT);
            ranks.put(lid, new Rank(lid,
                    s.getString("label", id),
                    s.getString("name", "&f" + id),
                    s.getString("icon", "PAPER"),
                    s.getString("oraxen", ""),
                    s.getString("model", "oraxen:rank_" + lid),
                    s.getString("kit", ""),
                    s.getString("permission", "group." + lid),
                    s.getString("price", ""),
                    s.getStringList("description"),
                    s.getStringList("perks")));
        }
    }

    // ---------------------------------------------------------------- tra cứu

    public Collection<Rank> all() {
        return ranks.values();
    }

    public Rank find(String id) {
        return id == null ? null : ranks.get(id.trim().toLowerCase(Locale.ROOT));
    }

    public boolean has(Player p, Rank r) {
        return p.hasPermission(r.permission());
    }

    /** Rank cao nhất người chơi đang có (thứ tự khai báo trong ranks.yml: thấp -> cao). */
    public Rank highest(Player p) {
        Rank best = null;
        for (Rank r : ranks.values()) if (has(p, r)) best = r;
        return best;
    }

    public String defaultName() {
        return cfg.getString("settings.default-rank-name", "&7Thành viên");
    }

    public String display(Rank r) {
        return r == null ? defaultName() : "&f" + r.name();
    }

    private String t(String key, String... kv) {
        String s = cfg.getString("texts." + key, "");
        for (int i = 0; i + 1 < kv.length; i += 2) s = s.replace("{" + kv[i] + "}", kv[i + 1]);
        return s;
    }

    private String cooldownText(KitReader.Kit k) {
        if (k.oneTime()) return t("kit-once");
        if (k.cooldown() <= 0) return t("kit-nocd");
        return Text.fmtTime((long) k.cooldown());
    }

    private void fill(Inventory inv, Player p) {
        ItemStack f = Items.make(pl, p, pl.getConfig().getString("gui.filler-material", "BLACK_STAINED_GLASS_PANE"),
                null, pl.getConfig().getString("gui.filler-model", ""), 0,
                pl.getConfig().getString("gui.filler-name", " "), null, 1, false, null);
        for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) == null) inv.setItem(i, f);
    }

    // ---------------------------------------------------------------- vật phẩm rank

    private ItemStack rankItem(Player p, Rank r) {
        boolean owned = has(p, r);
        boolean current = r == highest(p);
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.addAll(r.description());
        if (!r.description().isEmpty()) lore.add(" ");
        if (!r.perks().isEmpty()) {
            lore.add(t("perks-title"));
            lore.addAll(r.perks());
            lore.add(" ");
        }
        if (!r.price().isBlank()) lore.add(t("price-line", "price", r.price()));
        if (!r.kit().isBlank()) {
            lore.add(t("kit-line", "kit", r.kit()));
            KitReader.Kit k = kits.read(r.kit());
            if (k != null) lore.add(t("kit-cooldown", "cooldown", cooldownText(k)));
        }
        lore.add(" ");
        lore.add(current ? t("status-current") : owned ? t("status-owned") : t("status-locked"));
        lore.add(" ");
        lore.add(t("hint-left"));
        if (!r.kit().isBlank()) lore.add(t("hint-right", "kit", r.kit()));
        return Items.make(pl, p, r.material(), r.oraxen(), r.model(), 0, r.name(), lore, 1, current, null);
    }

    // ---------------------------------------------------------------- danh sách rank

    public void openList(Player p) {
        int rows = Math.max(3, Math.min(6, cfg.getInt("settings.rows", 4)));
        ListHolder h = new ListHolder(rows);
        Inventory inv = Bukkit.createInventory(h, rows * 9,
                Text.raw(Placeholders.apply(pl, p, cfg.getString("settings.title", "Rank"))));
        h.inv = inv;

        List<String> hl = new ArrayList<>();
        hl.add(t("header-current"));
        hl.add(" ");
        hl.add(t("header-hint-1"));
        hl.add(t("header-hint-2"));
        inv.setItem(4, Items.make(pl, p, "PLAYER_HEAD", null, null, 0, t("header-name"), hl, 1, false, "{player}"));

        List<Integer> slots = cfg.getIntegerList("settings.slots");
        int i = 0;
        for (Rank r : ranks.values()) {
            if (i >= slots.size()) break;
            int slot = slots.get(i++);
            if (slot < 0 || slot >= rows * 9) continue;
            inv.setItem(slot, rankItem(p, r));
            h.bySlot.put(slot, r);
        }

        String back = cfg.getString("settings.back-menu", "");
        if (back != null && !back.isBlank()) {
            h.backSlot = (rows - 1) * 9;
            inv.setItem(h.backSlot, Items.make(pl, p, "ARROW", null, null, 0, t("back"), null, 1, false, null));
        }
        h.closeSlot = (rows - 1) * 9 + 4;
        inv.setItem(h.closeSlot, Items.make(pl, p, "BARRIER", null, null, 0, t("close"), null, 1, false, null));
        fill(inv, p);
        p.openInventory(inv);
    }

    // ---------------------------------------------------------------- chi tiết một rank

    public void openDetail(Player p, Rank r) {
        DetailHolder h = new DetailHolder(r);
        String title = cfg.getString("settings.detail-title", "&8Rank: {rank}").replace("{rank}", r.name());
        Inventory inv = Bukkit.createInventory(h, 36, Text.raw(Placeholders.apply(pl, p, title)));
        h.inv = inv;
        inv.setItem(4, rankItem(p, r));

        List<Integer> slots = cfg.getIntegerList("settings.detail-slots");
        if (slots.isEmpty()) slots = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);
        boolean hasKit = !r.kit().isBlank();
        if (!hasKit) {
            inv.setItem(22, Items.make(pl, p, "BARRIER", null, null, 0, t("no-kit"), null, 1, false, null));
        } else if (!cfg.getBoolean("settings.show-kit-contents", true)) {
            inv.setItem(22, Items.make(pl, p, "BARRIER", null, null, 0, t("preview-off"), null, 1, false, null));
        } else {
            KitReader.Kit k = kits.read(r.kit());
            if (k == null) {
                inv.setItem(22, Items.make(pl, p, "BARRIER", null, null, 0,
                        t("kit-unreadable", "kit", r.kit()), null, 1, false, null));
            } else if (k.items().isEmpty()) {
                inv.setItem(22, Items.make(pl, p, "BARRIER", null, null, 0, t("empty-title"), null, 1, false, null));
            } else {
                int n = 0;
                for (ItemStack it : k.items()) {
                    if (n >= slots.size()) break;
                    int slot = slots.get(n);
                    if (slot >= 0 && slot < 36) inv.setItem(slot, it.clone());
                    n++;
                }
                int more = k.items().size() - n;
                if (more > 0) {
                    inv.setItem(26, Items.make(pl, p, "PAPER", null, null, 0,
                            t("more-items", "n", String.valueOf(more)), null, 1, false, null));
                }
            }
            h.hasClaim = true;
            inv.setItem(31, Items.make(pl, p, cfg.getString("settings.claim-material", "EMERALD"), null, null, 0,
                    t("claim-name", "kit", r.kit()), List.of(t("claim-lore")), 1, true, null));
        }
        inv.setItem(27, Items.make(pl, p, "ARROW", null, null, 0, t("back-list"), null, 1, false, null));
        inv.setItem(35, Items.make(pl, p, "BARRIER", null, null, 0, t("close"), null, 1, false, null));
        fill(inv, p);
        p.openInventory(inv);
    }

    // ---------------------------------------------------------------- nhận kit

    public void claim(Player p, Rank r) {
        if (r.kit().isBlank()) {
            pl.messages().send(p, "kit-missing");
            return;
        }
        if (cfg.getBoolean("settings.require-owned-to-claim", true) && !has(p, r)) {
            pl.messages().send(p, "rank-not-owned", "rank", Text.plain(r.label()));
            return;
        }
        p.closeInventory();
        String cmd = cfg.getString("settings.kit-command", "kit {kit}").replace("{kit}", r.kit());
        p.performCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
    }

    // ---------------------------------------------------------------- holder

    private final class ListHolder implements AstraxHolder {
        private Inventory inv;
        private final Map<Integer, Rank> bySlot = new HashMap<>();
        private int backSlot = -1;
        private int closeSlot = -1;

        ListHolder(int rows) {}

        @Override
        public Inventory getInventory() {
            return inv;
        }

        @Override
        public void onClick(Player p, int slot, ClickType click) {
            if (slot == closeSlot) {
                p.closeInventory();
                return;
            }
            if (slot == backSlot) {
                pl.menus().open(p, cfg.getString("settings.back-menu", "main"));
                return;
            }
            Rank r = bySlot.get(slot);
            if (r == null) return;
            if (click.isRightClick()) claim(p, r);
            else if (click.isLeftClick()) openDetail(p, r);
        }
    }

    private final class DetailHolder implements AstraxHolder {
        private Inventory inv;
        private final Rank rank;
        private boolean hasClaim;

        DetailHolder(Rank rank) {
            this.rank = rank;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }

        @Override
        public void onClick(Player p, int slot, ClickType click) {
            if (slot == 35) p.closeInventory();
            else if (slot == 27) openList(p);
            else if (slot == 31 && hasClaim) claim(p, rank);
        }
    }
}
