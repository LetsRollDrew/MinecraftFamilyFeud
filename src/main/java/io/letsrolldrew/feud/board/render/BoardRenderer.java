package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.BoardLayout10x6;
import io.letsrolldrew.feud.board.layout.TilePos;

import java.awt.Color;

//Paints a simple base (border + interior fill) into tile framebuffers
//Tiles should still be mutable from here so other renderers can update them for game flow
// TODO: When artists are done with the map art, add a way to load them from a directory
public final class BoardRenderer {
    private final TileFramebufferStore store;
    private final DirtyTracker dirty;

    private final byte borderColor;
    private final byte interiorColor;

    public BoardRenderer(TileFramebufferStore store, DirtyTracker dirty) {
        this.store = store;
        this.dirty = dirty;
        // map palette colors
        this.borderColor = org.bukkit.map.MapPalette.matchColor(new Color(40, 40, 40));
        this.interiorColor = org.bukkit.map.MapPalette.matchColor(new Color(12, 51, 89));
    }

    // fill tiles with base colors and mark dirty
    public void paintBase() {
        var border = BoardLayout10x6.borderTiles();
        var interior = BoardLayout10x6.interiorTiles();

        for (TilePos pos : border) {
            fillTile(pos, borderColor);
        }
        for (TilePos pos : interior) {
            fillTile(pos, interiorColor);
        }
    }

    private void fillTile(TilePos pos, byte color) {
        TileBuffer buf = store.get(pos);
        for (int y = 0; y < TileBuffer.SIZE; y++) {
            for (int x = 0; x < TileBuffer.SIZE; x++) {
                buf.set(x, y, color);
            }
        }
        dirty.markDirty(pos);
    }
}
