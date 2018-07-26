package ru.saidgajiev.ormnext.cache;

import java.util.*;

public class ResultSetMap {

    List<Object, Object> values = new LinkedHashMap<>();

    Iterator<Map.Entry<Object, Object>> iterator;

    public ResultSetMap() {
        iterator = values.entrySet().iterator();
    }

    public boolean next() {
        return iterator.hasNext();
    }

    public Object get(int columnId) {
        return iterator.next();
    }

    public Object get(String columnLabel) {
        return values.get(columnLabel);
    }

    public
}
