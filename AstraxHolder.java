package vn.astrax.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;

/** Mọi GUI của plugin đều implement interface này để GuiListener nhận diện và chặn lấy đồ. */
public interface AstraxHolder extends InventoryHolder {
    void onClick(Player player, int slot, ClickType click);
}
