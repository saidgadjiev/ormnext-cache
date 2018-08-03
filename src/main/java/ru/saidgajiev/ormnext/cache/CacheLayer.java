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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache layer.
 *
 * @author Said Gadjiev
 */
public class CacheLayer implements Cache {

    /**
     * Meta model.
     */
    private MetaModel metaModel;

    /**
     * Object cache map.
     */
    private Map<Class<?>, ObjectCache> objectCacheMap = new ConcurrentHashMap<>();

    /**
     * Query for all results cache.
     */
    private Map<Class<?>, List<Object>> queryForAllCache = new ConcurrentHashMap<>();

    /**
     * Count off results cache.
     */
    private Map<Class<?>, Long> countOffCache = new ConcurrentHashMap<>();

    /**
     * Exist results cache.
     */
    private final Map<Class<?>, Map<Object, Boolean>> existCache = new ConcurrentHashMap<>();

    /**
     * Select statement cache.
     */
    private SelectStatementCache selectStatementCache;

    /**
     * Evict api.
     */
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
        create(Collections.singletonList(object));
    }

    @Override
    public void create(Collection<Object> objects) {
        if (objects.isEmpty()) {
            return;
        }
        Class<?> entityType = objects.iterator().next().getClass();

        if (isCacheable(entityType)) {
            //Очищаем кэши

            //Удаляем кэш для list
            evictApi().evictList(entityType);

            //Удаляем кэш для запросов с limit offset
            evictApi().evictLimitedList(entityType);

            //Кэш countOff по таблице
            evictApi().evictCountOff(entityType);

            //Кэш по long начению по запросу
            evictApi().evictQueryForLong(entityType);

            //Кэш с SELECT *
            evictApi().evictQueryForAll(entityType);

            for (Object object : objects) {
                Object id = extractId(object);

                //Добавляем в exist
                addToExistCache(entityType, id, true);
                objectCacheMap.get(entityType).put(entityType, id, object);
            }
        }
    }

    /**
     * Put objects to cache and return object ids.
     *
     * @param objects target objects.
     * @return object ids
     */
    private List<Object> putToCache(Collection<Object> objects) {
        if (objects.isEmpty()) {
            return Collections.emptyList();
        }
        Class<?> entityType = objects.iterator().next().getClass();
        List<Object> ids = new ArrayList<>();

        for (Object object : objects) {
            Object id = extractId(object);

            ids.add(id);
            addToExistCache(entityType, id, true);
            objectCacheMap.get(entityType).put(entityType, id, object);
        }

        return ids;
    }

    @Override
    public void putToCache(Object id, Object data) {
        if (isCacheable(data.getClass())) {
            addToExistCache(data.getClass(), id, true);
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

        if (isCacheable(entityType)) {
            queryForAllCache.putIfAbsent(entityType, putToCache(collection));
        }
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
            Class<?> entityType = o.getClass();

            //Очищаем все кеши в selectstatement
            evictApi().evictList(entityType);
            evictApi().evictLimitedList(entityType);
            evictApi().evictQueryForLong(entityType);

            objectCacheMap.get(o.getClass()).put(entityType, extractId(o), o);
        }
    }

    @Override
    public void deleteById(Class<?> entityType, Object id) {
        if (isCacheable(entityType)) {
            //Удаляем запись из кеша объектов
            evictApi().evict(entityType, id);

            //Очищаем кеш long результатов
            evictApi().evictQueryForLong(entityType);

            //Кэш count off
            evictApi().evictCountOff(entityType);

            //Кэш SELECT *
            evictApi().evictQueryForAll(entityType);

            addToExistCache(entityType, id, false);
        }
    }

    @Override
    public void refresh(Object o) {
        update(o);
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
            addToExistCache(aClass, extractId(o), exist);
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

    @Override
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

    /**
     * Add object to exist cache.
     *
     * @param entityType target entity type
     * @param id         target entity id
     * @param exist      target exist
     */
    private void addToExistCache(Class<?> entityType, Object id, boolean exist) {
        existCache.putIfAbsent(entityType, new ConcurrentHashMap<>());
        existCache.get(entityType).put(id, exist);
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

    /**
     * Return true if it is limited query.
     *
     * @param selectStatement target select statement
     * @return true if it is limited query
     */
    private boolean isLimitedQuery(SelectStatement<?> selectStatement) {
        return selectStatement.getLimit() != null;
    }
}
