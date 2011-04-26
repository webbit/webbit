package samples.authentication;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.handler.authentication.BasicAuthenticationHandler;

/**
 * Simple handler that shows the user who they are logged in as.
 */
public class WhoAmIHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        response.header("Content-type", "text/html")
            .content("You are: " + request.data(BasicAuthenticationHandler.USERNAME))
            .end();
    }
}
