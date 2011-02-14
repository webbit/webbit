package org.webbitserver.stub;

import org.webbitserver.DataHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StubDataHolder implements DataHolder {

    private Map<String, Object> data = new HashMap<String, Object>();

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public Object data(String key) {
        return data.get(key);
    }

    @Override
    public DataHolder data(String key, Object value) {
        data.put(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return data.keySet();
    }
}
