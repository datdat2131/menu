package vn.astrax.menu;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class JoinListener implements Listener {
    private final AstraxMenuPlugin pl;
    private final NamespacedKey key;

    public JoinListener(AstraxMenuPlugin pl) {
        this.pl = pl;
        this.key = new NamespacedKey(pl, "join_menu");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ConfigurationSection j = pl.getConfig().getConfigurationSection("join");
        if (j == null) return;

        if (j.getBoolean("hint.enabled", false)) {
            p.sendMessage(Text.raw(Placeholders.apply(pl, p, j.getString("hint.message", ""))));
        }
        if (j.getBoolean("first-join-menu.enabled", false) && !p.hasPlayedBefore()) {
            String menu = j.getString("first-join-menu.menu", pl.getConfig().getString("main-menu", "main"));
            Bukkit.getScheduler().runTaskLater(pl, () -> {
                if (p.isOnline()) pl.menus().open(p, menu);
            }, 40L);
        }
        if (j.getBoolean("item.enabled", false)) {
            ConfigurationSection s = j.getConfigurationSection("item");
            if (s == null) return;
            int slot = Math.max(0, Math.min(8, s.getInt("slot", 8)));
            ItemStack cur = p.getInventory().getItem(slot);
            if (cur != null && !isJoinItem(cur)) return; // không đè vật phẩm khác
            ItemStack it = Items.of(pl, p, s);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING,
                        s.getString("menu", pl.getConfig().getString("main-menu", "main")));
                it.setItemMeta(meta);
            }
            p.getInventory().setItem(slot, it);
        }
    }

    private boolean isJoinItem(ItemStack it) {
        ItemMeta m = it.getItemMeta();
        return m != null && m.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack it = e.getItem();
        if (it == null) return;
        ItemMeta m = it.getItemMeta();
        if (m == null) return;
        String menu = m.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (menu == null) return;
        e.setCancelled(true);
        pl.menus().open(e.getPlayer(), menu);
    }
}
