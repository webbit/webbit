package org.webbitserver.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;

import java.lang.Thread.UncaughtExceptionHandler;

public class NettyWebSocketHandshakeHandler {

    public NettyWebSocketHandshakeHandler(
            WebSocketHandler webSocketHandler,
            ChannelHandlerContext ctx,
            UncaughtExceptionHandler exceptionHandler,
            NettyWebSocketConnection webSocketConnection,
            HttpRequest req,
            HttpResponse res,
            WebSocketConnectionHandler webSocketConnectionHandler
    ) {
        prepareConnection(req, res, ctx, webSocketConnection, webSocketConnectionHandler);

        try {
            webSocketHandler.onOpen(webSocketConnection);
        } catch (Exception e) {
            exceptionHandler.uncaughtException(Thread.currentThread(), new WebbitException(e));
        }
    }

    private void prepareConnection(HttpRequest req, HttpResponse res, ChannelHandlerContext ctx, NettyWebSocketConnection webSocketConnection, ChannelHandler webSocketConnectionHandler) {
        WebSocketVersion[] versions = new WebSocketVersion[]{
                new Hybi(req, res),
                new Hixie76(req, res),
                new Hixie75(req, res)
        };

        Channel channel = ctx.getChannel();
        ChannelPipeline pipeline = channel.getPipeline();

        for (WebSocketVersion webSocketVersion : versions) {
            if (webSocketVersion.matches()) {
                getReadyToReceiveWebSocketMessages(HybiWebSocketFrameDecoder.serverSide(), webSocketConnectionHandler, pipeline, channel);
                webSocketVersion.prepareHandshakeResponse(webSocketConnection);
                channel.write(res);
                getReadyToSendWebSocketMessages(new HybiWebSocketFrameEncoder(), pipeline);
                break;
            }
        }
    }

    private void getReadyToReceiveWebSocketMessages(ChannelHandler webSocketFrameDecoder, ChannelHandler webSocketConnectionHandler, ChannelPipeline p, Channel channel) {
        StaleConnectionTrackingHandler staleConnectionTracker = (StaleConnectionTrackingHandler) p.remove("staleconnectiontracker");
        staleConnectionTracker.stopTracking(channel);
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", webSocketFrameDecoder);
        p.replace("handler", "wshandler", webSocketConnectionHandler);
    }

    private void getReadyToSendWebSocketMessages(ChannelHandler webSocketFrameEncoder, ChannelPipeline p) {
        p.replace("encoder", "wsencoder", webSocketFrameEncoder);
    }
}
