package flowforge.core.ui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class UiServer {

    private static final String JS_CLIENT =
        "const es=new EventSource('/ui/sse/'+document.documentElement.dataset.session);" +
        "es.onmessage=function(e){" +
          "const p=JSON.parse(e.data);" +
          "const el=document.getElementById(p.id);" +
          "if(el)el.innerHTML=p.value;" +
        "};" +
        "es.onerror=function(){es.close();};";

    public static void register(HttpServer server) {
        if (!UiRouter.hasRoutes()) return;

        server.createContext("/ui/sse/", UiServer::handleSse);

        for (Map.Entry<String, Class<? extends FlowView>> entry : UiRouter.getAllRoutes().entrySet()) {
            final Class<? extends FlowView> viewClass = entry.getValue();
            server.createContext(entry.getKey(), exchange -> handlePage(exchange, viewClass));
        }
    }

    private static void handlePage(HttpExchange exchange, Class<? extends FlowView> viewClass)
            throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            FlowView view = viewClass.getDeclaredConstructor().newInstance();
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            UiSession session = new UiSession(sessionId, view);
            view.session = session;
            view.build();
            UiSessionRegistry.put(session);

            String html = buildPage(sessionId, view.renderHtml());
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            System.err.println("[UI] Failed to render page: " + e.getMessage());
            exchange.sendResponseHeaders(500, -1);
        }
    }

    private static void handleSse(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String sessionId = path.substring("/ui/sse/".length());
        UiSession session = UiSessionRegistry.get(sessionId);

        if (session == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);

        OutputStream os = exchange.getResponseBody();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String patch = session.takePatch();
                os.write(("data: " + patch + "\n\n").getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Client disconnected — normal shutdown
        } finally {
            UiSessionRegistry.remove(sessionId);
            exchange.close();
        }
    }

    private static String buildPage(String sessionId, String bodyContent) {
        return "<!DOCTYPE html>\n" +
               "<html data-session=\"" + sessionId + "\">\n" +
               "<head><meta charset=\"UTF-8\"></head>\n" +
               "<body>\n" +
               bodyContent + "\n" +
               "<script>" + JS_CLIENT + "</script>\n" +
               "</body>\n</html>";
    }
}
