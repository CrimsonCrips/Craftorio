package org.amoverride.craftorio;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;


import xaero.map.highlight.ChunkHighlighter;

import java.util.List;


public class ClaimsHighlighter extends ChunkHighlighter {

    public ClaimsHighlighter() {
        super(true);
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> resourceKey, int i, int i1) {
        return 1;
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> resourceKey, int i, int i1) {
        return false;
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> key, int x, int z) {

        return true;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> resourceKey, int i, int i1, int i2) {

    }


    @Override
    protected int[] getColors(ResourceKey<Level> resourceKey, int i, int i1) {
        this.resultStore[0] = 255;
        this.resultStore[1] = 255;
        this.resultStore[2] = 255;
        this.resultStore[3] = 255;
        this.resultStore[4] = 255;
        return resultStore;
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> resourceKey, int i, int i1) {
        return null;
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> resourceKey, int i, int i1) {
        return null;
    }

}
