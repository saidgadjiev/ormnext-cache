package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.ObjectCache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache with LRU caching algorithm.
 *
 * @author Said Gadjiev
 */
public class LRUObjectCache implements ObjectCache {

    /**
     * Default cache size.
     */
    private static final int DEFAULT_CACHE_SIZE = 16;

    /**
     * Cached classes map.
     */
    private Map<Class<?>, Map<Object, Object>> cache = new ConcurrentHashMap<>();

    /**
     * Max cache size.
     */
    private int maxSize;

    /**
     * Create a new instance.
     *
     * @param maxSize target max size
     */
    public LRUObjectCache(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Create a new instance with default max size.
     */
    public LRUObjectCache() {
        this.maxSize = DEFAULT_CACHE_SIZE;
    }

    @Override
    public void registerClass(Class<?> tClass) {
        cache.computeIfAbsent(tClass, k -> createLRUMap(maxSize));
    }

    @Override
    public void put(Class<?> tClass, Object id, Object data) {
        Map<Object, Object> objectCache = cache.get(tClass);

        if (objectCache != null) {
            objectCache.put(id, data);
        }
    }

    @Override
    public Object get(Class<?> tClass, Object id) {
        Map<Object, Object> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return null;
        }

        return objectCache.get(id);
    }

    @Override
    public Collection<Object> getAll(Class<?> aClass) {
        return cache.get(aClass).values();
    }

    @Override
    public boolean contains(Class<?> tClass, Object id) {
        Map<Object, Object> objectCache = cache.get(tClass);

        return objectCache != null && objectCache.containsKey(id);
    }

    @Override
    public void invalidate(Class<?> tClass, Object id) {
        Map<Object, Object> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return;
        }
        objectCache.remove(id);
    }

    @Override
    public void invalidateAll(Class<?> tClass) {
        cache.remove(tClass);
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
        Map<Object, Object> objectCache = cache.get(tClass);

        if (objectCache == null) {
            return 0;
        }

        return objectCache.size();
    }

    /**
     * Create a new LRU map.
     *
     * @param maxSize target max size
     * @return created map with LRU remove algorithm
     */
    private Map<Object, Object> createLRUMap(int maxSize) {
        return new LinkedHashMap<Object, Object>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                return size() > maxSize;
            }
        };
    }
}
