package dev.falsegamemaster.propengine;

import dev.falsegamemaster.propengine.commands.PropEngineCommand;
import dev.falsegamemaster.propengine.nms.api.NmsAccessProvider;
import dev.falsegamemaster.propengine.nms.api.PropEngineNmsAccessor;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropCodec;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.ai.GraphDebugRenderer;
import dev.falsegamemaster.propengine.prop.ai.RouteDebugRenderer;
import dev.falsegamemaster.propengine.prop.ai.PropAI;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class PropEnginePlugin extends JavaPlugin {

    public static PropEnginePlugin getInstance() {
        return INSTANCE;
    }

    private static PropEnginePlugin INSTANCE = null;
    public static Logger LOGGER;
    private PropEngineNmsAccessor nms;
    public final PropType.Registrar propTypeRegistrar = new PropType.Registrar();
    public final Prop.Registrar propRegistrar = new Prop.Registrar();
    public final PropAI.PathRegistrar pathRegistrar = new PropAI.PathRegistrar();
    public final PropCodec propCodec = new PropCodec("propengine:props");
    private RouteDebugRenderer routeDebugRenderer;
    private GraphDebugRenderer graphDebugRenderer;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = getLogger();
        nms = NmsAccessProvider.create();
        LOGGER.info("Using PropEngine NMS accessor for " + nms.minecraftVersion());
        routeDebugRenderer = new RouteDebugRenderer(this);
        graphDebugRenderer = new GraphDebugRenderer(this);
        new PropEngineCommand(this).register();
        PropType.register(propTypeRegistrar, PropType.STATIC);
        PropType.register(propTypeRegistrar, PropType.DOOR);
        PropType.register(propTypeRegistrar, PropType.ELEVATOR);
        PropType.register(propTypeRegistrar, PropType.VEHICLE);
        PropType.register(propTypeRegistrar, PropType.NPC);
        PropType.register(propTypeRegistrar, PropType.MISC);
        getServer().getScheduler().runTaskTimer(this, propRegistrar.animationManager::tick, 1L, 1L);
        getServer().getScheduler().runTaskTimer(this, propRegistrar.controllers::tick, 1L, 1L);
        getServer().getScheduler().runTask(this, () -> {
            propCodec.loadRegistryFromStorage();
            propCodec.loadAllProps();
        });
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
    }

    public PropEngineNmsAccessor getNms() {
        return nms;
    }

    public RouteDebugRenderer getRouteDebugRenderer() {
        return routeDebugRenderer;
    }

    public GraphDebugRenderer getGraphDebugRenderer() {
        return graphDebugRenderer;
    }

}
