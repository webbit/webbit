package webbit;

import java.io.Closeable;
import java.net.URI;

public interface WebServer extends Closeable {
    URI getUri();
}
