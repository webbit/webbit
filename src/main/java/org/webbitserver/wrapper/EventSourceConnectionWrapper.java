package org.webbitserver.wrapper;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.HttpRequest;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class EventSourceConnectionWrapper implements EventSourceConnection {

    private EventSourceConnection connection;

    public EventSourceConnectionWrapper(EventSourceConnection connection) {
        this.connection = connection;
    }

    public EventSourceConnection underlyingControl() {
        return connection;
    }

    public EventSourceConnectionWrapper underlyingControl(EventSourceConnection control) {
        this.connection = control;
        return this;
    }

    public EventSourceConnection originalControl() {
        if (connection instanceof EventSourceConnectionWrapper) {
            EventSourceConnectionWrapper wrapper = (EventSourceConnectionWrapper) connection;
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
    public EventSourceConnectionWrapper send(org.webbitserver.EventSourceMessage message) {
        connection.send(message);
        return this;
    }

    @Override
    public EventSourceConnectionWrapper close() {
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
    public EventSourceConnectionWrapper data(String key, Object value) {
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
    public void execute(Runnable command) {
        connection.execute(command);
    }

}
