package webbit.handler.logging;

import webbit.HttpRequest;
import webbit.WebSocketConnection;

import java.io.Flushable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;

public class SimpleLogSink implements LogSink {

    // TODO: WebSocket connection id
    // TODO: Offload filesystem IO to another thread

    protected final Appendable out;
    protected final String[] dataValuesToLog;

    protected final String lineSeparator = System.getProperty("line.separator", "\n");

    protected boolean trouble = false;

    public SimpleLogSink(Appendable out, String... dataValuesToLog) {
        this.out = out;
        this.dataValuesToLog = dataValuesToLog;
    }

    public SimpleLogSink(String... dataValuesToLog) {
        this(System.out, dataValuesToLog);
    }

    @Override
    public void httpStart(HttpRequest request) {
        custom(request, "HTTP-START", null);
    }

    @Override
    public void httpEnd(HttpRequest request) {
        custom(request, "HTTP-END", null); // TODO: Time request
    }

    @Override
    public void webSocketOpen(WebSocketConnection connection) {
        custom(connection.httpRequest(), "WEBSOCKET-OPEN", null);
    }

    @Override
    public void webSocketClose(WebSocketConnection connection) {
        custom(connection.httpRequest(), "WEBSOCKET-CLOSE", null);
    }

    @Override
    public void webSocketInboundData(WebSocketConnection connection, String data) {
        custom(connection.httpRequest(), "WEBSOCKET-IN", data);
    }

    @Override
    public void webSocketOutboundData(WebSocketConnection connection, String data) {
        custom(connection.httpRequest(), "WEBSOCKET-OUT", data);
    }

    @Override
    public void error(HttpRequest request, Throwable error) {
        custom(request, "ERROR-OPEN", error.toString());
    }

    @Override
    public void custom(HttpRequest request, String action, String data) {
        if (trouble) {
            return;
        }
        try {
            formatLogLine(out, request, action, data);
            out.append(lineSeparator);
            if (out instanceof Flushable) {
                Flushable flushable = (Flushable) out;
                flushable.flush();
            }
        } catch (IOException e) {
            trouble = true;
            panic(e);
        }
    }

    protected void panic(IOException exception) {
        // If we can't log, be rude!
        exception.printStackTrace();
    }

    protected Appendable formatLogLine(Appendable out, HttpRequest request, String action, String data) throws IOException {
        long now = System.currentTimeMillis();
        formatValue(out, new Date(now));
        formatValue(out, now);
        formatValue(out, address(request.remoteAddress()));
        formatValue(out, action);
        formatValue(out, request.uri());
        formatValue(out, data);
        for (String key : dataValuesToLog) {
            formatValue(out, request.data(key));
        }
        return out;
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
        if (address instanceof InetSocketAddress) {
            InetSocketAddress inetAddress = (InetSocketAddress) address;
            return inetAddress.getHostName();
        } else {
            return address.toString();
        }
    }
}
