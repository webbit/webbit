package org.webbitserver.wrapper;

import org.webbitserver.CometConnection;
import org.webbitserver.HttpRequest;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class CometConnectionWrapper implements CometConnection {

    private CometConnection connection;

    public CometConnectionWrapper(CometConnection connection) {
        this.connection = connection;
    }

    public CometConnection underlyingControl() {
        return connection;
    }

    public CometConnectionWrapper underlyingControl(CometConnection control) {
        this.connection = control;
        return this;
    }

    public CometConnection originalControl() {
        if (connection instanceof CometConnectionWrapper) {
            CometConnectionWrapper wrapper = (CometConnectionWrapper) connection;
            return wrapper.originalControl();
        } else {
            return connection;
        }
    }

    @Override
    public HttpRequest httpRequest() {
        return connection.httpRequest();
    }

    @Override
    public CometConnectionWrapper send(String message) {
        connection.send(message);
        return this;
    }

    @Override
    public CometConnectionWrapper close() {
        connection.close();
        return this;
    }

    @Override
    public Map<String, Object> data() {
        return connection.data();
    }

    @Override
    public Object data(String key) {
        return connection.data(key);
    }

    @Override
    public CometConnectionWrapper data(String key, Object value) {
        connection.data(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return connection.dataKeys();
    }

    @Override
    public Executor handlerExecutor() {
        return connection.handlerExecutor();
    }

    @Override
    public String protocol() {
        return connection.protocol();
    }

    @Override
    public void execute(Runnable command) {
        connection.execute(command);
    }

}
