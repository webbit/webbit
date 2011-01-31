package webbit.handler;

import webbit.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;

public class LoggingHandler implements HttpHandler, WebSocketHandler {

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        log(new Date(), address(request.remoteAddress()),"HTTP", request.method(), request.uri());
        control.nextHandler();
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        log(new Date(), address(connection.httpRequest().remoteAddress()), "WS", "OPEN", connection.httpRequest().uri());
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
        log(new Date(), address(connection.httpRequest().remoteAddress()), "WS", "IN", connection.httpRequest().uri());
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        log(new Date(), address(connection.httpRequest().remoteAddress()), "WS", "CLOSE", connection.httpRequest().uri());
    }

    @Override
    public void onError(WebSocketConnection connection, Exception error) throws Exception {
        log(new Date(), "WS", "ERROR" + connection.httpRequest().uri());
    }

    protected void log(Object... components) {
        StringBuilder msg = new StringBuilder();
        for (Object component : components) {
            msg.append(component).append('\t');
        }
        System.out.println(msg);
    }

    protected String address(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            InetSocketAddress inetAddress = (InetSocketAddress) address;
            return inetAddress.getHostName();
        } else {
            return address.toString();
        }
    }

}
