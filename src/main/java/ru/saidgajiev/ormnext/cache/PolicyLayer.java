package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.Cache;
import ru.saidgadjiev.ormnext.core.cache.CacheEvict;
import ru.saidgadjiev.ormnext.core.cache.ObjectCache;
import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.DeleteStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.UpdateStatement;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;
import ru.saidgajiev.ormnext.cache.policy.CachePut;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PolicyLayer implements Cache {

    private Map<Class<?>, Set<CachePut>> policiesMap = new HashMap<>();

    private final Cache cache;

    public PolicyLayer(Cache cache) {
        this.cache = cache;
    }

    @Override
    public void init(MetaModel metaModel, DatabaseEngine<?> databaseEngine) {
        for (Class<?> entityType : metaModel.getPersistentClasses()) {
            if (entityType.isAnnotationPresent(Cacheable.class)) {
                Cacheable cacheable = entityType.getAnnotation(Cacheable.class);

                policiesMap.put(entityType, Stream.of(cacheable.policies()).collect(Collectors.toSet()));
            }
        }
        cache.init(metaModel, databaseEngine);
    }

    @Override
    public void create(Object object) {
        cache.create(object);
    }

    @Override
    public void create(Collection<Object> objects) {
        cache.create(objects);
    }

    @Override
    public void putToCache(Object id, Object data) {
        cache.putToCache(id, data);
    }

    @Override
    public void cacheQueryForId(Object id, Object data) {
        cache.cacheQueryForId(id, data);
    }

    @Override
    public Optional<Object> queryForId(Class<?> tClass, Object id) {
        return cache.queryForId(tClass, id);
    }

    @Override
    public void cacheQueryForAll(Collection<Object> objects) {
        cache.cacheQueryForAll(objects);
    }

    @Override
    public Optional<List<Object>> queryForAll(Class<?> tClass) {
        return cache.queryForAll(tClass);
    }

    @Override
    public void update(Object object) {
        cache.update(object);
    }

    @Override
    public void deleteById(Class<?> tClass, Object id) {
        cache.deleteById(tClass, id);
    }

    @Override
    public void refresh(Object object) {
        cache.refresh(object);
    }

    @Override
    public void delete(Object object) {
        cache.delete(object);
    }

    @Override
    public void delete(DeleteStatement deleteStatement) {
        cache.delete(deleteStatement);
    }

    @Override
    public void update(UpdateStatement updateStatement) {
        cache.update(updateStatement);
    }

    @Override
    public void cacheCountOff(Class<?> entityType, long countOff) {
        cache.cacheCountOff(entityType, countOff);
    }

    @Override
    public Optional<Long> countOff(Class<?> entityType) {
        return cache.countOff(entityType);
    }

    @Override
    public void cacheExist(Class<?> entityType, Object id, Boolean exist) {
        cache.cacheExist(entityType, id, exist);
    }

    @Override
    public Optional<Boolean> exist(Class<?> entityType, Object id) {
        return cache.exist(entityType, id);
    }

    @Override
    public void cacheList(SelectStatement<?> selectStatement, List<Object> objects) {
        cache.cacheList(selectStatement, objects);
    }

    @Override
    public Optional<List<Object>> list(SelectStatement<?> selectStatement) {
        return cache.list(selectStatement);
    }

    @Override
    public void cacheQueryForLong(SelectStatement<?> selectStatement, long result) {
        cache.cacheQueryForLong(selectStatement, result);
    }

    @Override
    public Optional<Long> queryForLong(SelectStatement<?> selectStatement) {
        return cache.queryForLong(selectStatement);
    }

    @Override
    public void cacheUniqueResult(SelectStatement<?> selectStatement, Object result) {
        cache.cacheUniqueResult(selectStatement, result);
    }

    @Override
    public Optional<Object> uniqueResult(SelectStatement<?> selectStatement) {
        return cache.uniqueResult(selectStatement);
    }

    @Override
    public void enableDefaultCache() {
        cache.enableDefaultCache();
    }

    @Override
    public void setCache(Class<?> entityType, ObjectCache objectCache) {
        cache.setCache(entityType, objectCache);
    }

    @Override
    public void setCache(Class<?>[] entityTypes, ObjectCache objectCache) {
        cache.setCache(entityTypes, objectCache);
    }

    @Override
    public CacheEvict evictApi() {
        return cache.evictApi();
    }

    @Override
    public void close() {
        cache.close();
    }
}
