package io.letsrolldrew.feud.ui;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("MockBukkit registry setup currently broken under test harness; enable once registry data is resolved")
class HostRemoteServiceTest {
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void replacesOffhand() {
        var plugin = MockBukkit.createMockPlugin();
        NamespacedKey key = new NamespacedKey(plugin, "host_remote");
        HostRemoteService service = new HostRemoteService(plugin, key);
        ItemStack remote1 = remote(key);
        ItemStack remote2 = remote(key);
        PlayerMock player = server.addPlayer();
        player.getInventory().setItemInOffHand(remote1);

        service.giveOrReplace(player, remote2);

        assertEquals(remote2, player.getInventory().getItemInOffHand());
        assertEquals(0, countTaggedInInventory(player, key));
    }

    @Test
    void replacesInventorySlot() {
        var plugin = MockBukkit.createMockPlugin();
        NamespacedKey key = new NamespacedKey(plugin, "host_remote");
        HostRemoteService service = new HostRemoteService(plugin, key);
        ItemStack remote1 = remote(key);
        ItemStack remote2 = remote(key);
        PlayerMock player = server.addPlayer();
        player.getInventory().setItem(5, remote1);

        service.giveOrReplace(player, remote2);

        assertEquals(remote2, player.getInventory().getItem(5));
        assertEquals(0, countTaggedInInventory(player, key));
    }

    @Test
    void removesDuplicatesKeepsOne() {
        var plugin = MockBukkit.createMockPlugin();
        NamespacedKey key = new NamespacedKey(plugin, "host_remote");
        HostRemoteService service = new HostRemoteService(plugin, key);
        ItemStack remote1 = remote(key);
        ItemStack remote2 = remote(key);
        PlayerMock player = server.addPlayer();
        player.getInventory().setItem(1, remote1);
        player.getInventory().setItem(2, remote1.clone());

        service.giveOrReplace(player, remote2);

        assertEquals(1, countTagged(player, key));
    }

    private ItemStack remote(NamespacedKey key) {
        ItemStack stack = new ItemStack(org.bukkit.Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) stack.getItemMeta();
        meta.setTitle("Family Feud Remote");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        return stack;
    }

    private int countTagged(Player player, NamespacedKey key) {
        int count = 0;
        var inv = player.getInventory();
        for (ItemStack stack : inv.getContents()) {
            if (isTagged(stack, key)) {
                count++;
            }
        }
        if (isTagged(inv.getItemInOffHand(), key)) {
            count++;
        }
        return count;
    }

    private int countTaggedInInventory(Player player, NamespacedKey key) {
        int count = 0;
        var inv = player.getInventory();
        for (ItemStack stack : inv.getContents()) {
            if (isTagged(stack, key)) {
                count++;
            }
        }
        return count;
    }

    private boolean isTagged(ItemStack stack, NamespacedKey key) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        var val = stack.getItemMeta().getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.INTEGER);
        return val != null && val == 1;
    }
}
