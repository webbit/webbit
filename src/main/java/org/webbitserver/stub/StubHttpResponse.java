package org.webbitserver.stub;

import org.webbitserver.HttpResponse;
import org.webbitserver.helpers.DateHelper;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of HttpResponse that is easy to construct manually, and inspect results.
 * Useful for testing.
 */
public class StubHttpResponse implements HttpResponse {

    private Charset charset = Charset.forName("UTF-8");
    private int status = 200;
    private Map<String, String> headers = new HashMap<String, String>();
    private Throwable error;
    private boolean ended;
    private ByteArrayOutputStream contents = new ByteArrayOutputStream();
    private List<HttpCookie> cookies = new ArrayList<HttpCookie>();


    @Override
    public StubHttpResponse charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public Charset charset() {
        return charset;
    }

    @Override
    public StubHttpResponse chunked() {
        header(Names.TRANSFER_ENCODING, Values.CHUNKED);
        return this;
    }

    @Override
    public StubHttpResponse status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public StubHttpResponse header(String name, String value) {
        if (value == null) {
            headers.remove(name);
        } else {
            headers.put(name, value);
        }
        return this;
    }

    @Override
    public StubHttpResponse header(String name, long value) {
        return header(name, String.valueOf(value));
    }

    @Override
    public StubHttpResponse header(String name, Date value) {
        return header(name, DateHelper.rfc1123Format(value));
    }

    @Override
    public StubHttpResponse cookie(HttpCookie httpCookie) {
        cookies.add(httpCookie);
        return this;
    }

    public StubHttpResponse cookie(String name, String value) {
        return cookie(new HttpCookie(name, value));
    }

    public String header(String name) {
        return headers.get(name);
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public StubHttpResponse content(String content) {
        return content(content.getBytes(charset));
    }

    @Override
    public long contentLength() {
        return contents.size();
    }

    @Override
    public StubHttpResponse write(String content) {
        return content(content);
    }

    @Override
    public StubHttpResponse content(byte[] content) {
        try {
            contents.write(content);
        } catch (IOException e) {
            throw new Error(e);
        }
        return this;
    }

    @Override
    public StubHttpResponse content(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            contents.write(buffer.get());
        }
        return this;
    }

    public byte[] contents() {
        return contents.toByteArray();
    }

    public String contentsString() {
        return new String(contents(), charset);
    }

    @Override
    public StubHttpResponse error(Throwable error) {
        this.error = error;
        status = 500;
        String message = error.toString();
        this.content(message);
        header("Content-Type", "text/plain");
        header("Content-Length", message.length());
        ended = true;
        return this;
    }

    public Throwable error() {
        return error;
    }

    @Override
    public StubHttpResponse end() {
        ended = true;
        return this;
    }

    public boolean ended() {
        return ended;
    }

    public List<HttpCookie> cookies() {
        return cookies;
    }

    @Override
    public String toString() {
        return "StubHttpResponse{" +
                "charset=" + charset +
                ", status=" + status +
                ", headers=" + headers +
                ", error=" + error +
                ", ended=" + ended +
                ", contents=" + contentsString() +
                '}';
    }
}
