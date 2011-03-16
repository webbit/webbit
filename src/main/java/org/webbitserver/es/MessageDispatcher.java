package org.webbitserver.es;

/**
 * <a href="http://dev.w3.org/html5/eventsource/#event-stream-interpretation">Interprets an event stream</a>
 * and dispatches messages to the {@link EventSourceHandler}.
 */
class MessageDispatcher {
    private final EventSourceHandler eventSourceHandler;
    private StringBuffer data = new StringBuffer();

    public MessageDispatcher(EventSourceHandler eventSourceHandler) {
        this.eventSourceHandler = eventSourceHandler;
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

            if(field.equals("data")) {
                data.append(value).append("\n");
            }
        }
    }

    private void dispatchEvent() {
        String dataString = data.toString();
        if(dataString.endsWith("\n")) {
            dataString = dataString.substring(0, dataString.length()-1);
        }
        MessageEvent e = new MessageEvent(dataString);
        eventSourceHandler.onMessage(e);
        data = new StringBuffer();
    }
}
