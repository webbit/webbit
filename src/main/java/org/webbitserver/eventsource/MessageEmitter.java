package org.webbitserver.eventsource;

interface MessageEmitter {
    void emitMessage(String event, final MessageEvent message);

    void setReconnectionTime(long reconnectionTimeMillis);
}
