package samples.ranges;

import org.webbitserver.WebServer;
import org.webbitserver.handler.StaticFileHandler;

import java.io.IOException;

import static org.webbitserver.WebServers.createWebServer;

/**
 * This example demonstrates restricting access using HTTP BASIC authentication.
 * <p/>
 * Passwords are known in advance and stored in memory.
 */
public class AudioTagUsesRangesExample {

    public static void main(String[] args) throws IOException {
        WebServer webServer = createWebServer(45453)
                .add(new StaticFileHandler("src/test/java/samples/ranges/content"))
                .start();

        System.out.println("Running on " + webServer.getUri());
    }

}
