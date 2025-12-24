package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.TilePos;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

//MapRenderer that copies the TileBuffer for a specific tile
@SuppressWarnings("deprecation") // MapCanvas#setPixel is deprecated, fix later
public final class TileMapRenderer extends MapRenderer {
    private final TilePos tile;
    private final TileFramebufferStore store;

    public TileMapRenderer(TilePos tile, TileFramebufferStore store) {
        super(true);
        this.tile = tile;
        this.store = store;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        TileBuffer buffer = store.get(tile);
        byte[] data = buffer.data();
        int idx = 0;
        for (int y = 0; y < TileBuffer.SIZE; y++) {
            for (int x = 0; x < TileBuffer.SIZE; x++) {
                canvas.setPixel(x, y, data[idx++]);
            }
        }
    }
}
