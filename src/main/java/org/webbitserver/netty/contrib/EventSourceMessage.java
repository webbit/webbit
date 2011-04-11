package org.webbitserver.netty.contrib;

import java.util.regex.Pattern;

public class EventSourceMessage {
    private static final Pattern START = Pattern.compile("^", Pattern.MULTILINE);
    private static final String DATA = "data: ";
    private static final String COLON = ": ";
    private static final String ID = "id";
    private static final String EVENT = "event";
    private static final String RETRY = "retry";

    private static final String LF = "\n";
    private StringBuilder payload = new StringBuilder();

    public EventSourceMessage data(String data) {
        prependOnAllLines(DATA, data);
        return this;
    }

    public EventSourceMessage comment(String data) {
        prependOnAllLines(COLON, data);
        return this;
    }

    public EventSourceMessage id(long id) {
        return id(String.valueOf(id));
    }

    public EventSourceMessage id(String id) {
        appendFieldValue(ID, id);
        return this;
    }

    public EventSourceMessage event(String event) {
        appendFieldValue(EVENT, event);
        return this;
    }

    public EventSourceMessage retry(Long reconnectionTimeMillis) {
        appendFieldValue(RETRY, reconnectionTimeMillis.toString());
        return this;
    }

    public EventSourceMessage end() {
        payload.append(LF);
        return this;
    }

    @Override
    public String toString() {
        return payload.toString();
    }

    private void appendFieldValue(String field, String value) {
        payload.append(field);
        if (value != null && !value.isEmpty()) {
            payload.append(COLON);
            payload.append(value);
        }
        payload.append(LF);
    }

    private void prependOnAllLines(String field, String value) {
        String multilineField = START.matcher(value).replaceAll(field);
        payload.append(multilineField).append(LF);
    }
}
