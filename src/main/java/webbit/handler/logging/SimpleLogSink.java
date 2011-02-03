package webbit.handler.logging;

import webbit.HttpRequest;
import webbit.WebSocketConnection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;

public class SimpleLogSink implements LogSink {

    // TODO: WebSocket connection id
    // TODO: Allow additional request data attributes (e.g. 'user') to be logged

    @Override
    public void httpStart(HttpRequest request) {
        log(request, "HTTP-START", null);
    }

    @Override
    public void httpEnd(HttpRequest request) {
        log(request, "HTTP-END", null); // TODO: Time request
    }

    @Override
    public void webSocketOpen(WebSocketConnection connection) {
        log(connection.httpRequest(), "WEBSOCKET-OPEN", null);
    }

    @Override
    public void webSocketClose(WebSocketConnection connection) {
        log(connection.httpRequest(), "WEBSOCKET-CLOSE", null);
    }

    @Override
    public void webSocketInboundData(WebSocketConnection connection, String data) {
        log(connection.httpRequest(), "WEBSOCKET-IN", data);
    }

    @Override
    public void webSocketOutboundData(WebSocketConnection connection, String data) {
        log(connection.httpRequest(), "WEBSOCKET-OUT", data);
    }

    @Override
    public void error(HttpRequest httpRequest, Throwable error) {
        log(httpRequest, "ERROR-OPEN", error.toString());
    }

    protected void log(HttpRequest request, String action, String data) {
        System.out.println(format(request, action, data));
    }

    protected String format(HttpRequest request, String action, String data) {
        long now = System.currentTimeMillis();
        return formatValue(new Date(now)) + '\t'
                + String.valueOf(now) + '\t'
                + formatValue(address(request.remoteAddress())) + '\t'
                + formatValue(action) + '\t'
                + formatValue(request.uri()) + '\t'
                + formatValue(data);
    }

    protected String formatValue(Object value) {
        String string = value == null ? "-" : value.toString().trim();
        if (string.isEmpty()) {
            string = "-";
        }
        return string;
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
