package vn.astrax.menu;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public final class GuideBooks {
    private record BookDef(String title, String author, List<String> pages) {}

    private final AstraxMenuPlugin pl;
    private final Map<String, BookDef> books = new LinkedHashMap<>();

    public GuideBooks(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    public void load() {
        books.clear();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(new File(pl.getDataFolder(), "guides.yml"));
        ConfigurationSection root = y.getConfigurationSection("books");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection b = root.getConfigurationSection(id);
            if (b == null) continue;
            books.put(id.toLowerCase(Locale.ROOT),
                    new BookDef(b.getString("title", id), b.getString("author", "Server"), b.getStringList("pages")));
        }
    }

    public boolean exists(String id) {
        return id != null && books.containsKey(id.trim().toLowerCase(Locale.ROOT));
    }

    public Set<String> ids() {
        return books.keySet();
    }

    public boolean open(Player p, String id) {
        BookDef d = id == null ? null : books.get(id.trim().toLowerCase(Locale.ROOT));
        if (d == null) return false;
        List<Component> pages = new ArrayList<>();
        for (String s : d.pages()) pages.add(Text.raw(Placeholders.apply(pl, p, s)));
        if (pages.isEmpty()) pages.add(Component.empty());
        p.openBook(Book.book(Text.raw(d.title()), Text.raw(d.author()), pages));
        return true;
    }
}
