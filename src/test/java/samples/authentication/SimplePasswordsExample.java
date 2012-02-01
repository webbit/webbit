package samples.authentication;

import org.webbitserver.*;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.authentication.BasicAuthenticationHandler;
import org.webbitserver.handler.authentication.InMemoryPasswords;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.webbitserver.WebServers.createWebServer;

/**
 * This example demonstrates restricting access using HTTP BASIC authentication.
 *
 * Passwords are known in advance and stored in memory.
 */
public class SimplePasswordsExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        InMemoryPasswords passwords = new InMemoryPasswords()
                .add("joe", "secret")
                .add("jeff", "somepassword");

        WebServer webServer = createWebServer(45453)
                .add(new BasicAuthenticationHandler(passwords))
                .add("/whoami", new WhoAmIHttpHandler())
                .add("/whoami-ws", new WhoAmIWebSocketHandler())
                .add(new StaticFileHandler("src/test/java/samples/authentication/content"))
                .start().get();

        System.out.println("Running on " + webServer.getUri());
    }

}
