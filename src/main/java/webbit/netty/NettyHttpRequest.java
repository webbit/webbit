package webbit.netty;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class NettyHttpRequest implements webbit.HttpRequest {

    private final HttpRequest httpRequest;
    private final MessageEvent messageEvent;
    private final Map<String, Object> data = new HashMap<String, Object>();

    public NettyHttpRequest(MessageEvent messageEvent, HttpRequest httpRequest) {
        this.messageEvent = messageEvent;
        this.httpRequest = httpRequest;
    }

    @Override
    public String uri() {
        return httpRequest.getUri();
    }

    @Override
    public String header(String name) {
        return httpRequest.getHeader(name);
    }

    @Override
    public boolean hasHeader(String name) {
        return httpRequest.containsHeader(name);
    }

    @Override
    public String method() {
        return httpRequest.getMethod().getName();
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public String toString() {
        return messageEvent.getRemoteAddress() + " " + httpRequest.getMethod() + " " + httpRequest.getUri();
    }
}
