package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.CacheEvict;
import ru.saidgadjiev.ormnext.core.cache.ObjectCache;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;

import java.util.List;
import java.util.Map;

public class EvictHelper implements CacheEvict {

    private final Map<Class<?>, ObjectCache> objectCacheMap;

    private final Map<Class<?>, List<Object>> queryForAllCache;

    private final Map<Class<?>, Long> countOffCache;

    private final Map<Class<?>, Map<Object, Boolean>> existCache;

    private final SelectStatementCache selectStatementCache;

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
    public void evictList(SelectStatement<?> selectStatement) {
        selectStatementCache.evictList(selectStatement);
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
    public void evictQueryForLong(SelectStatement<?> selectStatement) {
        selectStatementCache.evictLong(selectStatement);
    }

    @Override
    public void evictQueryForLong() {
        selectStatementCache.evictLong();
    }

    @Override
    public void evictUniqueResult(SelectStatement<?> selectStatement) {
        selectStatementCache.evictUniqueResult(selectStatement);
    }

    @Override
    public void evictUniqueResult(Class<?> entityType) {
        selectStatementCache.evictUniqueResult(entityType);
    }

    @Override
    public void evictUniqueResult() {
        selectStatementCache.evictUniqueResult();
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
    public void evictLimitedList(SelectStatement<?> selectStatement) {
        selectStatementCache.evictLimitedList(selectStatement);
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

    @Override
    public ObjectCache getCache(Class<?> entityType) {
        return objectCacheMap.get(entityType);
    }
}
