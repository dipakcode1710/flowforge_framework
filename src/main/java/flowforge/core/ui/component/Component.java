package flowforge.core.ui.component;

import flowforge.core.ui.UiSession;

import java.util.UUID;

public abstract class Component {

    private final String id = "ff_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    UiSession session;

    public void setSession(UiSession session) {
        this.session = session;
    }

    public String getId() {
        return id;
    }

    /** Full outer HTML used for the initial page render. */
    public abstract String renderHtml();

    /** Inner content only, used for DOM patch updates. */
    protected abstract String renderInner();

    /** Call this in subclasses after state changes to push a live DOM patch. */
    protected void pushPatch() {
        if (session != null) {
            session.pushPatch(id, renderInner());
        }
    }
}
