package dev.falsegamemaster.propengine.prop.part;

import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropSpawnRequest;
import org.bukkit.block.Block;

public abstract class BlockPropPart<P extends Prop, B extends Block> extends PropPart<P, B> {

    public BlockPropPart(Class<P> propClass, Prop prop, int sequentialID) {
        super(propClass, prop, sequentialID);
    }

    @Override
    protected final void setupInternal(B raw, PropSpawnRequest request) {
        // TODO: create this logic (4/18/2026)
    }

    @Override
    public void spawn(PropSpawnRequest request) {
        // TODO: create this logic (4/18/2026)
    }

}
