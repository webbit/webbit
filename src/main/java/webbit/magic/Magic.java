package webbit.magic;

import com.google.gson.Gson;
import webbit.HttpRequest;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class Magic<C extends Client> implements WebSocketHandler {

    private final Class<C> clientType;
    private final Server<C> server;
    private final Map<String, Method> serverMethods = new HashMap<String, Method>();
    private final Gson gson = new Gson();

    public Magic(Class<C> clientType, Server<C> server) {
        this.clientType = clientType;
        this.server = server;
        for (Method method : server.getClass().getMethods()) {
            if (method.getAnnotation(Web.class) != null) {
                serverMethods.put(method.getName(), method);
            }
        }
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        exportMethods(connection);
        C client = implementClientProxy(connection);
        connection.data("client", client);
        server.onOpen(client);
    }

    public static class Foo {
        public String action;
        public Object[] args;
    }

    private void exportMethods(WebSocketConnection connection) {
        Map<String, Object> r = new HashMap<String, Object>();
        r.put("exports", serverMethods.keySet());
        connection.send(gson.toJson(r));
    }

    @SuppressWarnings({"unchecked"})
    private C implementClientProxy(final WebSocketConnection connection) {
        return (C) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[] {clientType}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(connection, args);
                }
                if (method.getDeclaringClass() == Client.class && method.getName().equals("connection")) {
                    return connection;
                }
                Map<String, Object> outgoing = new HashMap<String, Object>();
                outgoing.put("action", method.getName());
                outgoing.put("args", args);
                connection.send(gson.toJson(outgoing));
                return null;
            }

        });
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
        C client = (C) connection.data("client");
        Foo map = gson.fromJson(msg, Foo.class);
        Method method = serverMethods.get(map.action);

        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        int argIndex = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType.isAssignableFrom(clientType)) {
                 args[i] = client;
            } else if (paramType.isAssignableFrom(WebSocketConnection.class)) {
                args[i] = client.connection();
            } else if (paramType.isAssignableFrom(HttpRequest.class)) {
                args[i] = client.connection().httpRequest();
            } else {
                args[i] = map.args[argIndex++];
            }
        }
        method.invoke(server, args);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        C client = (C) connection.data("client");
        server.onClose(client);
    }
}
