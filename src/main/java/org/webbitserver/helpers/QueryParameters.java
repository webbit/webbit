package org.webbitserver.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class QueryParameters {
    private final Map<String, List<String>> params = new HashMap<String, List<String>>();
    private static final List<String> EMPTY = Collections.emptyList();

    public QueryParameters(String query) {
        if (query != null) {
            parse(query);
        }
    }

    private void parse(String query) {
        try {
            // StringTokenizer is faster than split. http://www.javamex.com/tutorials/regular_expressions/splitting_tokenisation_performance.shtml
            StringTokenizer st = new StringTokenizer(query, "&");
            while (st.hasMoreTokens()) {
                String[] pair = st.nextToken().split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = pair.length == 1 ? null : URLDecoder.decode(pair[1], "UTF-8");
                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't parse query string: " + query, e);
        }
    }

    public String first(String key) {
        List<String> all = all(key);
        return all.isEmpty() ? null : all.get(0);
    }

    public List<String> all(String key) {
        return params.containsKey(key) ? params.get(key) : EMPTY;
    }

    public Set<String> keys() {
        return params.keySet();
    }
}
