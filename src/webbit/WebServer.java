package webbit;

import java.net.URI;

public interface WebServer {
    void start();
    void stop();

    URI getUri();
}
