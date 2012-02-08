package org.webbitserver.handler.authentication;

import org.webbitserver.HttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Implementation of PasswordAuthenticator that verifies usernames and password from a prepopulated hashmap.
 */
public class InMemoryPasswords implements PasswordAuthenticator {

    private final Map<String, String> usernameToPasswords = new HashMap<String, String>();

    public InMemoryPasswords add(String username, String password) {
        usernameToPasswords.put(username, password);
        return this;
    }

    @Override
    public void authenticate(HttpRequest request, String username, String password, ResultCallback callback, Executor handlerExecutor) {
        String expectedPassword = usernameToPasswords.get(username);
        if (expectedPassword != null && password.equals(expectedPassword)) {
            callback.success();
        } else {
            callback.failure();
        }
    }
}
