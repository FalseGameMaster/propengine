package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;
import org.bukkit.block.Block;

public abstract class BlockPropPart<P extends Prop, B extends Block> extends PropPart<P, B> {

    public BlockPropPart(Class<P> propClass, Prop prop, int sequentialID) {
        super(propClass, prop, sequentialID);
    }

    @Override
    protected final void setupInternal(B raw, Prop.SpawnRequest request) {
        // TODO: create this logic (4/18/2026)
    }

    @Override
    public void spawn(Prop.SpawnRequest request) {
        // TODO: create this logic (4/18/2026)
    }

    @Override
    public void unload() {
        // TODO: create this logic (5/29/2026)
    }

}
