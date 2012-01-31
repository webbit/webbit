package org.webbitserver.netty.contrib;

/**
 * @deprecated use {@link org.webbitserver.EventSourceMessage} instead
 */
@Deprecated
public class EventSourceMessage extends org.webbitserver.EventSourceMessage {
    public EventSourceMessage() {
        super();
    }

    public EventSourceMessage(String data) {
        super(data);
    }
}
