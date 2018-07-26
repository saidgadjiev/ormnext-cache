package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.cache.Cache;
import ru.saidgadjiev.ormnext.core.cache.CacheEvict;
import ru.saidgadjiev.ormnext.core.cache.ObjectCache;
import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.DeleteStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.UpdateStatement;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Ormnext cache implementation. It use {@link ReferenceObjectCache} by default.
 *
 * @author Said Gadjiev
 */
public class CacheImpl implements Cache {

    private final Cache proxy;

    public CacheImpl() {
        proxy = new PolicyLayer(new CacheLayer());
    }

    @Override
    public void init(MetaModel metaModel, DatabaseEngine<?> databaseEngine) {
        proxy.init(metaModel, databaseEngine);
    }

    @Override
    public void create(Object object) {
        proxy.create(object);
    }

    @Override
    public void create(Collection<Object> objects) {
        proxy.create(objects);
    }

    @Override
    public void putToCache(Object id, Object data) {
        proxy.putToCache(id, data);
    }

    @Override
    public void cacheQueryForId(Object id, Object data) {
        proxy.cacheQueryForId(id, data);
    }

    @Override
    public Optional<Object> queryForId(Class<?> tClass, Object id) {
        return proxy.queryForId(tClass, id);
    }

    @Override
    public void cacheQueryForAll(Collection<Object> objects) {
        proxy.cacheQueryForAll(objects);
    }

    @Override
    public Optional<List<Object>> queryForAll(Class<?> tClass) {
        return proxy.queryForAll(tClass);
    }

    @Override
    public void update(Object object) {
        proxy.update(object);
    }

    @Override
    public void deleteById(Class<?> tClass, Object id) {
        proxy.deleteById(tClass, id);
    }

    @Override
    public void refresh(Object object) {
        proxy.refresh(object);
    }

    @Override
    public void delete(Object object) {
        proxy.delete(object);
    }

    @Override
    public void delete(DeleteStatement deleteStatement) {
        proxy.delete(deleteStatement);
    }

    @Override
    public void update(UpdateStatement updateStatement) {
        proxy.update(updateStatement);
    }

    @Override
    public void cacheCountOff(Class<?> entityType, long countOff) {
        proxy.cacheCountOff(entityType, countOff);
    }

    @Override
    public Optional<Long> countOff(Class<?> entityType) {
        return proxy.countOff(entityType);
    }

    @Override
    public void cacheExist(Class<?> entityType, Object id, Boolean exist) {
        proxy.cacheExist(entityType, id, exist);
    }

    @Override
    public Optional<Boolean> exist(Class<?> entityType, Object id) {
        return proxy.exist(entityType, id);
    }

    @Override
    public void cacheList(SelectStatement<?> selectStatement, List<Object> objects) {
        proxy.cacheList(selectStatement, objects);
    }

    @Override
    public Optional<List<Object>> list(SelectStatement<?> selectStatement) {
        return proxy.list(selectStatement);
    }

    @Override
    public void cacheQueryForLong(SelectStatement<?> selectStatement, long result) {
        proxy.cacheQueryForLong(selectStatement, result);
    }

    @Override
    public Optional<Long> queryForLong(SelectStatement<?> selectStatement) {
        return proxy.queryForLong(selectStatement);
    }

    @Override
    public void cacheUniqueResult(SelectStatement<?> selectStatement, Object result) {
        proxy.cacheUniqueResult(selectStatement, result);
    }

    @Override
    public Optional<Object> uniqueResult(SelectStatement<?> selectStatement) {
        return proxy.uniqueResult(selectStatement);
    }

    @Override
    public void enableDefaultCache() {
        proxy.enableDefaultCache();
    }

    @Override
    public void setCache(Class<?> entityType, ObjectCache objectCache) {
        proxy.setCache(entityType, objectCache);
    }

    @Override
    public void setCache(Class<?>[] entityTypes, ObjectCache objectCache) {
        proxy.setCache(entityTypes, objectCache);
    }

    @Override
    public CacheEvict evictApi() {
        return proxy.evictApi();
    }

    @Override
    public void close() {
        proxy.close();
    }
}
