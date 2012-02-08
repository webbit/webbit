package org.webbitserver.handler.authentication;

import org.webbitserver.HttpRequest;

import java.util.concurrent.Executor;

/**
 * Provided to BasicAuthenticationHandler to verify the supplied username and password are valid.
 * <p/>
 * Implementations should check username/password are valid and call
 * ResultCallback.success() or ResultCallback.failure(). One of these methods must called - once and only once.
 * <p/>
 * If the result cannot be obtained automatically, the code should not block (as this will block the entire server).
 * Instead, the work should be offloaded to another thread/process, and the ResultCallback methods should be invoked
 * using the handlerExecutor when done.
 * <p/>
 * For simple cases, use InMemoryPasswords.
 * <p/>
 * See samples.authentication.SimplePasswordsExample in the src/tests directory for a really basic usage. To implement
 * a custom authenticator that performs background IO, see samples.authentication.AsyncPasswordsExample.
 *
 * @see BasicAuthenticationHandler
 * @see InMemoryPasswords
 */
public interface PasswordAuthenticator {

    void authenticate(HttpRequest request, String username, String password, ResultCallback callback, Executor handlerExecutor);

    interface ResultCallback {
        void success();

        void failure();
    }

}
