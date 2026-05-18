package dev.falsegamemaster.propengine.prop;

import com.google.gson.JsonObject;
import dev.falsegamemaster.propengine.util.AdvancedLocation;

import javax.annotation.Nullable;

public record PropSpawnRequest(String literal, String uniqueName, AdvancedLocation location, @Nullable JsonObject data) {}
