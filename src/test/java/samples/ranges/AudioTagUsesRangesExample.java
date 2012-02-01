package samples.ranges;

import org.webbitserver.WebServer;
import org.webbitserver.handler.StaticFileHandler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.webbitserver.WebServers.createWebServer;

/**
 * This example has a simple HTML page with an audio element
 * With Chrome, the request for the audio file uses a Range header
 */
public class AudioTagUsesRangesExample {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        WebServer webServer = createWebServer(45453)
                .add(new StaticFileHandler("src/test/java/samples/ranges/content"))
                .start().get();

        System.out.println("Running on " + webServer.getUri());
    }

}
