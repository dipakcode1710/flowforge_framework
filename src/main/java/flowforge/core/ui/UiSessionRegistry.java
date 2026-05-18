package flowforge.core.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UiSessionRegistry {

    private static final Map<String, UiSession> sessions = new ConcurrentHashMap<>();

    public static void put(UiSession session) {
        sessions.put(session.id, session);
    }

    public static UiSession get(String id) {
        return sessions.get(id);
    }

    public static void remove(String id) {
        sessions.remove(id);
    }
}
