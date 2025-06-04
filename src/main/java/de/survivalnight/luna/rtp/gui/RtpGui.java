package de.survivalnight.luna.rtp.gui;

import de.survivalnight.luna.rtp.Rtp;
import de.survivalnight.luna.rtp.logic.ColorUtil;
import de.survivalnight.luna.rtp.logic.RtpManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RtpGui implements Listener {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§b§lRandom Teleport");

        ConfigurationSection buttons = Rtp.getInstance().getConfig().getConfigurationSection("gui");
        if (buttons != null) {
            for (String key : buttons.getKeys(false)) {
                ConfigurationSection section = buttons.getConfigurationSection(key);
                if (section == null) continue;

                Material mat = Material.matchMaterial(section.getString("material", "GRASS_BLOCK"));
                int slot = section.getInt("slot", 0);
                if (mat == null || slot < 0 || slot >= inv.getSize()) continue;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();

                String name = section.getString("name", "&aTeleport");
                meta.displayName(ColorUtil.mm(name));

                List<String> lore = section.getStringList("lore");
                if (!lore.isEmpty()) {
                    List<Component> parsedLore = lore.stream().map(ColorUtil::mm).toList();
                    meta.lore(parsedLore);
                }

                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§b§lRandom Teleport")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String clickedName = clicked.getItemMeta().getDisplayName();

        ConfigurationSection buttons = Rtp.getInstance().getConfig().getConfigurationSection("gui");
        if (buttons == null) return;

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection sec = buttons.getConfigurationSection(key);
            if (sec == null) continue;

            String configName = sec.getString("name");
            if (configName == null || !clickedName.equals(ColorUtil.translateColor(configName))) continue;

            String world = sec.getString("world");
            int range = sec.getInt("range", 1000);

            RtpManager.teleportPlayer(player, world, range);
            player.closeInventory();
            return;
        }
    }
}
