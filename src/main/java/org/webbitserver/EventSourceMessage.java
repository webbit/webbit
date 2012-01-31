package org.webbitserver;

import java.util.regex.Pattern;

public class EventSourceMessage {
    private static final Pattern START = Pattern.compile("^", Pattern.MULTILINE);
    private static final String DATA = "data: ";
    private static final String COLON = ": ";
    private static final String ID = "id";
    private static final String EVENT = "event";
    private static final String RETRY = "retry";
    private static final String LF = "\n";
    private final StringBuilder payload = new StringBuilder();

    public EventSourceMessage() {
    }

    public EventSourceMessage(String data) {
        this();
        data(data);
    }

    public EventSourceMessage data(String data) {
        prependOnAllLines(EventSourceMessage.DATA, data);
        return this;
    }

    public EventSourceMessage comment(String data) {
        prependOnAllLines(EventSourceMessage.COLON, data);
        return this;
    }

    public EventSourceMessage id(long id) {
        return id(String.valueOf(id));
    }

    public EventSourceMessage id(String id) {
        appendFieldValue(EventSourceMessage.ID, id);
        return this;
    }

    public EventSourceMessage event(String event) {
        appendFieldValue(EventSourceMessage.EVENT, event);
        return this;
    }

    public EventSourceMessage retry(Long reconnectionTimeMillis) {
        appendFieldValue(EventSourceMessage.RETRY, reconnectionTimeMillis.toString());
        return this;
    }

    public String build() {
        return payload.toString() + "\n";
    }

    private void appendFieldValue(String field, String value) {
        payload.append(field);
        if (value != null && !value.isEmpty()) {
            payload.append(EventSourceMessage.COLON);
            payload.append(value);
        }
        payload.append(EventSourceMessage.LF);
    }

    private void prependOnAllLines(String field, String value) {
        String multilineField = EventSourceMessage.START.matcher(value).replaceAll(field);
        payload.append(multilineField).append(EventSourceMessage.LF);
    }
}
