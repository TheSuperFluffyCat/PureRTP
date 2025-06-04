package de.survivalnight.luna.rtp.command;

import de.survivalnight.luna.rtp.Rtp;
import de.survivalnight.luna.rtp.gui.RtpGui;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RtpCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rtp.reload")) {
                return true;
            }
            Rtp.getInstance().reloadConfig();
            sender.sendMessage("§aDie RTP-Config wurde neu geladen.");
            return true;
        }

        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("rtp.use")) {
            return true;
        }

        RtpGui.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
