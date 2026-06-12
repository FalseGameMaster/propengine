package dev.falsegamemaster.propengine.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.annotation.Nullable;

// TODO (5/31/2026): perhaps merge this with another class. PropCodec or Prop?
public class PropDataParser {

    public static @Nullable JsonObject parseObject(@Nullable String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return JsonParser.parseString(raw).getAsJsonObject();
        } catch (IllegalArgumentException | JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON object: " + e.getMessage(), e);
        }
    }

    private PropDataParser() {}

}
