package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;
import ru.saidgajiev.ormnext.cache.commons.DigestHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Select statement cache.
 *
 * @author Said Gadjiev
 */
public class SelectStatementCache {

    /**
     * Select statement list results cache.
     */
    private final Map<Class<?>, Map<String, List<Object>>> listCache = new ConcurrentHashMap<>();

    /**
     * Limited select statement cache.
     */
    private final Map<Class<?>, Map<String, List<Object>>> limitedListCache = new ConcurrentHashMap<>();

    /**
     * Long results cache.
     */
    private final Map<Class<?>, Map<String, Long>> longCache = new ConcurrentHashMap<>();

    /**
     * Digest helper.
     */
    private final DigestHelper digestHelper;

    /**
     * Create a new instance.
     *
     * @param metaModel      target meta model
     * @param databaseEngine target database engine
     */
    SelectStatementCache(MetaModel metaModel, DatabaseEngine<?> databaseEngine) {
        digestHelper = new DigestHelper(metaModel, databaseEngine);
    }

    /**
     * Put list result object ids to cache.
     *
     * @param selectStatement target select statement
     * @param resultObjectIds target result object ids.
     */
    public void putList(SelectStatement<?> selectStatement, List<Object> resultObjectIds) {
        listCache.putIfAbsent(selectStatement.getEntityClass(), new ConcurrentHashMap<>());

        listCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), resultObjectIds);
    }

    /**
     * Retrieve cached result ids.
     *
     * @param selectStatement target select statement
     * @return result ids
     */
    public List<Object> getList(SelectStatement<?> selectStatement) {
        Map<String, List<Object>> cache = listCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    /**
     * Evict cached list result ids by entity type.
     *
     * @param entityType target entity type
     */
    public void evictList(Class<?> entityType) {
        listCache.remove(entityType);
    }

    /**
     * Evict all list results.
     */
    public void evictList() {
        listCache.clear();
    }

    /**
     * Cache long result.
     *
     * @param selectStatement target select statement
     * @param result          target long result
     */
    public void putLong(SelectStatement<?> selectStatement, Long result) {
        longCache.putIfAbsent(selectStatement.getEntityClass(), new ConcurrentHashMap<>());

        longCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), result);
    }

    /**
     * Retrieve long result.
     *
     * @param selectStatement target select statement
     * @return long result
     */
    public Long getLong(SelectStatement<?> selectStatement) {
        Map<String, Long> cache = longCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    /**
     * Evict long result by entity type.
     *
     * @param entityType target entity type
     */
    public void evictLong(Class<?> entityType) {
        longCache.remove(entityType);
    }

    /**
     * Evict long results.
     */
    public void evictLong() {
        longCache.clear();
    }

    /**
     * Put limited result to cache.
     *
     * @param selectStatement target select statement
     * @param list            target limited result ids
     */
    public void putLimitedList(SelectStatement<?> selectStatement, List<Object> list) {
        limitedListCache.putIfAbsent(selectStatement.getEntityClass(), new ConcurrentHashMap<>());

        limitedListCache.get(selectStatement.getEntityClass()).put(digestHelper.digest(selectStatement), list);
    }

    /**
     * Retrieve limited list results.
     *
     * @param selectStatement target select statement
     * @return limited list results
     */
    public List<Object> getLimitedList(SelectStatement<?> selectStatement) {
        Map<String, List<Object>> cache = limitedListCache.get(selectStatement.getEntityClass());

        return cache == null ? null : cache.get(digestHelper.digest(selectStatement));
    }

    /**
     * Evict limited list.
     *
     * @param entityType target entity type
     */
    public void evictLimitedList(Class<?> entityType) {
        limitedListCache.remove(entityType);
    }

    /**
     * Evict limited list results.
     */
    public void evictLimitedList() {
        limitedListCache.clear();
    }

    /**
     * Evict all caches by entity type.
     *
     * @param entityType target entity type
     */
    public void evictAll(Class<?> entityType) {
        synchronized (this) {
            listCache.remove(entityType);
            longCache.remove(entityType);
            limitedListCache.remove(entityType);
        }
    }

    /**
     * Evict all caches.
     */
    public void flush() {
        synchronized (this) {
            listCache.clear();
            longCache.clear();
            limitedListCache.clear();
        }
    }
}
