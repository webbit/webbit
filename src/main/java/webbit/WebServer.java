package webbit;

import java.io.IOException;
import java.net.URI;

public interface WebServer {
    URI getUri();

    void start() throws IOException;
    void stop() throws IOException;
    void join() throws InterruptedException;
}
