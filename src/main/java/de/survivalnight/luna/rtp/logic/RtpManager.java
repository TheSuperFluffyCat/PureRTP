package de.survivalnight.luna.rtp.logic;

import de.survivalnight.luna.rtp.Rtp;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class RtpManager {

    private static final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public static void teleportPlayer(Player player, String worldName, int range) {
        if (!checkCooldown(player)) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            send(player, "messages.world-not-found");
            return;
        }

        SafeLocationFinder.findSafeLocationAsync(world, range, loc -> {
            if (loc == null) {
                send(player, "messages.teleport-fail");
            } else {
                player.teleportAsync(loc);
                send(player, "messages.teleport-success");
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
        });
    }

    private static boolean checkCooldown(Player player) {
        long now = System.currentTimeMillis();
        long cooldown = Rtp.getInstance().getConfig().getInt("cooldown-seconds", 60) * 1000L;

        if (player.hasPermission("rtp.bypass")) return true;

        if (cooldowns.containsKey(player.getUniqueId())) {
            long last = cooldowns.get(player.getUniqueId());
            long remaining = cooldown - (now - last);
            if (remaining > 0) {
                String msg = Rtp.getInstance().getConfig().getString("messages.cooldown-wait", "")
                        .replace("<seconds>", String.valueOf(remaining / 1000));
                player.sendMessage(ColorUtil.mm(msg));
                return false;
            }
        }
        return true;
    }

    private static void send(Player p, String path) {
        String msg = Rtp.getInstance().getConfig().getString(path, "");
        if (!msg.isEmpty()) p.sendMessage(ColorUtil.mm(msg));
    }
}
