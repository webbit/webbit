package org.webbitserver;

import java.util.Map;
import java.util.Set;

/**
 * Objects implementing this interface can have arbitrary named values associated with
 * them, making it easy to pass data around an application.
 *
 * @author Joe Walnes
 */
public interface DataHolder {

    /**
     * Arbitrary data that can be stored for the lifetime of the connection.
     */
    Map<String, Object> data();

    /**
     * Retrieve data value by key.
     *
     * @see #data()
     */
    Object data(String key);

    /**
     * Store data value by key.
     *
     * @see #data()
     */
    DataHolder data(String key, Object value);

    /**
     * List data keys.
     *
     * @see #data()
     */
    Set<String> dataKeys();

}
