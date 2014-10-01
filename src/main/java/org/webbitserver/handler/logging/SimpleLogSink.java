package org.webbitserver.handler.logging;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketConnection;

import java.io.Flushable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Date;

import static org.webbitserver.helpers.Hex.toHex;

public class SimpleLogSink implements LogSink {

    // TODO: Offload filesystem IO to another thread

    protected final Appendable out;
    protected final String[] dataValuesToLog;

    protected final String lineSeparator = System.getProperty("line.separator", "\n");

    protected boolean trouble = false;

    public SimpleLogSink(Appendable out, String... dataValuesToLog) {
        this.out = out;
        this.dataValuesToLog = dataValuesToLog;
        try {
            formatHeader(out);
            flush();
        } catch (IOException e) {
            trouble = true;
            panic(e);
        }
    }

    public SimpleLogSink(String... dataValuesToLog) {
        this(System.out, dataValuesToLog);
    }

    @Override
    public void httpStart(HttpRequest request) {
        custom(request, null, "HTTP-START", null);
    }

    @Override
    public void httpEnd(HttpRequest request, HttpResponse response) {
        custom(request, response, "HTTP-END", null); // TODO: Time request
    }

    @Override
    public void webSocketConnectionOpen(WebSocketConnection connection) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-OPEN", null);
    }

    @Override
    public void webSocketConnectionClose(WebSocketConnection connection) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-CLOSE", null);
    }

    @Override
    public void webSocketInboundData(WebSocketConnection connection, String data) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-IN-STRING", data);
    }

    @Override
    public void webSocketInboundData(WebSocketConnection connection, byte[] data) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-IN-HEX", toHex(data));
    }

    @Override
    public void webSocketInboundPing(WebSocketConnection connection, byte[] msg) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-IN-PING", toHex(msg));
    }

    @Override
    public void webSocketInboundPong(WebSocketConnection connection, byte[] msg) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-IN-PONG", toHex(msg));
    }

    @Override
    public void webSocketOutboundData(WebSocketConnection connection, String data) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-OUT-STRING", data);
    }

    @Override
    public void webSocketOutboundData(WebSocketConnection connection, byte[] data) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-OUT-HEX", toHex(data));
    }

    @Override
    public void webSocketOutboundPing(WebSocketConnection connection, byte[] msg) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-OUT-PING", toHex(msg));
    }

    @Override
    public void webSocketOutboundPong(WebSocketConnection connection, byte[] msg) {
        custom(connection.httpRequest(), null, "WEB-SOCKET-" + connection.version() + "-OUT-PONG", toHex(msg));
    }

    @Override
    public void error(HttpRequest request, Throwable error) {
        custom(request, null, "ERROR-OPEN", error.toString());
    }

    @Override
    public void custom(HttpRequest request, HttpResponse reponse, String action, String data) {
        if (trouble) {
            return;
        }
        try {
            formatLogEntry(out, request, action, data);
            flush();
        } catch (IOException e) {
            trouble = true;
            panic(e);
        }
    }

    @Override
    public void eventSourceConnectionOpen(EventSourceConnection connection) {
        custom(connection.httpRequest(), null, "EVENT-SOURCE-OPEN", null);
    }

    @Override
    public void eventSourceConnectionClose(EventSourceConnection connection) {
        custom(connection.httpRequest(), null, "EVENT-SOURCE-CLOSE", null);
    }

    @Override
    public void eventSourceOutboundData(EventSourceConnection connection, String data) {
        custom(connection.httpRequest(), null, "EVENT-SOURCE-OUT", data);
    }

    protected void flush() throws IOException {
        if (out instanceof Flushable) {
            Flushable flushable = (Flushable) out;
            flushable.flush();
        }
    }

    protected void panic(IOException exception) {
        // If we can't log, be rude!
        exception.printStackTrace();
    }

    protected Appendable formatLogEntry(Appendable out, HttpRequest request, String action, String data) throws IOException {
        long cumulativeTimeOfRequest = cumulativeTimeOfRequest(request);
        Date now = new Date();
        formatValue(out, now);
        formatValue(out, now.getTime());
        formatValue(out, cumulativeTimeOfRequest);
        formatValue(out, request.id());
        formatValue(out, address(request.remoteAddress()));
        formatValue(out, action);
        formatValue(out, request.uri());
        formatValue(out, data);
        for (String key : dataValuesToLog) {
            formatValue(out, request.data(key));
        }
        return out.append(lineSeparator);
    }

    protected Appendable formatHeader(Appendable out) throws IOException {
        out.append("#Log started at ")
                .append(new Date().toString())
                .append(" (").append(String.valueOf(System.currentTimeMillis())).append(")")
                .append(lineSeparator)
                .append('#');
        formatValue(out, "Date");
        formatValue(out, "Timestamp");
        formatValue(out, "MillsSinceRequestStart");
        formatValue(out, "RequestID");
        formatValue(out, "RemoteHost");
        formatValue(out, "Action");
        formatValue(out, "Path");
        formatValue(out, "Payload");
        for (String key : dataValuesToLog) {
            formatValue(out, "Data:" + key);
        }
        return out.append(lineSeparator);
    }


    private long cumulativeTimeOfRequest(HttpRequest request) {
        return System.currentTimeMillis() - request.timestamp();
    }

    protected Appendable formatValue(Appendable out, Object value) throws IOException {
        if (value == null) {
            return out.append("-\t");
        }
        String string = value.toString().trim();
        if (string.isEmpty()) {
            return out.append("-\t");
        }
        return out.append(string).append('\t');
    }

    protected String address(SocketAddress address) {
        return address.toString();
    }
}
