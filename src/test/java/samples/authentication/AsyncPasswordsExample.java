package samples.authentication;

import org.webbitserver.*;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.authentication.BasicAuthenticationHandler;
import org.webbitserver.handler.authentication.PasswordAuthenticator;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.webbitserver.WebServers.createWebServer;

/**
 * This example how to verify username/passwords in the background without blocking the
 * main Webbit thread.
 */
public class AsyncPasswordsExample {

    static Executor backgroundAuthenticatorThread = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws IOException {
        WebServer webServer = createWebServer(45454)
                .add(new BasicAuthenticationHandler(new SlowPasswordAuthenticator()))
                .add("/whoami", new WhoAmIHandler())
                .add(new StaticFileHandler("src/test/java/samples/authentication/content"))
                .start();

        System.out.println("Running on " + webServer.getUri());
    }

    /**
     * Custom password authenticator. This runs on the main Webbit handler thread.
     */
    private static class SlowPasswordAuthenticator implements PasswordAuthenticator {
        @Override
        public void authenticate(HttpRequest request, final String username, final String password, final ResultCallback callback, final Executor handlerExecutor) {
            // Submit some slow work to a background thread, so we don't block the main Webbit thread.
            backgroundAuthenticatorThread.execute(new BackgroundWorker(username, password, callback, handlerExecutor));
        }
    }

    /**
     * This runs on the background thread.
     */
    static class BackgroundWorker implements Runnable {

        private final String username;
        private final String password;
        private final PasswordAuthenticator.ResultCallback callback;
        private final Executor handlerExecutor;

        public BackgroundWorker(String username, String password, PasswordAuthenticator.ResultCallback callback, Executor handlerExecutor) {
            this.username = username;
            this.password = password;
            this.callback = callback;
            this.handlerExecutor = handlerExecutor;
        }

        @Override
        public void run() {
            // Do something slowly in the background
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            boolean authenticated = username.equals("admin") && password.endsWith("secret");

            // Ok, we have a result.. back to the main Webbit thread.
            handlerExecutor.execute(new BackgroundWorkerResult(authenticated, callback));
        }
    }

    /**
     * This runs back on the main Webbit thread.
     */
    static class BackgroundWorkerResult implements Runnable {

        private final boolean authenticated;
        private final PasswordAuthenticator.ResultCallback callback;

        BackgroundWorkerResult(boolean authenticated, PasswordAuthenticator.ResultCallback callback) {
            this.authenticated = authenticated;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (authenticated) {
                callback.success();
            } else {
                callback.failure();
            }
        }
    }
}
