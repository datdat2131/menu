package vn.astrax.menu;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Commands implements CommandExecutor, TabCompleter {
    private final AstraxMenuPlugin pl;

    public Commands(AstraxMenuPlugin pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        switch (cmd.getName().toLowerCase(Locale.ROOT)) {
            case "menu" -> {
                if (!(s instanceof Player p)) {
                    pl.messages().send(s, "players-only");
                    return true;
                }
                String id = a.length > 0 ? a[0] : pl.getConfig().getString("main-menu", "main");
                if (a.length > 0 && pl.books().exists(id) && !pl.menus().exists(id)) {
                    pl.books().open(p, id);
                    return true;
                }
                pl.menus().open(p, id);
            }
            case "rank" -> {
                if (!(s instanceof Player p)) {
                    pl.messages().send(s, "players-only");
                    return true;
                }
                if (a.length == 0 || a[0].equalsIgnoreCase("list")) {
                    pl.ranks().openList(p);
                } else {
                    RankManager.Rank r = pl.ranks().find(a[0]);
                    if (r == null) pl.messages().send(p, "rank-not-found", "rank", a[0]);
                    else pl.ranks().openDetail(p, r);
                }
            }
            case "astraxmenu" -> admin(s, a);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void admin(CommandSender s, String[] a) {
        String sub = a.length == 0 ? "" : a[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                pl.loadAll();
                pl.messages().send(s, "reloaded", "info",
                        pl.menus().ids().size() + " menu, " + pl.ranks().all().size() + " rank, " + pl.books().ids().size() + " sách");
            }
            case "list" -> {
                s.sendMessage(Text.raw("&6Menu: &f" + String.join("&7, &f", pl.menus().ids())));
                s.sendMessage(Text.raw("&6Rank: &f" + String.join("&7, &f", pl.ranks().all().stream().map(RankManager.Rank::id).toList())));
                s.sendMessage(Text.raw("&6Sách: &f" + String.join("&7, &f", pl.books().ids())));
            }
            case "check" -> {
                List<String> problems = pl.menus().check();
                if (problems.isEmpty()) s.sendMessage(Text.raw("&aKhông có lỗi liên kết nào."));
                else for (String line : problems) s.sendMessage(Text.raw("&c✖ &7" + line));
            }
            case "open" -> {
                if (a.length < 2) {
                    s.sendMessage(Text.raw("&cDùng: /astraxmenu open <menu> [người chơi]"));
                    return;
                }
                Player target = a.length > 2 ? Bukkit.getPlayerExact(a[2]) : (s instanceof Player p ? p : null);
                if (target == null) {
                    pl.messages().send(s, "player-not-found", "player", a.length > 2 ? a[2] : "");
                    return;
                }
                pl.menus().open(target, a[1]);
            }
            default -> s.sendMessage(Text.raw("&6/astraxmenu &f<reload|list|check|open <menu> [người chơi]>"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        String name = cmd.getName().toLowerCase(Locale.ROOT);
        String cur = a.length == 0 ? "" : a[a.length - 1].toLowerCase(Locale.ROOT);
        if (name.equals("menu") && a.length == 1) {
            out.addAll(pl.menus().ids());
            out.addAll(pl.books().ids());
        } else if (name.equals("rank") && a.length == 1) {
            out.add("list");
            for (RankManager.Rank r : pl.ranks().all()) out.add(r.id());
        } else if (name.equals("astraxmenu")) {
            if (a.length == 1) out.addAll(List.of("reload", "list", "check", "open"));
            else if (a.length == 2 && a[0].equalsIgnoreCase("open")) out.addAll(pl.menus().ids());
            else if (a.length == 3 && a[0].equalsIgnoreCase("open"))
                for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        }
        out.removeIf(x -> !x.toLowerCase(Locale.ROOT).startsWith(cur));
        return out;
    }
}
