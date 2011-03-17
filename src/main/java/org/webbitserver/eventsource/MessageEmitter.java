package org.webbitserver.eventsource;

interface MessageEmitter {
    void emitMessage(final MessageEvent e);
}
