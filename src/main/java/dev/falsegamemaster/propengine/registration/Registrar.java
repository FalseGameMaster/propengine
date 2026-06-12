package dev.falsegamemaster.propengine.registration;

import java.util.Map;

public abstract class Registrar<E> {

    public boolean registerIfMissing(String literal, E entry) {
        Map<String, E> entries = getEntriesInternal();
        if (!entries.containsKey(literal)) {
            entries.put(literal, entry);
            return true;
        }
        return false;
    }

    protected abstract Map<String, E> getEntriesInternal();

    public abstract Map<String, E> getEntries();

}
