package io.letsrolldrew.feud.display;

import io.letsrolldrew.feud.display.lookup.EntityLookup;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// uses a fake lookup + Mockito entities to avoid Bukkit.getEntity in unit tests
// while still covering register/resolve/remove logic
final class DisplayRegistryTest {

    @Test
    void registerAndResolveEntity() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);
        DisplayKey key = new DisplayKey("ns", "group", "id1", "part");

        Entity entity = mockEntity();
        lookup.put(entity);

        registry.register(key, entity);

        Optional<Entity> resolved = registry.resolve(key);
        assertTrue(resolved.isPresent());
        assertSame(entity, resolved.get());
    }

    @Test
    void resolveCleansMissingEntity() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);
        DisplayKey key = new DisplayKey("ns", "group", "id1", "part");

        Entity entity = mockEntity();
        lookup.put(entity);
        registry.register(key, entity);

        lookup.clear(); // simulate entity gone

        Optional<Entity> resolved = registry.resolve(key);
        assertFalse(resolved.isPresent());
        assertEquals(0, registry.removeAll()); // cleaned up
    }

    @Test
    void resolveCleansDeadEntity() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);
        DisplayKey key = new DisplayKey("ns", "group", "id1", "part");

        Entity entity = mockEntity();
        when(entity.isDead()).thenReturn(true);
        lookup.put(entity);
        registry.register(key, entity);

        Optional<Entity> resolved = registry.resolve(key);
        assertFalse(resolved.isPresent());
        assertEquals(0, registry.removeAll());
    }

    @Test
    void removeRemovesEntity() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);
        DisplayKey key = new DisplayKey("ns", "group", "id1", "part");

        Entity entity = mockEntity();
        lookup.put(entity);
        registry.register(key, entity);

        registry.remove(key);

        verify(entity).remove();
        assertEquals(0, registry.removeAll());
    }

    @Test
    void removeByNamespaceRemovesMatchingOnly() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);

        Entity first = mockEntity();
        lookup.put(first);
        registry.register(new DisplayKey("board", "g1", "id1", "part"), first);

        Entity second = mockEntity();
        lookup.put(second);
        registry.register(new DisplayKey("other", "g2", "id2", "part"), second);

        int removed = registry.removeByNamespace("board");
        assertEquals(1, removed);
        verify(first).remove();

        Optional<Entity> stillThere = registry.resolve(new DisplayKey("other", "g2", "id2", "part"));
        assertTrue(stillThere.isPresent());
        assertSame(second, stillThere.get());
    }

    @Test
    void removeAllRemovesEverything() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);

        Entity first = mockEntity();
        lookup.put(first);
        registry.register(new DisplayKey("ns", "g1", "id1", "part"), first);

        Entity second = mockEntity();
        lookup.put(second);
        registry.register(new DisplayKey("ns", "g2", "id2", "part"), second);

        int removed = registry.removeAll();
        assertEquals(2, removed);
        verify(first).remove();
        verify(second).remove();
    }

    @Test
    void typedResolveFiltersByType() {
        FakeLookup lookup = new FakeLookup();
        DisplayRegistry registry = new DisplayRegistry(lookup);
        DisplayKey textKey = new DisplayKey("ns", "g1", "id1", "answer");
        DisplayKey itemKey = new DisplayKey("ns", "g1", "id1", "bg");

        TextDisplay text = mock(TextDisplay.class);
        UUID textId = UUID.randomUUID();
        when(text.getUniqueId()).thenReturn(textId);
        World world = mock(World.class);
        UUID worldId = UUID.randomUUID();
        when(world.getUID()).thenReturn(worldId);
        when(text.getWorld()).thenReturn(world);
        lookup.put(text);
        registry.register(textKey, text);

        ItemDisplay item = mock(ItemDisplay.class);
        UUID itemId = UUID.randomUUID();
        when(item.getUniqueId()).thenReturn(itemId);
        when(item.getWorld()).thenReturn(world);
        lookup.put(item);
        registry.register(itemKey, item);

        assertTrue(registry.resolveText(textKey).isPresent());
        assertTrue(registry.resolveItem(itemKey).isPresent());
        assertTrue(registry.resolveText(itemKey).isEmpty());
        assertTrue(registry.resolveItem(textKey).isEmpty());
    }

    // map based lookup so registry logic is testable without Bukkit entities
    private static final class FakeLookup implements EntityLookup {
        private final Map<UUID, Entity> map = new HashMap<>();

        @Override
        public Entity get(UUID id) {
            return map.get(id);
        }

        void put(Entity entity) {
            if (entity == null) {
                return;
            }
            map.put(entity.getUniqueId(), entity);
        }

        void clear() {
            map.clear();
        }
    }

    private static Entity mockEntity() {
        Entity entity = mock(Entity.class);
        UUID id = UUID.randomUUID();
        when(entity.getUniqueId()).thenReturn(id);
        World world = mock(World.class);
        UUID worldId = UUID.randomUUID();
        when(world.getUID()).thenReturn(worldId);
        when(entity.getWorld()).thenReturn(world);
        return entity;
    }
}
