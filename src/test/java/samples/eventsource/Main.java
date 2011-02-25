package samples.eventsource;

import org.webbitserver.*;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.util.Date;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.webbitserver.WebServers.createWebServer;

public class Main {
    public static class Pusher implements Runnable {
        public HttpResponse response;
        public HttpControl control;

        @Override
        public void run() {
            while (true) {
                try {
                    if(control != null) {
                        control.execute(new Runnable() {
                            @Override
                            public void run() {
                                response.write("data: " + new Date() + "\n");
                            }
                        });
                    }
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final Pusher pusher = new Pusher();
        newSingleThreadExecutor().execute(pusher);

        WebServer webServer = createWebServer(9876)
                .add("/events", new HttpHandler() {
                    @Override
                    public void handleHttpRequest(HttpRequest request, final HttpResponse response, final HttpControl control) throws Exception {
                        response.header("Content-Type", "text/event-stream").header("Cache-Control", "no-cache");
                        pusher.response = response;
                        pusher.control = control;
                    }
                })
                .add(new EmbeddedResourceHandler("samples/eventsource/content"))
                .start();

        System.out.println("EventSource demo running on: " + webServer.getUri());
    }
}
