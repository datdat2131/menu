package vn.astrax.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

/** Đọc kit của PlayerKits2 (plugins/PlayerKits2/kits/<tên>.yml). Chỉ đọc, không sửa file. */
public final class KitReader {
    public record Kit(String name, double cooldown, boolean oneTime, List<ItemStack> items) {}

    private record Cached(long mtime, Kit kit) {}

    private final AstraxMenuPlugin pl;
    private final Map<String, Cached> cache = new HashMap<>();
    private String folder = "PlayerKits2/kits";

    public KitReader(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    public void setFolder(String folder) {
        this.folder = (folder == null || folder.isBlank()) ? "PlayerKits2/kits" : folder.trim();
    }

    public void clear() {
        cache.clear();
    }

    public Kit read(String name) {
        if (name == null || name.isBlank()) return null;
        File dir = new File(pl.getDataFolder().getParentFile(), folder);
        File f = new File(dir, name + ".yml");
        if (!f.isFile()) f = new File(dir, name.toLowerCase(Locale.ROOT) + ".yml");
        if (!f.isFile()) return null;
        Cached c = cache.get(f.getPath());
        if (c != null && c.mtime() == f.lastModified()) return c.kit();
        Kit k = parse(name, f);
        cache.put(f.getPath(), new Cached(f.lastModified(), k));
        return k;
    }

    private Kit parse(String name, File f) {
        try {
            YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
            double cd = y.getDouble("cooldown", 0);
            boolean once = y.getBoolean("one_time", false);
            ConfigurationSection sec = y.getConfigurationSection("items");
            List<Object[]> found = new ArrayList<>();
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    ConfigurationSection e = sec.getConfigurationSection(key);
                    if (e == null) continue;
                    try {
                        ItemStack it = e.getItemStack("original");
                        if (it == null) continue;
                        int order = e.getInt("preview_slot", Integer.MAX_VALUE);
                        found.add(new Object[]{order, it});
                    } catch (Exception ex) {
                        pl.getLogger().warning("Không đọc được item " + key + " của kit " + name + ": " + ex.getMessage());
                    }
                }
            }
            found.sort(Comparator.comparingInt(o -> (Integer) o[0]));
            List<ItemStack> items = new ArrayList<>();
            for (Object[] o : found) items.add((ItemStack) o[1]);
            return new Kit(name, cd, once, items);
        } catch (Exception ex) {
            pl.getLogger().warning("Không đọc được kit " + name + ": " + ex.getMessage());
            return null;
        }
    }
}
