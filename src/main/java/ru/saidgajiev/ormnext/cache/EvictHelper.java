package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.CacheEvict;
import ru.saidgadjiev.ormnext.core.cache.ObjectCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evict api implementation.
 *
 * @author Said Gadjiev
 */
public class EvictHelper implements CacheEvict {

    /**
     * Object cache map.
     */
    private Map<Class<?>, ObjectCache> objectCacheMap = new HashMap<>();

    /**
     * Query for all results cache.
     */
    private Map<Class<?>, List<Object>> queryForAllCache = new HashMap<>();

    /**
     * Count off results cache.
     */
    private Map<Class<?>, Long> countOffCache = new HashMap<>();

    /**
     * Exist results cache.
     */
    private Map<Class<?>, Map<Object, Boolean>> existCache = new HashMap<>();

    /**
     * Select statement cache.
     */
    private SelectStatementCache selectStatementCache;

    /**
     * Create a new instance.
     *
     * @param objectCacheMap target object cache map
     * @param queryForAllCache target query for all cache
     * @param countOffCache target count off cache
     * @param existCache target exist cache
     * @param selectStatementCache target select statement cache
     */
    EvictHelper(Map<Class<?>, ObjectCache> objectCacheMap,
                       Map<Class<?>, List<Object>> queryForAllCache,
                       Map<Class<?>, Long> countOffCache,
                       Map<Class<?>, Map<Object, Boolean>> existCache,
                       SelectStatementCache selectStatementCache
    ) {
        this.objectCacheMap = objectCacheMap;
        this.queryForAllCache = queryForAllCache;
        this.countOffCache = countOffCache;
        this.existCache = existCache;
        this.selectStatementCache = selectStatementCache;
    }

    @Override
    public void evictList(Class<?> entityType) {
        selectStatementCache.evictList(entityType);
    }

    @Override
    public void evictList() {
        selectStatementCache.evictList();
    }

    @Override
    public void evictQueryForLong(Class<?> entityType) {
        selectStatementCache.evictLong(entityType);
    }

    @Override
    public void evictQueryForLong() {
        selectStatementCache.evictLong();
    }

    @Override
    public void evictCountOff(Class<?> entityType) {
        countOffCache.remove(entityType);
    }

    @Override
    public void evictCountOff() {
        countOffCache.clear();
    }

    @Override
    public void evictExist(Class<?> entityType) {
        existCache.remove(entityType);
    }

    @Override
    public void evictExist() {
        existCache.clear();
    }

    @Override
    public void evictQueryForAll(Class<?> entityType) {
        queryForAllCache.remove(entityType);
    }

    @Override
    public void evictQueryForAll() {
        queryForAllCache.clear();
    }

    @Override
    public void evictLimitedList(Class<?> entityType) {
        selectStatementCache.evictLimitedList(entityType);
    }

    @Override
    public void evictLimitedList() {
        selectStatementCache.evictLimitedList();
    }

    @Override
    public void evict(Class<?> entityType, Object id) {
        objectCacheMap.get(entityType).invalidate(entityType, id);
    }

    @Override
    public void evict(Class<?> entityType) {
        objectCacheMap.get(entityType).invalidateAll(entityType);
        evictList(entityType);
        evictQueryForAll(entityType);
    }

    @Override
    public void evict() {
        objectCacheMap.forEach((aClass, objectCache) -> {
            objectCache.invalidateAll(aClass);
            evictList(aClass);
            evictQueryForAll(aClass);
        });
    }

    @Override
    public void evictAll(Class<?> entityType) {
        selectStatementCache.evictAll(entityType);
        queryForAllCache.remove(entityType);
        objectCacheMap.get(entityType).invalidateAll(entityType);
        countOffCache.remove(entityType);
        existCache.remove(entityType);
    }

    @Override
    public void flush() {
        queryForAllCache.clear();
        selectStatementCache.flush();
        objectCacheMap.forEach((aClass, objectCache) -> objectCache.invalidateAll(aClass));
        countOffCache.clear();
        existCache.clear();
    }
}
