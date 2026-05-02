package dev.falsegamemaster.propengine.prop;

import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.util.AdvancedLocation;

import javax.annotation.Nullable;

public record PropSpawnRequest(String literal, String uniqueFriendlyName, AdvancedLocation location, @Nullable JsonObject data) {}
