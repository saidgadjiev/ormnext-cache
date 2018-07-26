package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.ObjectCache;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This cache implementation use {@link SoftReference} for cached values.
 *
 * @author Said Gadjiev
 */
public class ReferenceObjectCache implements ObjectCache {

    /**
     * Map for cache values.
     */
    private Map<Class<?>, Map<Object, SoftReference<Object>>> cache = new ConcurrentHashMap<>();

    @Override
    public void registerClass(Class<?> tClass) {
        cache.computeIfAbsent(tClass, k -> new HashMap<>());
    }

    @Override
    public void put(Class<?> tClass, Object id, Object data) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        if (objectCache != null) {
            objectCache.put(id, new SoftReference<>(data));
        }
    }

    @Override
    public Object get(Class<?> tClass, Object id) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return null;
        }
        Reference<Object> ref = objectCache.get(id);

        if (ref == null) {
            return null;
        }
        Object obj = ref.get();

        if (obj == null) {
            objectCache.remove(id);

            return null;
        } else {
            return obj;
        }
    }

    @Override
    public Collection<Object> getAll(Class<?> tClass) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return null;
        }
        List<Object> result = new ArrayList<>();

        for (Object id: objectCache.keySet()) {
            result.add(get(tClass, id));
        }

        return result;
    }

    @Override
    public boolean contains(Class<?> tClass, Object id) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        return objectCache != null && objectCache.containsKey(id);
    }

    @Override
    public void invalidate(Class<?> tClass, Object id) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return;
        }
        objectCache.remove(id);
    }

    @Override
    public void invalidateAll(Class<?> tClass) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return;
        }
        objectCache.clear();
    }

    @Override
    public void invalidateAll() {
        cache.forEach((key, value) -> value.clear());
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public long size(Class<?> tClass) {
        Map<Object, SoftReference<Object>> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return 0;
        }

        return objectCache.size();
    }
}
