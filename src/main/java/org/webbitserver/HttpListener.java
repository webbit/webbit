package org.webbitserver; 

/**
 * An HttpChannel lets you monitor onclose and onopen events
 */
public interface HttpListener extends HttpHandler {
        /*
         * called when a channel is opened
         */
        public void onOpen (Integer channelId, HttpRequest request);
        /*
         * called when a channel is closed
         */
        public void onClose (Integer channelId);
}