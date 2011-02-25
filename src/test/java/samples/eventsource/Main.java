package samples.eventsource;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.WebServer;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.webbitserver.WebServers.createWebServer;

public class Main {
    public static class Pusher implements Runnable {
        private List<EventSourceConnection> connections = new ArrayList<EventSourceConnection>();

        @Override
        public void run() {
            while (true) {
                try {
                    for (EventSourceConnection connection : connections) {
                        connection.send(new Date().toString());
                    }
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    break;
                }
            }
        }

        public void addConnection(EventSourceConnection connection) {
            connections.add(connection);
        }
    }

    public static void main(String[] args) throws Exception {
        final Pusher pusher = new Pusher();
        newSingleThreadExecutor().execute(pusher);

        WebServer webServer = createWebServer(9876)
                .add("/events", new EventSourceHandler() {
                    @Override
                    public void onOpen(EventSourceConnection connection) throws Exception {
                        pusher.addConnection(connection);
                    }
                })
                .add(new EmbeddedResourceHandler("samples/eventsource/content"))
                .start();

        System.out.println("EventSource demo running on: " + webServer.getUri());
    }
}
