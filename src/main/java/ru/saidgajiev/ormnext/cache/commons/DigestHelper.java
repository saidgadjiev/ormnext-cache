package ru.saidgajiev.ormnext.cache.commons;

import ru.saidgadjiev.ormnext.core.dao.DatabaseEngine;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.CriterionArgument;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.query.space.EntityQuerySpace;
import ru.saidgadjiev.ormnext.core.query.visitor.element.SelectQuery;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Digest helper.
 *
 * @author Said Gadjiev
 */
public final class DigestHelper {

    /**
     * Hex suffix.
     */
    private static final int SUFFIX = 255;

    /**
     * Meta model.
     */
    private MetaModel metaModel;

    /**
     * Database engine.
     */
    private DatabaseEngine<?> databaseEngine;

    /**
     * Create a new instance.
     *
     * @param metaModel      target meta model
     * @param databaseEngine target database engine
     */
    public DigestHelper(MetaModel metaModel, DatabaseEngine<?> databaseEngine) {
        this.metaModel = metaModel;
        this.databaseEngine = databaseEngine;
    }

    /**
     * Digest {@link SelectStatement}.
     *
     * @param selectStatement target statement.
     * @return digest string
     */
    public String digest(SelectStatement<?> selectStatement) {
        EntityQuerySpace entityQuerySpace = metaModel.getPersister(
                selectStatement.getEntityClass()
        ).getEntityQuerySpace();

        SelectQuery selectQuery = entityQuerySpace.getSelectQuery(selectStatement);
        StringBuilder digestQuery = new StringBuilder(databaseEngine.prepareQuery(selectQuery));
        AtomicInteger index = new AtomicInteger();
        Map<Integer, Object> resultArgs = new HashMap<>();

        for (CriterionArgument argument : selectStatement.getArgs()) {
            argument.getValues().forEach(arg -> resultArgs.put(index.incrementAndGet(), arg));
        }
        selectStatement.getUserProvidedArgs().forEach(resultArgs::put);
        digestQuery.append(resultArgs);

        return digest(digestQuery.toString());
    }

    /**
     * Digest string.
     *
     * @param str target string
     * @return digest string
     */
    private String digest(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            digest.update(str.getBytes(Charset.forName("UTF-8")));

            return toHash(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Convert bytes to string.
     *
     * @param bytes target bytes
     * @return converted string
     */
    private String toHash(byte[] bytes) {
        StringBuilder out = new StringBuilder();

        for (byte b : bytes) {
            out.append(Integer.toHexString(b & SUFFIX));
        }

        return out.toString();
    }
}
