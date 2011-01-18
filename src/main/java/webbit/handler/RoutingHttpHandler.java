package webbit.handler;

import webbit.HttpHandler;
import webbit.HttpRequest;
import webbit.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class RoutingHttpHandler implements HttpHandler {

    private final HttpHandler defaultHandler;
    private final Map<String, HttpHandler> map = new HashMap<String, HttpHandler>();

    public RoutingHttpHandler(Map.Entry<String, HttpHandler>... entries) {
        this(new NotFoundHttpHandler(), entries);
    }

    public RoutingHttpHandler(HttpHandler defaultHandler, Map.Entry<String, HttpHandler>... entries) {
        this.defaultHandler = defaultHandler;
        for (Map.Entry<String, HttpHandler> entry : entries) {
            map(entry.getKey(), entry.getValue());
        }
    }

    public void map(String path, HttpHandler handler) {
        map.put(path, handler);
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response) throws Exception {
        HttpHandler handler = map.get(request.uri());
        if (handler == null) {
            handler = defaultHandler;
        }
        handler.handleHttpRequest(request, response);
    }
}
