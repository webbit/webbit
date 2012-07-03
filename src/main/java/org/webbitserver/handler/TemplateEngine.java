package org.webbitserver.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Abstract interface for template engines. It can be passed as an argument to the constructors of
 * {@link StaticFileHandler} and {@link EmbeddedResourceHandler}.
 *
 * Webbit itself doesn't provide any template engines. Look at <a href="https://github.com/webbit/webbit-rest">Webbit-REST</a>
 * for implementations.
 */
public interface TemplateEngine {
    String TEMPLATE_CONTEXT = "TEMPLATE_CONTEXT";

    /**
     * Renders a template.
     * <p/>
     * Most template engines merge a {@code templateContext} with a template to produce output. What constitutes a valid
     * context is template-engine specific.
     * <p/>
     * Webbit will pass the request data value keyed with {@link #TEMPLATE_CONTEXT} as the {@code templateContext} argument.
     * It's the programmer's responsibility to make sure the data value is set before the template is rendered,
     * i.e. before the {@link StaticFileHandler} or {@link EmbeddedResourceHandler} handler instance handles a request.
     *
     * @see org.webbitserver.HttpRequest#data(String)
     * @see org.webbitserver.HttpRequest#data(String, Object)
     * @param length          number of bytes in the template
     * @param template        the template source
     * @param templatePath    the path the template is read from. Allows implementations to cache compiled templates.
     * @param templateContext object to merge into the template
     * @return a rendered template
     */
    ByteBuffer process(int length, InputStream template, String templatePath, Object templateContext) throws IOException;
}
