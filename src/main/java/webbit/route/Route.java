package webbit.route;

import webbit.HttpHandler;
import webbit.WebSocketHandler;
import webbit.handler.HttpToWebSocketHandler;
import webbit.handler.RoutingHttpHandler;
import webbit.handler.StaticDirectoryHttpHandler;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Route {

    public static RoutingHttpHandler route(HttpHandler defaultHandler, Map.Entry<String, HttpHandler>... routes) {
        return new RoutingHttpHandler(defaultHandler, routes);
    }

    public static RoutingHttpHandler route(Map.Entry<String, HttpHandler>... routes) {
        return new RoutingHttpHandler(routes);
    }

    public static Map.Entry<String, HttpHandler> get(String path, HttpHandler httpHandler) {
        return new AbstractMap.SimpleEntry<String, HttpHandler>(path, httpHandler);
    }

    public static Map.Entry<String, HttpHandler> post(String path, HttpHandler httpHandler) {
        // TODO: Differentiate get vs post
        return new AbstractMap.SimpleEntry<String, HttpHandler>(path, httpHandler);
    }

    public static Map.Entry<String, HttpHandler> socket(String path, WebSocketHandler webSocketHandler) {
        return new AbstractMap.SimpleEntry<String, HttpHandler>(path, new HttpToWebSocketHandler(webSocketHandler));
    }

}
