package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HybiWebSocketFrameDecoderTest {
    @Ignore
    @Test
    public void decodes_an_86_kb_message() throws Exception {
        DecoderEmbedder<WebSocketFrame> decoder = new DecoderEmbedder<WebSocketFrame>(new HybiWebSocketFrameDecoder());
        decoder.offer(payloadFromFile("/org/webbitserver/ws-payload-86kb.bin"));
        WebSocketFrame frame = decoder.poll();
        assertNotNull(frame);
    }

    private ChannelBuffer payloadFromFile(String path) throws IOException {
        InputStream payloadStream = getClass().getResourceAsStream(path);
        int available = payloadStream.available();
        byte[] payloadBytes = new byte[available];
        assertEquals(available, payloadStream.read(payloadBytes));
        return ChannelBuffers.wrappedBuffer(payloadBytes);
    }
}
