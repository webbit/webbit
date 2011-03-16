package org.webbitserver.es;

import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
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

class EventSourceClientHandler extends SimpleChannelUpstreamHandler {
    private URI url;
    private EventSourceHandler eventSourceHandler;
    private boolean handshakeCompleted = false;
    private Channel channel;
    private MessageDispatcher messageDispatcher;

    public EventSourceClientHandler(URI url, EventSourceHandler eventSourceHandler) {
        this.url = url;
        this.eventSourceHandler = eventSourceHandler;
        this.messageDispatcher = new MessageDispatcher(eventSourceHandler);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.getPath());
        request.addHeader(Names.ACCEPT, "text/event-stream");
        request.addHeader(Names.HOST, url.getHost());
        request.addHeader(Names.ORIGIN, "http://" + url.getHost());
        request.addHeader(Names.CACHE_CONTROL, "no-cache");
        e.getChannel().write(request);
        channel = e.getChannel();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        eventSourceHandler.onDisconnect();
        handshakeCompleted = false;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!handshakeCompleted) {
            HttpResponse response = (HttpResponse) e.getMessage();

            final boolean validStatus = response.getStatus().getCode() == 200;
            final boolean validUpgrade = response.getHeader(Names.CONTENT_TYPE).equals("text/event-stream");

            if (!validStatus || !validUpgrade) {
                throw new EventSourceException("Invalid response:" + response.toString());
            }

            handshakeCompleted = true;
            ctx.getPipeline().replace("decoder", "es-decoder", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            eventSourceHandler.onConnect();
            return;
        }

        BigEndianHeapChannelBuffer frame = (BigEndianHeapChannelBuffer) e.getMessage();
        String line = frame.toString(Charset.forName("UTF-8"));
        messageDispatcher.line(line);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        eventSourceHandler.onError(e.getCause());
    }

    public ChannelFuture close() {
        return channel.close();
    }
}