package vn.astrax.menu;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class AstraxMenuPlugin extends JavaPlugin {
    private Messages messages;
    private MenuManager menus;
    private RankManager ranks;
    private GuideBooks books;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        for (String f : new String[]{"menus.yml", "ranks.yml", "guides.yml", "messages.yml"}) {
            if (!new File(getDataFolder(), f).exists()) saveResource(f, false);
        }
        messages = new Messages(this);
        menus = new MenuManager(this);
        ranks = new RankManager(this);
        books = new GuideBooks(this);
        loadAll();

        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

        Commands cmds = new Commands(this);
        for (String name : new String[]{"menu", "rank", "astraxmenu"}) {
            PluginCommand pc = getCommand(name);
            if (pc != null) {
                pc.setExecutor(cmds);
                pc.setTabCompleter(cmds);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AstraxExpansion(this).register();
        }
        getLogger().info("AstraxMenu " + getPluginMeta().getVersion() + " đã bật: "
                + menus.ids().size() + " menu, " + ranks.all().size() + " rank.");
    }

    /** Đọc lại toàn bộ cấu hình (dùng cho /astraxmenu reload). */
    public void loadAll() {
        reloadConfig();
        Map<String, String> g = new HashMap<>();
        ConfigurationSection gs = getConfig().getConfigurationSection("glyphs");
        if (gs != null) {
            for (String k : gs.getKeys(false)) g.put(k, gs.getString(k, ""));
        }
        Text.setGlyphs(g);
        messages.load();
        ranks.load();
        menus.load();
        books.load();
    }

    public Messages messages() {
        return messages;
    }

    public MenuManager menus() {
        return menus;
    }

    public RankManager ranks() {
        return ranks;
    }

    public GuideBooks books() {
        return books;
    }
}
