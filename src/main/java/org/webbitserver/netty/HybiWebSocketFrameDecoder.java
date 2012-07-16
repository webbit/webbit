package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.webbitserver.helpers.UTF8Exception;
import org.webbitserver.helpers.UTF8Output;

import static org.webbitserver.netty.HybiWebSocketFrameDecoder.State.CORRUPT;
import static org.webbitserver.netty.HybiWebSocketFrameDecoder.State.FRAME_START;
import static org.webbitserver.netty.HybiWebSocketFrameDecoder.State.MASKING_KEY;
import static org.webbitserver.netty.HybiWebSocketFrameDecoder.State.PAYLOAD;
import static org.webbitserver.netty.Opcodes.OPCODE_BINARY;
import static org.webbitserver.netty.Opcodes.OPCODE_CLOSE;
import static org.webbitserver.netty.Opcodes.OPCODE_CONT;
import static org.webbitserver.netty.Opcodes.OPCODE_PING;
import static org.webbitserver.netty.Opcodes.OPCODE_PONG;
import static org.webbitserver.netty.Opcodes.OPCODE_TEXT;

public class HybiWebSocketFrameDecoder extends ReplayingDecoder<HybiWebSocketFrameDecoder.State> {
    private final UTF8Output utf8Output = new UTF8Output();
    private final boolean isServer;
    private final boolean requireMaskedInboundFrames;
    private final byte[] outboundMaskingKey;

    private boolean frameFin;
    private int frameOpcode;
    private long framePayloadLen;
    private byte[] inboundMaskingKey;

    private DecodingHybiFrame currentFrame;

    public static enum State {
        FRAME_START,
        MASKING_KEY,
        PAYLOAD,
        CORRUPT
    }

    public static HybiWebSocketFrameDecoder serverSide() {
        return new HybiWebSocketFrameDecoder(true, null);
    }

    public static HybiWebSocketFrameDecoder clientSide(byte[] outboundMaskingKey) {
        return new HybiWebSocketFrameDecoder(false, outboundMaskingKey);
    }

    private HybiWebSocketFrameDecoder(boolean isServer, byte[] outboundMaskingKey) {
        super(FRAME_START);
        this.isServer = isServer;
        this.requireMaskedInboundFrames = isServer;
        this.outboundMaskingKey = outboundMaskingKey;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, final Channel channel, ChannelBuffer buffer, State state) throws Exception {
        switch (state) {
            case FRAME_START: {
                inboundMaskingKey = null;
                // FIN, RSV, OPCODE
                int b = buffer.readByte();
                frameFin = (b & 0x80) != 0;
                int frameRsv = (b & 0x70) >> 4;
                frameOpcode = (b & 0x0F);

                // MASK, PAYLOAD LEN 1
                b = buffer.readByte();
                boolean frameMasked = (b & 0x80) != 0;
                int framePayloadLen1 = (b & 0x7F);

                if (frameRsv != 0) {
                    protocolViolation(channel, "RSV != 0 and no extension negotiated, RSV:" + frameRsv);
                    return null;
                }

                if (isServer && requireMaskedInboundFrames && !frameMasked) {
                    protocolViolation(channel, "Received unmasked frame");
                    return null;
                }

                if (frameOpcode > 7) { // control frame (have MSB in opcode set)

                    // control frames MUST NOT be fragmented
                    if (!frameFin) {
                        protocolViolation(channel, "fragmented control frame");
                        return null;
                    }

                    // control frames MUST have payload 125 octets or less
                    if (framePayloadLen1 > 125) {
                        protocolViolation(channel, "control frame with payload length > 125 octets");
                        return null;
                    }

                    // check for reserved control frame opcodes
                    if (!(frameOpcode == OPCODE_CLOSE || frameOpcode == OPCODE_PING || frameOpcode == OPCODE_PONG)) {
                        protocolViolation(channel, "control frame using reserved opcode " + frameOpcode);
                        return null;
                    }

                    // close frame : if there is a body, the first two bytes of the body MUST be a 2-byte
                    // unsigned integer (in network byte order) representing a status code
                    if (frameOpcode == 8 && framePayloadLen1 == 1) {
                        protocolViolation(channel, "received close control frame with payload len 1");
                        return null;
                    }
                } else { // data frame
                    // check for reserved data frame opcodes
                    if (!(frameOpcode == OPCODE_CONT || frameOpcode == OPCODE_TEXT || frameOpcode == OPCODE_BINARY)) {
                        protocolViolation(channel, "data frame using reserved opcode " + frameOpcode);
                        return null;
                    }

                    // check opcode vs message fragmentation state 1/2
                    if (currentFrame == null && frameOpcode == OPCODE_CONT) {
                        protocolViolation(channel, "received continuation data frame outside fragmented message");
                        return null;
                    }

                    // check opcode vs message fragmentation state 2/2
                    if (currentFrame != null && frameOpcode != OPCODE_CONT) {
                        protocolViolation(channel, "received non-continuation data frame while inside fragmented message");
                        return null;
                    }
                }

                if (framePayloadLen1 == 126) {
                    framePayloadLen = buffer.readUnsignedShort();
                    if (framePayloadLen < 126) {
                        protocolViolation(channel, "invalid data frame length (not using minimal length encoding)");
                        return null;
                    }
                } else if (framePayloadLen1 == 127) {
                    framePayloadLen = buffer.readLong();
                    // TODO: check if it's bigger than 0x7FFFFFFFFFFFFFFF, Maybe just check if it's negative?

                    if (framePayloadLen < 65536) {
                        protocolViolation(channel, "invalid data frame length (not using minimal length encoding)");
                        return null;
                    }
                } else {
                    framePayloadLen = framePayloadLen1;
                }
                if (frameMasked) {
                    checkpoint(MASKING_KEY);
                } else {
                    checkpoint(PAYLOAD);
                    return null;
                }
            }
            case MASKING_KEY: {
                inboundMaskingKey = buffer.readBytes(4).array();
                checkpoint(PAYLOAD);
            }
            case PAYLOAD: {
                ChannelBuffer frame = buffer.readBytes(toFrameLength(framePayloadLen));
                if (inboundMaskingKey != null) {
                    applyMask(frame, inboundMaskingKey);
                }
                checkpoint(FRAME_START);

                if (frameOpcode == OPCODE_CLOSE) {
                    EncodingHybiFrame close = new EncodingHybiFrame(OPCODE_CLOSE, true, 0, outboundMaskingKey, ChannelBuffers.buffer(0));
                    channel.write(close).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            channel.close();
                        }
                    });
                    return null;
                } else if (frameOpcode == OPCODE_CONT) {
                    try {
                        currentFrame.append(frame);
                    } catch (UTF8Exception e) {
                        protocolViolation(channel, "invalid UTF-8 bytes");
                    }
                } else if (frameOpcode == OPCODE_PING || frameOpcode == OPCODE_PONG) {
                    return new DecodingHybiFrame(frameOpcode, utf8Output, frame);
                } else {
                    try {
                        currentFrame = new DecodingHybiFrame(frameOpcode, utf8Output, frame);
                    } catch (UTF8Exception e) {
                        protocolViolation(channel, "invalid UTF-8 bytes");
                    }
                }

                if (frameFin) {
                    DecodingHybiFrame result = currentFrame;
                    currentFrame = null;
                    return result;
                } else {
                    return null;
                }
            }
            case CORRUPT: {
                // If we don't keep reading Netty will throw an exception saying
                // we can't return null if no bytes read and state not changed.
                buffer.readByte();
                return null;
            }
            default:
                throw new Error("Shouldn't reach here.");
        }
    }

    static void applyMask(ChannelBuffer data, byte[] maskingKey) {
        int length = data.writerIndex();
        for (int i = 0; i < length; i++) {
            data.setByte(i, data.getByte(i) ^ maskingKey[i % 4]);
        }
    }

    private void protocolViolation(Channel channel, String reason) throws CorruptedFrameException {
        checkpoint(CORRUPT);
        if (channel.isConnected()) {
            channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            channel.close().awaitUninterruptibly();
        }
        throw new CorruptedFrameException(reason);
    }

    private int toFrameLength(long l) throws TooLongFrameException {
        if (l > Integer.MAX_VALUE) {
            throw new TooLongFrameException("Length:" + l);
        } else {
            return (int) l;
        }
    }
}
