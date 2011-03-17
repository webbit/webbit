package org.webbitserver.eventsource;

/**
 * <a href="http://dev.w3.org/html5/eventsource/#event-stream-interpretation">Interprets an event stream</a>
 * and dispatches messages to the {@link EventSourceHandler}.
 */
class MessageDispatcher {
    private static final String DATA = "data";
    private static final String ID = "id";

    private final MessageEmitter messageEmitter;
    private final String origin;

    private StringBuffer data = new StringBuffer();
    private String lastEventId;

    public MessageDispatcher(MessageEmitter messageEmitter, String origin) {
        this.messageEmitter = messageEmitter;
        this.origin = origin;
    }

    public void line(String line) {
        int colonIndex;
        if(line.trim().isEmpty()) {
            dispatchEvent();
        } else if(line.startsWith(":")) {
            // ignore
        } else if((colonIndex = line.indexOf(":")) != -1) {
            String field = line.substring(0, colonIndex);
            String value = line.substring(colonIndex + 1).replaceFirst(" ", "");

            if(DATA.equals(field)) {
                data.append(value).append("\n");
            } else if(ID.equals(field)) {
                lastEventId = value;
            }
        }
    }

    private void dispatchEvent() {
        if(data.length() == 0) {
            return;
        }
        String dataString = data.toString();
        if(dataString.endsWith("\n")) {
            dataString = dataString.substring(0, dataString.length()-1);
        }
        MessageEvent e = new MessageEvent(dataString, lastEventId, origin);
        messageEmitter.emitMessage(e);
        data = new StringBuffer();
        lastEventId = null;
    }
}
