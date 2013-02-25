package samples.hello;

import org.webbitserver.WebServer;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.logging.LoggingHandler;
import org.webbitserver.handler.logging.SimpleLogSink;

import static org.webbitserver.WebServers.createWebServer;

public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9876)
                .add("/", new Hello())
                .start().get();

        System.out.println("Hello app  running on: " + webServer.getUri());
    }

}
