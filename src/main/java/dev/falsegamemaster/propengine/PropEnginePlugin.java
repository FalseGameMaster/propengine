package dev.falsegamemaster.propengine;

import dev.falsegamemaster.propengine.commands.PropEngineCommand;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropLoader;
import dev.falsegamemaster.propengine.prop.PropType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class PropEnginePlugin extends JavaPlugin {

    public static Logger LOGGER;
    public final PropType.Registrar PROP_TYPE_REGISTRAR = new PropType.Registrar();
    public final Prop.Registrar PROP_REGISTRAR = new Prop.Registrar();

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        new PropEngineCommand(this).register();
        PropType.register(PROP_TYPE_REGISTRAR, PropType.STATIC);
        PropType.register(PROP_TYPE_REGISTRAR, PropType.DOOR);
        PropType.register(PROP_TYPE_REGISTRAR, PropType.ELEVATOR);
        PropType.register(PROP_TYPE_REGISTRAR, PropType.VEHICLE);
        PropType.register(PROP_TYPE_REGISTRAR, PropType.NPC);
        PropType.register(PROP_TYPE_REGISTRAR, PropType.MISC);
        getServer().getScheduler().runTaskTimer(this, PROP_REGISTRAR.animationManager::tick, 1L, 1L);
        getServer().getScheduler().runTask(this, () -> PropLoader.loadProps(this));
    }

}
