package org.webbitserver.handler;

import org.webbitserver.WebSocket;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReconnectingWebSocketHandler implements WebSocketHandler {
    private final WebSocketHandler handler;
    private final WebSocket webSocket;
    private final long reconnectIntervalMillis;
    private final Timer timer = new Timer();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public ReconnectingWebSocketHandler(WebSocketHandler handler, WebSocket webSocket, long reconnectIntervalMillis) {
        this.handler = handler;
        this.webSocket = webSocket;
        this.reconnectIntervalMillis = reconnectIntervalMillis;
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Throwable {
        handler.onOpen(connection);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Throwable {
        handler.onClose(connection);
        scheduleReconnect();
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
        handler.onMessage(connection, msg);
    }

    @Override
    public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
        handler.onMessage(connection, msg);
    }

    @Override
    public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
        handler.onPing(connection, msg);
    }

    @Override
    public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
        handler.onPong(connection, msg);
    }

    private void scheduleReconnect() {
        connected.set(false);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!connected.get()) {
                    webSocket.start();
                }
            }
        }, reconnectIntervalMillis);
    }

}
