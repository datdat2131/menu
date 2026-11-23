package vn.astrax.menu;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class Messages {
    private final AstraxMenuPlugin pl;
    private YamlConfiguration y = new YamlConfiguration();

    public Messages(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    public void load() {
        y = YamlConfiguration.loadConfiguration(new File(pl.getDataFolder(), "messages.yml"));
    }

    public String get(String key, String... kv) {
        String s = y.getString(key, "&c[thiếu tin nhắn: " + key + "]");
        for (int i = 0; i + 1 < kv.length; i += 2) s = s.replace("{" + kv[i] + "}", kv[i + 1]);
        return y.getString("prefix", "") + s;
    }

    public void send(CommandSender to, String key, String... kv) {
        to.sendMessage(Text.raw(get(key, kv)));
    }
}
