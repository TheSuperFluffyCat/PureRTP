package de.survivalnight.luna.rtp.gui;

import de.survivalnight.luna.rtp.Rtp;
import de.survivalnight.luna.rtp.logic.ColorUtil;
import de.survivalnight.luna.rtp.logic.RtpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        // Lade Titel aus Config
        String rawTitle = Rtp.getInstance().getConfig().getString("gui-title", "<bold><aqua>Random Teleport</bold>");
        Component titleComponent = ColorUtil.mm(rawTitle).decoration(TextDecoration.ITALIC, false);
        String legacyTitle = LegacyComponentSerializer.legacySection().serialize(titleComponent);

        // Lade Reihenanzahl aus Config
        int rows = Rtp.getInstance().getConfig().getInt("gui-rows", 3);
        rows = Math.max(1, Math.min(6, rows)); // nur 1–6 erlaubt
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size, legacyTitle);

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
                Component nameComponent = ColorUtil.mm(name).decoration(TextDecoration.ITALIC, false);
                meta.displayName(nameComponent);

                List<String> lore = section.getStringList("lore");
                if (!lore.isEmpty()) {
                    List<Component> parsedLore = lore.stream()
                            .map(ColorUtil::mm)
                            .map(c -> c.decoration(TextDecoration.ITALIC, false))
                            .toList();
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
        // Vergleiche Titel über Component
        String rawTitle = Rtp.getInstance().getConfig().getString("gui-title", "<bold><aqua>Random Teleport</bold>");
        Component configTitle = ColorUtil.mm(rawTitle).decoration(TextDecoration.ITALIC, false);
        String expectedTitle = LegacyComponentSerializer.legacySection().serialize(configTitle);

        if (!e.getView().getTitle().equals(expectedTitle)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String legacyName = clicked.getItemMeta().getDisplayName();
        Component clickedComponent = LegacyComponentSerializer.legacySection().deserialize(legacyName)
                .decoration(TextDecoration.ITALIC, false);

        ConfigurationSection buttons = Rtp.getInstance().getConfig().getConfigurationSection("gui");
        if (buttons == null) return;

        for (String key : buttons.getKeys(false)) {
            ConfigurationSection sec = buttons.getConfigurationSection(key);
            if (sec == null) continue;

            String configName = sec.getString("name");
            if (configName == null) continue;

            Component configComponent = ColorUtil.mm(configName).decoration(TextDecoration.ITALIC, false);

            if (!clickedComponent.equals(configComponent)) continue;

            String world = sec.getString("world");
            int range = sec.getInt("range", 1000);

            RtpManager.teleportPlayer(player, world, range);
            player.closeInventory();
            return;
        }
    }
}
