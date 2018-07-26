package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;
import ru.saidgajiev.ormnext.cache.commons.DigestHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelectStatementCache {

    private final Map<Class<?>, Map<String, List<Object>>> listCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Map<String, List<Object>>> limitedListCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Map<String, Long>> longCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Map<String, Object>> uniqueResultCache = new ConcurrentHashMap<>();

    private final DigestHelper digestHelper;

    SelectStatementCache(MetaModel metaModel, DatabaseEngine<?> databaseEngine) {
        digestHelper = new DigestHelper(metaModel, databaseEngine);
    }

    public synchronized void putList(SelectStatement<?> selectStatement, List<Object> resultObjectIds) {
        listCache.putIfAbsent(selectStatement.getEntityClass(), new HashMap<>());

        listCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), resultObjectIds);
    }

    public List<Object> getList(SelectStatement<?> selectStatement) {
        Map<String, List<Object>> cache = listCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    public void evictList(SelectStatement<?> selectStatement) {
        listCache.get(selectStatement.getEntityClass()).remove(digestHelper.digest(selectStatement));
    }

    public void evictList(Class<?> entityType) {
        listCache.remove(entityType);
    }

    public void evictList() {
        listCache.clear();
    }

    public synchronized void putLong(SelectStatement<?> selectStatement, Long result) {
        longCache.putIfAbsent(selectStatement.getEntityClass(), new HashMap<>());

        longCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), result);
    }

    public Long getLong(SelectStatement<?> selectStatement) {
        Map<String, Long> cache = longCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    public void evictLong(SelectStatement<?> selectStatement) {
        longCache.get(selectStatement.getEntityClass()).remove(digestHelper.digest(selectStatement));
    }

    public void evictLong(Class<?> entityType) {
        longCache.remove(entityType);
    }

    public void evictLong() {
        longCache.clear();
    }

    public synchronized void putUniqueResult(SelectStatement<?> selectStatement, Object id) {
        uniqueResultCache.putIfAbsent(selectStatement.getEntityClass(), new HashMap<>());

        uniqueResultCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), id);
    }

    public Object getUniqueResult(SelectStatement<?> selectStatement) {
        Map<String, Object> cache = uniqueResultCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    public void evictUniqueResult(SelectStatement<?> selectStatement) {
        uniqueResultCache.get(selectStatement.getEntityClass()).remove(digestHelper.digest(selectStatement));
    }

    public void evictUniqueResult(Class<?> entityClass) {
        uniqueResultCache.remove(entityClass);
    }

    public void evictUniqueResult() {
        uniqueResultCache.clear();
    }

    public void putLimitedList(SelectStatement<?> selectStatement, List<Object> list) {
        limitedListCache.putIfAbsent(selectStatement.getEntityClass(), new HashMap<>());

        limitedListCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), list);
    }

    public List<Object> getLimitedList(SelectStatement<?> selectStatement) {
        Map<String, List<Object>> cache = limitedListCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    public void evictLimitedList(SelectStatement<?> selectStatement) {
        Map<String, List<Object>> cache = limitedListCache.get(selectStatement.getEntityClass());

        if (cache != null) {
            cache.remove(digestHelper.digest(selectStatement));
        }
    }

    public void evictLimitedList(Class<?> tClass) {
        limitedListCache.remove(tClass);
    }

    public void evictLimitedList() {
        limitedListCache.clear();
    }

    public void evictAll(Class<?> entityType) {
        listCache.remove(entityType);
        longCache.remove(entityType);
        uniqueResultCache.remove(entityType);
        limitedListCache.remove(entityType);
    }

    public void flush() {
        listCache.clear();
        longCache.clear();
        uniqueResultCache.clear();
        limitedListCache.clear();
    }
}
