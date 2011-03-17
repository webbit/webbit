package org.webbitserver.eventsource;

/**
 * <a href="http://dev.w3.org/html5/eventsource/#event-stream-interpretation">Interprets an event stream</a>
 * and dispatches messages to the {@link EventSourceHandler}.
 */
class MessageDispatcher {
    private static final String DATA = "data";
    private static final String ID = "id";
    private static final String EVENT = "event";
    private static final String DEFAULT_EVENT = "message";
    private static final String EMPTY_STRING = "";

    private final MessageEmitter messageEmitter;
    private final String origin;

    private StringBuffer data = new StringBuffer();
    private String lastEventId;
    private String eventName = DEFAULT_EVENT;

    public MessageDispatcher(MessageEmitter messageEmitter, String origin) {
        this.messageEmitter = messageEmitter;
        this.origin = origin;
    }

    public void line(String line) {
        int colonIndex;
        if (line.trim().isEmpty()) {
            dispatchEvent();
        } else if (line.startsWith(":")) {
            // ignore
        } else if ((colonIndex = line.indexOf(":")) != -1) {
            String field = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 1).replaceFirst(" ", EMPTY_STRING);
            processField(field, value);
        } else {
            processField(line, EMPTY_STRING);
        }
    }

    private void processField(String field, String value) {
        if (DATA.equals(field)) {
            data.append(value).append("\n");
        } else if (ID.equals(field)) {
            lastEventId = value;
        } else if (EVENT.equals(field)) {
            eventName = value;
        }
    }

    private void dispatchEvent() {
        if (data.length() == 0) {
            return;
        }
        String dataString = data.toString();
        if (dataString.endsWith("\n")) {
            dataString = dataString.substring(0, dataString.length() - 1);
        }
        MessageEvent message = new MessageEvent(dataString, lastEventId, origin);
        messageEmitter.emitMessage(eventName, message);
        data = new StringBuffer();
        lastEventId = null;
        eventName = DEFAULT_EVENT;
    }
}
