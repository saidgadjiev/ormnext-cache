package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.Cache;
import ru.saidgadjiev.ormnext.core.cache.CacheEvict;
import ru.saidgadjiev.ormnext.core.cache.ObjectCache;
import ru.saidgadjiev.ormnext.core.connection.DatabaseResults;
import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.field.fieldtype.DatabaseColumnType;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.DeleteStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.Query;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.UpdateStatement;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;

import java.util.*;

public class CacheLayer implements Cache {

    /**
     * Meta model.
     */
    private MetaModel metaModel;

    /**
     * Object cache map.
     */
    private Map<Class<?>, ObjectCache> objectCacheMap = new HashMap<>();

    private Map<Class<?>, List<Object>> queryForAllCache = new HashMap<>();

    private Map<Class<?>, Long> countOffCache = new HashMap<>();

    private Map<Class<?>, Map<Object, Boolean>> existCache = new HashMap<>();

    private SelectStatementCache selectStatementCache;

    private CacheEvict cacheEvict;

    /**
     * Cacheable entities.
     */
    private Collection<Class<?>> cacheableEntities = new ArrayList<>();

    @Override
    public void init(MetaModel metaModel, DatabaseEngine<?> databaseEngine) {
        this.metaModel = metaModel;
        selectStatementCache = new SelectStatementCache(metaModel, databaseEngine);

        for (Class<?> entityType : metaModel.getPersistentClasses()) {
            if (entityType.isAnnotationPresent(Cacheable.class)) {
                cacheableEntities.add(entityType);
            }
        }
        cacheEvict = new EvictHelper(
                objectCacheMap,
                queryForAllCache,
                countOffCache,
                existCache,
                selectStatementCache
        );
    }

    @Override
    public void create(Object object) {
        putToCache(Collections.singletonList(object));
    }

    @Override
    public void create(Collection<Object> objects) {
        putToCache(objects);
    }

    private List<Object> putToCache(Collection<Object> objects) {
        if (objects.isEmpty()) {
            return Collections.emptyList();
        }
        Class<?> entityType = objects.iterator().next().getClass();

        if (isCacheable(entityType)) {
            evictCaches(entityType);

            existCache.putIfAbsent(entityType, new HashMap<>());
            List<Object> ids = new ArrayList<>();

            for (Object object : objects) {
                Object id = extractId(object);

                ids.add(id);
                existCache.get(entityType).put(id, true);
                objectCacheMap.get(entityType).put(entityType, id, object);
            }

            return ids;
        }

        return Collections.emptyList();
    }

    @Override
    public void putToCache(Object id, Object data) {
        if (isCacheable(data.getClass())) {
            evictCaches(data.getClass());

            existCache.putIfAbsent(data.getClass(), new HashMap<>());
            existCache.get(data.getClass()).put(id, true);
            objectCacheMap.get(data.getClass()).put(data.getClass(), id, data);
        }
    }

    @Override
    public void cacheQueryForId(Object id, Object data) {
        putToCache(id, data);
    }

    @Override
    public Optional<Object> queryForId(Class<?> tClass, Object id) {
        if (isCacheable(tClass)) {
            return Optional.ofNullable(objectCacheMap.get(tClass).get(tClass, id));
        }

        return Optional.empty();
    }

    @Override
    public void cacheQueryForAll(Collection<Object> collection) {
        if (collection.isEmpty()) {
            return;
        }
        Class<?> entityType = collection.iterator().next().getClass();

        queryForAllCache.putIfAbsent(entityType, putToCache(collection));
    }

    @Override
    public Optional<List<Object>> queryForAll(Class<?> tClass) {
        if (isCacheable(tClass)) {
            List<Object> ids = queryForAllCache.get(tClass);

            if (ids == null) {
                return Optional.empty();
            }
            List<Object> results = new ArrayList<>();

            for (Object id : ids) {
                Object result = objectCacheMap.get(tClass).get(tClass, id);

                if (result != null) {
                    results.add(result);
                }
            }

            return Optional.of(results);
        }

        return Optional.empty();
    }

    @Override
    public void update(Object o) {
        if (isCacheable(o.getClass())) {
            evictApi().evictList(o.getClass());
            evictApi().evictLimitedList(o.getClass());
            evictApi().evictQueryForLong(o.getClass());
            objectCacheMap.get(o.getClass()).put(o.getClass(), extractId(o), o);
        }
    }

    @Override
    public void deleteById(Class<?> tClass, Object id) {
        if (isCacheable(tClass)) {
            objectCacheMap.get(tClass).invalidate(tClass, id);
            evictCaches(tClass);

            existCache.putIfAbsent(tClass, new HashMap<>());
            existCache.get(tClass).put(id, false);
        }
    }

    @Override
    public void refresh(Object o) {
        putToCache(Collections.singleton(o));
    }

    @Override
    public void delete(Object object) {
        deleteById(object.getClass(), extractId(object));
    }

    @Override
    public void delete(DeleteStatement deleteStatement) {
        if (isCacheable(deleteStatement.getEntityClass())) {
            evictApi().evictAll(deleteStatement.getEntityClass());
        }
    }

    @Override
    public void update(UpdateStatement updateStatement) {
        if (isCacheable(updateStatement.getEntityClass())) {
            evictApi().evictAll(updateStatement.getEntityClass());
        }
    }

    @Override
    public void cacheCountOff(Class<?> aClass, long l) {
        if (isCacheable(aClass)) {
            countOffCache.put(aClass, l);
        }
    }

    @Override
    public Optional<Long> countOff(Class<?> aClass) {
        return Optional.ofNullable(countOffCache.get(aClass));
    }

    @Override
    public void cacheExist(Class<?> aClass, Object o, Boolean exist) {
        if (isCacheable(aClass)) {
            existCache.putIfAbsent(aClass, new HashMap<>());

            existCache.get(aClass).put(o, exist);
        }
    }

    @Override
    public Optional<Boolean> exist(Class<?> aClass, Object o) {
        Map<Object, Boolean> cache = existCache.get(aClass);

        return cache == null ? Optional.empty() : Optional.ofNullable(cache.get(o));
    }

    @Override
    public void cacheList(SelectStatement<?> selectStatement, List<Object> list) {
        if (isCacheable(selectStatement.getEntityClass())) {
            if (isLimitedQuery(selectStatement)) {
                selectStatementCache.putLimitedList(selectStatement, putToCache(list));
            } else {
                selectStatementCache.putList(selectStatement, putToCache(list));
            }
        }
    }

    @Override
    public Optional<List<Object>> list(SelectStatement<?> selectStatement) {
        if (isCacheable(selectStatement.getEntityClass())) {
            List<Object> ids;

            if (isLimitedQuery(selectStatement)) {
                ids = selectStatementCache.getLimitedList(selectStatement);
            } else {
                ids = selectStatementCache.getList(selectStatement);
            }

            if (ids == null) {
                return Optional.empty();
            }
            List<Object> objects = new ArrayList<>();

            for (Object id : ids) {
                Object object = objectCacheMap.get(selectStatement.getEntityClass()).get(
                        selectStatement.getEntityClass(),
                        id
                );

                if (object != null) {
                    objects.add(object);
                }
            }

            return Optional.of(objects);
        }

        return Optional.empty();
    }

    @Override
    public void cacheQueryForLong(SelectStatement<?> selectStatement, long l) {
        if (isCacheable(selectStatement.getEntityClass())) {
            selectStatementCache.putLong(selectStatement, l);
        }
    }

    @Override
    public Optional<Long> queryForLong(SelectStatement<?> selectStatement) {
        return Optional.ofNullable(selectStatementCache.getLong(selectStatement));
    }

    @Override
    public void cacheUniqueResult(SelectStatement<?> selectStatement, Object o) {
        if (isCacheable(selectStatement.getEntityClass())) {
            Object id = putToCache(Collections.singleton(o)).iterator().next();
            selectStatementCache.putUniqueResult(selectStatement, id);
        }
    }

    @Override
    public Optional<Object> uniqueResult(SelectStatement<?> selectStatement) {
        if (isCacheable(selectStatement.getEntityClass())) {
            Object id = selectStatementCache.getUniqueResult(selectStatement);
            ObjectCache objectCache = objectCacheMap.get(selectStatement.getEntityClass());

            return id == null ? Optional.empty() : Optional.ofNullable(objectCache.get(selectStatement.getEntityClass(), id));
        }

        return Optional.empty();
    }

    @Override
    public void enableDefaultCache() {
        ObjectCache objectCache = new ReferenceObjectCache();

        for (Class<?> entityType : cacheableEntities) {
            objectCache.registerClass(entityType);
            objectCacheMap.put(entityType, objectCache);
        }
    }

    @Override
    public void setCache(Class<?> entityType, ObjectCache objectCache) {
        if (!cacheableEntities.contains(entityType)) {
            throw new IllegalArgumentException("Entity " + entityType + " not annotated with " + Cacheable.class);
        }

        objectCache.registerClass(entityType);
        objectCacheMap.put(entityType, objectCache);
    }

    @Override
    public void setCache(Class<?>[] entityTypes, ObjectCache objectCache) {
        for (Class<?> entityType : entityTypes) {
            objectCache.registerClass(entityType);
            objectCacheMap.put(entityType, objectCache);
        }
    }

    public ObjectCache getCache(Class<?> entityType) {
        return objectCacheMap.get(entityType);
    }

    @Override
    public CacheEvict evictApi() {
        return cacheEvict;
    }

    @Override
    public void close() {
        objectCacheMap.values().forEach(ObjectCache::invalidateAll);
    }

    @Override
    public Optional<DatabaseResults> query(Query query) {
        return Optional.empty();
    }

    @Override
    public void cacheQuery(Query query, DatabaseResults results) {

    }

    private void evictCaches(Class<?> entityType) {
        evictApi().evictList(entityType);
        evictApi().evictLimitedList(entityType);
        evictApi().evictCountOff(entityType);
        evictApi().evictQueryForLong(entityType);
        evictApi().evictQueryForAll(entityType);
    }

    /**
     * Extract id from object.
     *
     * @param object target object
     * @return id
     */
    private Object extractId(Object object) {
        DatabaseColumnType primaryKeyColumnType = metaModel
                .getPersister(object.getClass())
                .getMetadata()
                .getPrimaryKeyColumnType();

        return primaryKeyColumnType.access(object);
    }

    /**
     * Return true if entity type is cacheable.
     *
     * @param entityType target entity type
     * @return true if entity type is cacheable
     */
    private boolean isCacheable(Class<?> entityType) {
        return cacheableEntities.contains(entityType);
    }

    private boolean isLimitedQuery(SelectStatement<?> selectStatement) {
        return selectStatement.getLimit() != null;
    }
}
