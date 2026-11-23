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

public final class MenuManager {
    public record ItemDef(String id, List<Integer> slots, ConfigurationSection sec,
                          List<String> actions, List<String> left, List<String> right) {}

    public record MenuDef(String id, String title, int rows, boolean filler, List<ItemDef> items) {}

    private final AstraxMenuPlugin pl;
    private final Map<String, MenuDef> menus = new LinkedHashMap<>();

    public MenuManager(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    public void load() {
        menus.clear();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(new File(pl.getDataFolder(), "menus.yml"));
        ConfigurationSection root = y.getConfigurationSection("menus");
        if (root == null) {
            pl.getLogger().warning("menus.yml không có mục 'menus'.");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection m = root.getConfigurationSection(id);
            if (m == null) continue;
            int rows = Math.max(1, Math.min(6, m.getInt("rows", 3)));
            List<ItemDef> items = new ArrayList<>();
            ConfigurationSection is = m.getConfigurationSection("items");
            if (is != null) {
                for (String iid : is.getKeys(false)) {
                    ConfigurationSection s = is.getConfigurationSection(iid);
                    if (s == null) continue;
                    List<Integer> slots = new ArrayList<>(s.getIntegerList("slots"));
                    if (s.contains("slot")) slots.add(s.getInt("slot"));
                    items.add(new ItemDef(iid, slots, s, s.getStringList("actions"),
                            s.getStringList("left-actions"), s.getStringList("right-actions")));
                }
            }
            menus.put(id.toLowerCase(Locale.ROOT),
                    new MenuDef(id, m.getString("title", id), rows, m.getBoolean("filler", true), items));
        }
    }

    public Set<String> ids() {
        return menus.keySet();
    }

    public boolean exists(String id) {
        return id != null && menus.containsKey(id.trim().toLowerCase(Locale.ROOT));
    }

    public void open(Player p, String id) {
        MenuDef def = id == null ? null : menus.get(id.trim().toLowerCase(Locale.ROOT));
        if (def == null) {
            pl.messages().send(p, "menu-not-found", "menu", id == null ? "" : id);
            return;
        }
        MenuHolder holder = new MenuHolder(def);
        int size = def.rows() * 9;
        Inventory inv = Bukkit.createInventory(holder, size,
                Text.raw(Placeholders.apply(pl, p, def.title())));
        holder.inv = inv;
        for (ItemDef d : def.items()) {
            ItemStack stack = Items.of(pl, p, d.sec());
            for (int slot : d.slots()) {
                if (slot >= 0 && slot < size) {
                    inv.setItem(slot, stack);
                    holder.bySlot.put(slot, d);
                }
            }
        }
        if (def.filler()) {
            ItemStack f = Items.make(pl, p, pl.getConfig().getString("gui.filler-material", "BLACK_STAINED_GLASS_PANE"),
                    null, pl.getConfig().getString("gui.filler-model", ""), 0,
                    pl.getConfig().getString("gui.filler-name", " "), null, 1, false, null);
            for (int i = 0; i < size; i++) if (inv.getItem(i) == null) inv.setItem(i, f);
        }
        p.openInventory(inv);
    }

    /** Kiểm tra liên kết giữa các menu / sách / rank. */
    public List<String> check() {
        List<String> problems = new ArrayList<>();
        for (MenuDef d : menus.values()) {
            for (ItemDef it : d.items()) {
                List<String> all = new ArrayList<>(it.actions());
                all.addAll(it.left());
                all.addAll(it.right());
                for (String a : all) {
                    String t = a.trim();
                    if (t.regionMatches(true, 0, "[open]", 0, 6)) {
                        String target = t.substring(6).trim();
                        if (!exists(target)) problems.add(d.id() + "/" + it.id() + ": menu '" + target + "' không tồn tại");
                    } else if (t.regionMatches(true, 0, "[book]", 0, 6)) {
                        String target = t.substring(6).trim();
                        if (!pl.books().exists(target)) problems.add(d.id() + "/" + it.id() + ": sách '" + target + "' không tồn tại");
                    } else if (t.regionMatches(true, 0, "[rank]", 0, 6)) {
                        String target = t.substring(6).trim();
                        if (!target.isEmpty() && !target.equalsIgnoreCase("list") && pl.ranks().find(target) == null)
                            problems.add(d.id() + "/" + it.id() + ": rank '" + target + "' không tồn tại");
                    }
                }
            }
        }
        return problems;
    }

    private final class MenuHolder implements AstraxHolder {
        private final MenuDef def;
        private Inventory inv;
        private final Map<Integer, ItemDef> bySlot = new HashMap<>();

        MenuHolder(MenuDef def) {
            this.def = def;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }

        @Override
        public void onClick(Player p, int slot, ClickType click) {
            ItemDef d = bySlot.get(slot);
            if (d == null) return;
            List<String> acts = new ArrayList<>(d.actions());
            if (click.isRightClick()) acts.addAll(d.right());
            else if (click.isLeftClick()) acts.addAll(d.left());
            Actions.run(pl, p, acts);
        }
    }
}
