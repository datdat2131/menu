package vn.astrax.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuiListener implements Listener {
    private final AstraxMenuPlugin pl;
    private final Map<UUID, Long> last = new HashMap<>();

    public GuiListener(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof AstraxHolder holder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != top) return;
        long now = System.currentTimeMillis();
        long cd = pl.getConfig().getLong("gui.click-cooldown-ms", 250L);
        Long prev = last.get(p.getUniqueId());
        if (prev != null && now - prev < cd) return;
        last.put(p.getUniqueId(), now);
        Items.sound(p, pl.getConfig().getString("gui.click-sound", "ui.button.click"));
        final int slot = e.getSlot();
        final ClickType click = e.getClick();
        // chạy ở tick sau để việc mở/đóng GUI không xung đột với sự kiện click
        Bukkit.getScheduler().runTask(pl, () -> holder.onClick(p, slot, click));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof AstraxHolder) e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        last.remove(e.getPlayer().getUniqueId());
    }
}
