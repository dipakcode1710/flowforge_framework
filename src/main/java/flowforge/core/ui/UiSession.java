package flowforge.core.ui;

import java.util.concurrent.LinkedBlockingQueue;

public class UiSession {

    public final String id;
    final FlowView view;
    private final LinkedBlockingQueue<String> patchQueue = new LinkedBlockingQueue<>();

    UiSession(String id, FlowView view) {
        this.id = id;
        this.view = view;
    }

    public void pushPatch(String componentId, String innerValue) {
        String escaped = innerValue
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
        patchQueue.offer("{\"id\":\"" + componentId + "\",\"value\":\"" + escaped + "\"}");
    }

    String takePatch() throws InterruptedException {
        return patchQueue.take();
    }
}
