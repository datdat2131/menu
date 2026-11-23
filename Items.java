package vn.astrax.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tạo vật phẩm GUI. Hỗ trợ model: item_model (vd oraxen:rank_pro), custom-model-data, hoặc lấy thẳng item Oraxen. */
public final class Items {
    private Items() {}

    public static ItemStack of(AstraxMenuPlugin pl, Player viewer, ConfigurationSection s) {
        return make(pl, viewer,
                s.getString("material", "STONE"),
                s.getString("oraxen"),
                s.getString("model"),
                s.getInt("custom-model-data", 0),
                s.getString("name"),
                s.getStringList("lore"),
                s.getInt("amount", 1),
                s.getBoolean("glow", false),
                s.getString("skull-owner"));
    }

    public static ItemStack make(AstraxMenuPlugin pl, Player viewer, String material, String oraxenId,
                                 String model, int customModelData, String name, List<String> lore,
                                 int amount, boolean glow, String skullOwner) {
        int amt = Math.max(1, Math.min(64, amount));
        ItemStack it = null;
        if (oraxenId != null && !oraxenId.isBlank()) {
            it = oraxenItem(Placeholders.apply(pl, viewer, oraxenId.trim()));
            if (it == null) pl.getLogger().warning("Không tìm thấy item Oraxen '" + oraxenId + "' (đã cài Oraxen chưa?).");
        }
        if (it == null) {
            it = new ItemStack(parse(pl, material), amt);
        } else {
            it.setAmount(amt);
        }
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return it;
        if (name != null && !name.isBlank()) {
            meta.displayName(Text.c(Placeholders.apply(pl, viewer, name)));
        }
        if (lore != null && !lore.isEmpty()) {
            List<Component> l = new ArrayList<>();
            for (String line : lore) l.add(Text.c(Placeholders.apply(pl, viewer, line)));
            meta.lore(l);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        if (glow) meta.setEnchantmentGlintOverride(true);
        if (model != null) {
            String m = Placeholders.apply(pl, viewer, model).trim();
            if (!m.isEmpty() && !m.equalsIgnoreCase("none")) {
                NamespacedKey key = NamespacedKey.fromString(m.toLowerCase(Locale.ROOT));
                if (key != null) meta.setItemModel(key);
                else pl.getLogger().warning("model sai định dạng (cần namespace:key): " + m);
            }
        }
        if (customModelData > 0) meta.setCustomModelData(customModelData);
        if (meta instanceof SkullMeta sm && skullOwner != null && !skullOwner.isBlank()) {
            Player owner = Bukkit.getPlayerExact(Placeholders.apply(pl, viewer, skullOwner).trim());
            if (owner != null) sm.setOwningPlayer(owner);
        }
        it.setItemMeta(meta);
        return it;
    }

    private static Material parse(AstraxMenuPlugin pl, String name) {
        if (name != null) {
            Material m = Material.matchMaterial(name.trim().toUpperCase(Locale.ROOT));
            if (m != null && m.isItem() && !m.isAir()) return m;
        }
        pl.getLogger().warning("Vật phẩm không hợp lệ: '" + name + "' (dùng STONE thay thế).");
        return Material.STONE;
    }

    /** Lấy item Oraxen bằng reflection để plugin chạy được cả khi không có Oraxen. */
    private static ItemStack oraxenItem(String id) {
        try {
            Class<?> c = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            Object builder = c.getMethod("getItemById", String.class).invoke(null, id);
            if (builder == null) return null;
            Object built = builder.getClass().getMethod("build").invoke(builder);
            return built instanceof ItemStack st ? st : null;
        } catch (Throwable t) {
            return null;
        }
    }

    public static void sound(Player p, String key) {
        if (key == null || key.isBlank() || key.equalsIgnoreCase("none")) return;
        p.playSound(p.getLocation(), key.trim().toLowerCase(Locale.ROOT), 1f, 1f);
    }
}
