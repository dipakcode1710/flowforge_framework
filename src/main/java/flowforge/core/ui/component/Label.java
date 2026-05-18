package flowforge.core.ui.component;

public class Label extends Component {

    private String text;

    public Label(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
        pushPatch();
    }

    public String getText() {
        return text;
    }

    @Override
    public String renderHtml() {
        return "<span id=\"" + getId() + "\">" + escape(text) + "</span>";
    }

    @Override
    protected String renderInner() {
        return escape(text);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
