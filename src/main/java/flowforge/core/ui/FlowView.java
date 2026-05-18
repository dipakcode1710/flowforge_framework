package flowforge.core.ui;

import flowforge.core.ui.component.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class FlowView {

    UiSession session;
    private final List<Component> children = new ArrayList<>();

    public abstract void build();

    protected void add(Component... components) {
        for (Component c : components) {
            c.setSession(session);
            children.add(c);
        }
    }

    List<Component> getChildren() {
        return children;
    }

    String renderHtml() {
        StringBuilder sb = new StringBuilder();
        for (Component c : children) sb.append(c.renderHtml());
        return sb.toString();
    }
}
