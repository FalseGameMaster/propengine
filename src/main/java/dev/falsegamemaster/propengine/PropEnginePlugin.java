package dev.falsegamemaster.propengine;

import dev.falsegamemaster.propengine.commands.PropEngineCommand;
import dev.falsegamemaster.propengine.prop.Prop;
import dev.falsegamemaster.propengine.prop.PropType;
import dev.falsegamemaster.propengine.prop.doors.TestBlastDoorProp;
import dev.falsegamemaster.propengine.prop.doors.TestDoorProp;
import dev.falsegamemaster.propengine.prop.doors.TestUtilityDoorProp;
import dev.falsegamemaster.propengine.prop.statics.PlayerCorpseProp;
import dev.falsegamemaster.propengine.prop.statics.TestStaticProp;
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
        Prop.register(PROP_REGISTRAR, TestStaticProp::new);
        Prop.register(PROP_REGISTRAR, PlayerCorpseProp::new);
        Prop.register(PROP_REGISTRAR, TestDoorProp::new);
        Prop.register(PROP_REGISTRAR, TestBlastDoorProp::new);
        Prop.register(PROP_REGISTRAR, TestUtilityDoorProp::new);
    }

}
