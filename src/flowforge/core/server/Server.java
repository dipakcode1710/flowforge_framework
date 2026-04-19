package flowforge.core.server;

import flowforge.core.annotations.*;
import flowforge.core.middleware.*;
import flowforge.core.validation.Validator;
import flowforge.core.config.Config;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {

    private static final Map<String, Method> routeMethods = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    // Dev dashboard
    private static final List<RouteInfo> routeList = new ArrayList<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    // =========================
    public static class RouteInfo {
        public String method;
        public String path;
        public String handler;
        public List<String> params;
        public boolean auth;

        public RouteInfo(String method, String path, String handler,
                        List<String> params, boolean auth) {
            this.method = method;
            this.path = path;
            this.handler = handler;
            this.params = params;
            this.auth = auth;
        }
    }
    // =========================
    // Register route
    // =========================
    public static void addRoute(String httpMethod, String path, Method method, Object controller) {

        String key = httpMethod + ":" + path;

        routeMethods.put(key, method);
        controllers.put(key, controller);

        // 🔥 SAFE VERSION (no Parameter dependency)
        List<String> params = new ArrayList<>();

        for (Parameter p : method.getParameters()) {

            if (p.isAnnotationPresent(QueryParam.class)) {
                QueryParam qp = p.getAnnotation(QueryParam.class);
                params.add(qp.value());
            }

            if (p.isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = p.getAnnotation(PathVariable.class);
                params.add(pv.value());
            }
        }

        boolean auth = method.isAnnotationPresent(Auth.class) ||
                    controller.getClass().isAnnotationPresent(Auth.class);

        routeList.add(new RouteInfo(
                httpMethod,
                path,
                controller.getClass().getSimpleName() + "#" + method.getName(),
                params,
                auth
        ));
    }

    // =========================
    // START SERVER
    // =========================
    public static void start(int port) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // =========================
        // DEV DASHBOARD (IMPORTANT: BEFORE "/")
        // =========================
        server.createContext("/dev/routes", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String json = mapper.writeValueAsString(routeList);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);

            exchange.getResponseBody().write(json.getBytes());
            exchange.close();
        });

        // =========================
        // SWAGGER-LIKE DOCS (JSON)
        // =========================
        server.createContext("/dev/docs-ui", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String routeJson = "[]";
            try {
                routeJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(routeList);
            } catch (Exception ignored) {}

            String routeJsonBase64 = java.util.Base64.getEncoder()
                .encodeToString(routeJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder html = new StringBuilder();

            html.append("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>FlowForge API Docs</title>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            body{font-family:system-ui,sans-serif;background:#f8f8f7;color:#1a1a18;font-size:14px}
            .header{padding:20px 24px 14px;border-bottom:1px solid #e5e4df;display:flex;align-items:center;gap:12px;background:#fff}
            .logo{font-size:17px;font-weight:500}
            .badge{background:#e6f1fb;color:#185fa5;font-size:11px;padding:2px 8px;border-radius:20px}
            .token-row{padding:12px 24px;display:flex;gap:10px;align-items:center;background:#fff;border-bottom:1px solid #e5e4df}
            .token-row label{font-size:12px;color:#888;min-width:90px}
            .filter-bar{padding:10px 24px;display:flex;gap:6px;background:#fff;border-bottom:1px solid #e5e4df}
            .filter-btn{font-size:12px;padding:4px 12px;border-radius:20px;border:1px solid #ddd;background:#fff;color:#666;cursor:pointer}
            .filter-btn.active{background:#fff;color:#1a1a18;border-color:#888;font-weight:500}
            .routes{padding:16px 24px;display:flex;flex-direction:column;gap:8px}
            .route-card{border:1px solid #e5e4df;border-radius:10px;overflow:hidden;background:#fff}
            .route-header{display:flex;align-items:center;gap:10px;padding:11px 14px;cursor:pointer}
            .route-header:hover{background:#f5f5f3}
            .method{font-size:11px;font-weight:600;padding:3px 8px;border-radius:6px;min-width:50px;text-align:center;letter-spacing:.03em}
            .method.GET{background:#eaf3de;color:#3b6d11}
            .method.POST{background:#e6f1fb;color:#185fa5}
            .method.PUT{background:#faeeda;color:#854f0b}
            .method.DELETE{background:#fcebeb;color:#a32d2d}
            .path{font-family:monospace;font-size:13px;flex:1}
            .auth-badge{font-size:11px;padding:2px 8px;border-radius:20px;background:#faeeda;color:#854f0b}
            .handler-label{font-size:12px;color:#aaa}
            .chevron{font-size:11px;color:#bbb;transition:transform .2s}
            .route-body{border-top:1px solid #eee;padding:14px 16px;background:#fafaf9;display:none}
            .route-body.open{display:block}
            .meta-row{display:flex;gap:8px;align-items:center;margin-bottom:8px;font-size:13px}
            .meta-label{color:#888;font-size:12px;min-width:64px}
            .param-pill{background:#fff;border:1px solid #ddd;border-radius:20px;font-family:monospace;font-size:11px;padding:2px 8px}
            .try-section{margin-top:10px;padding-top:10px;border-top:1px solid #eee}
            .param-inputs{display:flex;flex-wrap:wrap;gap:10px;margin-bottom:10px}
            .param-group{display:flex;align-items:center;gap:6px}
            .param-name{font-family:monospace;font-size:12px;color:#888}
            .try-row{display:flex;gap:8px}
            input[type=text]{padding:6px 10px;font-size:13px;font-family:monospace;border:1px solid #ddd;border-radius:7px;background:#fff;color:#1a1a18;outline:none}
            input[type=text]:focus{border-color:#888}
            .try-btn{padding:6px 14px;font-size:13px;border:1px solid #ddd;border-radius:7px;background:#fff;color:#1a1a18;cursor:pointer;white-space:nowrap}
            .try-btn:hover{background:#f0f0ee}
            .response-box{margin-top:10px;padding:10px 12px;border-radius:7px;border:1px solid #eee;font-family:monospace;font-size:12px;background:#fff;color:#888;white-space:pre-wrap;min-height:36px}
            .response-box.success{border-color:#9fe1cb;color:#0f6e56;background:#e1f5ee}
            .response-box.error{border-color:#f7c1c1;color:#a32d2d;background:#fcebeb}
            #route-count{font-size:12px;color:#aaa;margin-left:auto}
            </style>
            </head>
            <body>

            <div class="header">
            <span class="logo">FlowForge</span>
            <span class="badge">API Docs</span>
            <span id="route-count"></span>
            </div>

            <div class="token-row">
            <label>Bearer token</label>
            <input type="text" id="global-token" placeholder="paste JWT here..." style="flex:1" />
            </div>

            <div class="filter-bar" id="filters"></div>
            <div class="routes" id="routes-container"></div>

            """);

            // Safe base64 injection - immune to any special characters in route data
            html.append("<script>\nconst ROUTES = JSON.parse(atob('");
            html.append(routeJsonBase64);
            html.append("'));\n");

            html.append("""
            let activeFilter = "ALL";
            const methods = ["ALL", ...new Set(ROUTES.map(r => r.method))];

            function renderFilters() {
            document.getElementById("filters").innerHTML = methods.map(m =>
                `<button class="filter-btn${m===activeFilter?' active':''}" onclick="setFilter('${m}')">${m}</button>`
            ).join("");
            }

            function setFilter(m) {
            activeFilter = m;
            renderFilters();
            renderRoutes();
            }

            function buildUrl(i, basePath, params) {
            const urlInput = document.getElementById("url-" + i);
            let url = basePath;
            const queryParts = [];

            params.forEach(p => {
                const input = document.getElementById("param-" + i + "-" + p);
                if (!input) return;
                const val = input.value.trim();
                if (!val) return;

                if (url.includes("{" + p + "}")) {
                url = url.replace("{" + p + "}", encodeURIComponent(val));
                } else {
                queryParts.push(p + "=" + encodeURIComponent(val));
                }
            });

            if (queryParts.length) url += "?" + queryParts.join("&");
            urlInput.value = url;
            }

            function renderRoutes() {
            const filtered = ROUTES
                .map((r, originalIndex) => ({ ...r, originalIndex }))
                .filter(r => activeFilter === "ALL" || r.method === activeFilter);

            document.getElementById("route-count").textContent =
                filtered.length + " route" + (filtered.length !== 1 ? "s" : "");

            document.getElementById("routes-container").innerHTML = filtered.map((r) => {
                const i = r.originalIndex;
                return `
                <div class="route-card">
                    <div class="route-header" onclick="toggle(${i})">
                    <span class="method ${r.method}">${r.method}</span>
                    <span class="path">${r.path}</span>
                    ${r.auth ? '<span class="auth-badge">auth</span>' : ''}
                    <span class="handler-label">${r.handler}</span>
                    <span class="chevron" id="chev-${i}">&#9662;</span>
                    </div>
                    <div class="route-body" id="body-${i}">
                    ${r.params && r.params.length ? `
                        <div class="meta-row">
                        <span class="meta-label">params</span>
                        ${r.params.map(p => `<span class="param-pill">${p}</span>`).join("")}
                        </div>` : ""}
                    <div class="meta-row">
                        <span class="meta-label">handler</span>
                        <span style="font-family:monospace;font-size:12px;color:#888">${r.handler}</span>
                    </div>
                    <div class="try-section">
                        ${r.params && r.params.length ? `
                        <div class="param-inputs">
                        ${r.params.map(p => `
                            <div class="param-group">
                            <span class="param-name">${p}</span>
                            <input type="text"
                                id="param-${i}-${p}"
                                placeholder="enter ${p}..."
                                style="width:130px"
                                oninput="buildUrl(${i},'${r.path}',${JSON.stringify(r.params)})" />
                            </div>
                        `).join("")}
                        </div>` : ""}
                        <div class="try-row">
                        <input type="text" id="url-${i}" value="${r.path}" style="flex:1" />
                        <button class="try-btn" onclick="tryIt(${i},'${r.method}')">Try &#8599;</button>
                        </div>
                        <div class="response-box" id="res-${i}">response will appear here</div>
                    </div>
                    </div>
                </div>
                `;
            }).join("");
            }

            function toggle(i) {
            const b = document.getElementById("body-" + i);
            const c = document.getElementById("chev-" + i);
            c.style.transform = b.classList.toggle("open") ? "rotate(180deg)" : "";
            }

            async function tryIt(i, method) {
            const path = document.getElementById("url-" + i).value;
            const token = document.getElementById("global-token").value;
            const box = document.getElementById("res-" + i);
            box.className = "response-box";
            box.textContent = "Sending...";
            try {
                const res = await fetch(path, {
                method,
                headers: token ? { Authorization: "Bearer " + token } : {}
                });
                const text = await res.text();
                box.className = "response-box " + (res.ok ? "success" : "error");
                box.textContent = "HTTP " + res.status + "\\n\\n" + text;
            } catch (e) {
                box.className = "response-box error";
                box.textContent = e.toString();
            }
            }

            renderFilters();
            renderRoutes();
            </script>
            </body></html>
            """);

            byte[] res = html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, res.length);
            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.flush();
            os.close();
        });        

        // =========================
        // MAIN ROUTER
        // =========================
        server.createContext("/", exchange -> {

            String path = exchange.getRequestURI().getPath();
            String httpMethod = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            Map<String, String> queryParams = parseQuery(query);

            String response = "";
            int statusCode = 200;

            try {

                Method method = null;
                Object controller = null;
                Map<String, String> pathVars = null;

                // Route matching
                for (String routeKey : routeMethods.keySet()) {

                    String[] parts = routeKey.split(":", 2);
                    String routeMethod = parts[0];
                    String routePath = parts[1];

                    if (!routeMethod.equals(httpMethod)) continue;

                    Map<String, String> vars = matchPath(routePath, path);

                    if (vars != null) {
                        method = routeMethods.get(routeKey);
                        controller = controllers.get(routeKey);
                        pathVars = vars;
                        break;
                    }
                }

                if (method == null) {
                    sendSafe(exchange, "404 Not Found", 404);
                    return;
                }

                String requestBody = readBody(exchange);
                RequestContext ctx = new RequestContext(exchange, pathVars, requestBody);

                List<Middleware> middlewareList = new ArrayList<>();

                // Auth middleware
                boolean authEnabled = Config.getBoolean("auth.enabled", true);

                if (authEnabled &&
                        (method.isAnnotationPresent(Auth.class) ||
                         controller.getClass().isAnnotationPresent(Auth.class))) {

                    Auth auth = method.isAnnotationPresent(Auth.class)
                            ? method.getAnnotation(Auth.class)
                            : controller.getClass().getAnnotation(Auth.class);

                    middlewareList.add(new AuthMiddleware(auth.role()));
                }

                Method finalMethod = method;
                Object finalController = controller;

                MiddlewareChain chain = new MiddlewareChain(
                        middlewareList,
                        context -> invokeController(finalMethod, finalController, context, queryParams)
                );

                Object result = chain.proceed(ctx);

                if (ctx.handled) return;

                // Response handling
                if (result instanceof String) {
                    response = (String) result;
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                } else {
                    response = mapper.writeValueAsString(result);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                }

            } catch (Exception e) {

                e.printStackTrace();

                try {

                    Throwable actual = (e instanceof java.lang.reflect.InvocationTargetException)
                            ? e.getCause()
                            : e;

                    ExceptionManager.Handler handler = ExceptionManager.getHandler(actual);

                    if (handler != null) {

                        Object result = handler.method.invoke(handler.instance, actual);

                        if (result instanceof String) {
                            response = (String) result;
                            exchange.getResponseHeaders().add("Content-Type", "text/plain");
                        } else {
                            response = mapper.writeValueAsString(result);
                            exchange.getResponseHeaders().add("Content-Type", "application/json");
                        }

                    } else {

                        response = mapper.writeValueAsString(
                                Map.of(
                                        "error", actual.getClass().getSimpleName(),
                                        "message", actual.getMessage()
                                )
                        );
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    response = "Critical Error";
                }

                statusCode = 500;
            }

            sendSafe(exchange, response, statusCode);
        });

        System.out.println(" Server started on port " + port);
        server.start();
    }

    // =========================
    // Controller invocation
    // =========================
    private static Object invokeController(
            Method method,
            Object controller,
            RequestContext ctx,
            Map<String, String> queryParams) throws Exception {

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter param = parameters[i];
            Object value;

            if (param.isAnnotationPresent(QueryParam.class)) {
                QueryParam qp = param.getAnnotation(QueryParam.class);
                value = convert(queryParams.get(qp.value()), param.getType());
            } else {
                value = mapper.readValue(ctx.body, param.getType());
            }

            Validator.validate(param, value);
            args[i] = value;
        }

        return method.invoke(controller, args);
    }

    // =========================
    private static Map<String, String> parseQuery(String query) {

        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }

        return map;
    }

    private static Map<String, String> matchPath(String route, String path) {

        String[] r = route.split("/");
        String[] p = path.split("/");

        if (r.length != p.length) return null;

        Map<String, String> vars = new HashMap<>();

        for (int i = 0; i < r.length; i++) {

            if (r[i].startsWith("{")) {
                vars.put(r[i].replace("{", "").replace("}", ""), p[i]);
            } else if (!r[i].equals(p[i])) {
                return null;
            }
        }

        return vars;
    }

    private static Object convert(String value, Class<?> type) {

        if (value == null) return null;

        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);

        return value;
    }

    private static String readBody(HttpExchange ex) {

        try {
            InputStream is = ex.getRequestBody();
            return new String(is.readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }

    // =========================
    // SAFE RESPONSE (no crash)
    // =========================
    private static void sendSafe(HttpExchange ex, String res, int code) {
        try {
            byte[] bytes = res.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ex.sendResponseHeaders(code, bytes.length);
            OutputStream os = ex.getResponseBody();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}