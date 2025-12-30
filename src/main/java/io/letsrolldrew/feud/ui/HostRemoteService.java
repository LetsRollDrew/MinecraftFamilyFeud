package io.letsrolldrew.feud.ui;

import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class HostRemoteService {
    private final Plugin plugin;
    private final NamespacedKey hostKey;
    private final boolean debug;

    public HostRemoteService(Plugin plugin, NamespacedKey hostKey) {
        this(plugin, hostKey, false);
    }

    public HostRemoteService(Plugin plugin, NamespacedKey hostKey, boolean debug) {
        this.plugin = plugin;
        this.hostKey = hostKey;
        this.debug = debug;
    }

    public void giveOrReplace(Player player, ItemStack book) {
        PlayerInventory inv = player.getInventory();
        Count before = countRemotes(inv);

        boolean replaced = false;

        if (isHostRemote(inv.getItemInOffHand())) {
            inv.setItemInOffHand(book);
            replaced = true;
            log("Placed host remote in offhand for " + player.getName());
        } else {
            int slot = findFirstHostRemoteSlot(inv);
            if (slot != -1) {
                inv.setItem(slot, book);
                replaced = true;
                log("Replaced host remote in slot " + slot + " for " + player.getName());
            }
        }

        if (!replaced) {
            Map<Integer, ItemStack> leftover = inv.addItem(book);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
                log("Inventory full, dropped host remote at " + player.getName());
            } else {
                log("Added host remote to inventory for " + player.getName());
            }
        }

        Count after = countRemotes(inv);
        log("Host remote counts before=" + before + " after=" + after + " for " + player.getName());
    }

    private boolean isHostRemote(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer tag = pdc.get(hostKey, PersistentDataType.INTEGER);
        return tag != null && tag == 1;
    }

    private int findFirstHostRemoteSlot(PlayerInventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (isHostRemote(inv.getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    private Count countRemotes(PlayerInventory inv) {
        int slots = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            if (isHostRemote(inv.getItem(i))) {
                slots++;
            }
        }
        int off = isHostRemote(inv.getItemInOffHand()) ? 1 : 0;
        return new Count(slots, off);
    }

    private void log(String msg) {
        if (debug) {
            plugin.getLogger().info(msg);
        }
    }

    private record Count(int slots, int offhand) {
        @Override
        public String toString() {
            return "slots=" + slots + ", offhand=" + offhand;
        }
    }
}
