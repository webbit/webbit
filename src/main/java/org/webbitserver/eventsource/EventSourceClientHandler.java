package org.webbitserver.eventsource;

import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;

class EventSourceClientHandler extends SimpleChannelUpstreamHandler implements MessageEmitter {

    private final Executor executor;
    private final URI uri;
    private final EventSourceHandler eventSourceHandler;
    private final MessageDispatcher messageDispatcher;

    private boolean needResponse = true;
    private Channel channel;

    public EventSourceClientHandler(Executor executor, URI uri, EventSourceHandler eventSourceHandler) {
        this.executor = executor;
        this.uri = uri;
        this.eventSourceHandler = eventSourceHandler;
        this.messageDispatcher = new MessageDispatcher(this, uri.toString());
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toString());
        request.addHeader(Names.ACCEPT, "text/event-stream");
        request.addHeader(Names.HOST, uri.getHost());
        request.addHeader(Names.ORIGIN, "http://" + uri.getHost());
        request.addHeader(Names.CACHE_CONTROL, "no-cache");
        e.getChannel().write(request);
        channel = e.getChannel();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        emitDisconnect();
        needResponse = true;
        channel = null;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (needResponse) {
            HttpResponse response = (HttpResponse) e.getMessage();

            final boolean validStatus = response.getStatus().getCode() == 200;
            final boolean validUpgrade = response.getHeader(Names.CONTENT_TYPE).equals("text/event-stream");

            if (!validStatus || !validUpgrade) {
                throw new EventSourceException("Invalid response:" + response.toString());
            }

            needResponse = false;
            ctx.getPipeline().replace("decoder", "es-decoder", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            emitConnect();
            return;
        }

        BigEndianHeapChannelBuffer frame = (BigEndianHeapChannelBuffer) e.getMessage();
        String line = frame.toString(Charset.forName("UTF-8"));
        messageDispatcher.line(line);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Throwable error = e.getCause();
        emitError(error);
    }

    public EventSourceClientHandler close() {
        if(channel != null) {
            channel.close();
        }
        return this;
    }

    public EventSourceClientHandler join() throws InterruptedException {
        if(channel != null) {
            channel.getCloseFuture().await();
        }
        return this;
    }

    private void emitConnect() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                eventSourceHandler.onConnect();
            }
        });
    }

    private void emitDisconnect() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                eventSourceHandler.onDisconnect();
            }
        });
    }

    private void emitError(final Throwable error) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                eventSourceHandler.onError(error);
            }
        });
    }

    public void emitMessage(final String event, final org.webbitserver.eventsource.MessageEvent message) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                eventSourceHandler.onMessage(event, message);
            }
        });
    }
}