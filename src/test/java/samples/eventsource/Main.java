package samples.eventsource;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.EventSourceMessage;
import org.webbitserver.WebServer;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.webbitserver.WebServers.createWebServer;

public class Main {
    public static class Pusher {
        private List<EventSourceConnection> connections = new ArrayList<EventSourceConnection>();
        private int count = 1;

        public void addConnection(EventSourceConnection connection) {
            connection.data("id", count++);
            connections.add(connection);
            broadcast("Client " + connection.data("id") + " joined");
        }

        public void removeConnection(EventSourceConnection connection) {
            connections.remove(connection);
            broadcast("Client " + connection.data("id") + " left");
        }

        public void pushPeriodicallyOn(ExecutorService webThread) throws InterruptedException, ExecutionException {
            while (true) {
                sleep(1000);
                webThread.submit(new Runnable() {
                    @Override
                    public void run() {
                        broadcast(new Date().toString());
                    }
                }).get();
            }
        }

        private void broadcast(String message) {
            for (EventSourceConnection connection : connections) {
                connection.send(new EventSourceMessage(message));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService webThread = newSingleThreadExecutor();
        final Pusher pusher = new Pusher();

        WebServer webServer = createWebServer(webThread, 9876)
                .add("/events", new EventSourceHandler() {
                    @Override
                    public void onOpen(EventSourceConnection connection) throws Exception {
                        pusher.addConnection(connection);
                    }

                    @Override
                    public void onClose(EventSourceConnection connection) throws Exception {
                        pusher.removeConnection(connection);
                    }
                })
                .add(new EmbeddedResourceHandler("samples/eventsource/content"))
                .start()
                .get();

        System.out.println("EventSource demo running on: " + webServer.getUri());

        pusher.pushPeriodicallyOn(webThread);
    }
}
