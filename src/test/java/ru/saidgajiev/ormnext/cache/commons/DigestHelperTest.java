package ru.saidgajiev.ormnext.cache.commons;

import org.junit.Assert;
import org.junit.Test;
import ru.saidgadjiev.ormnext.core.dao.DefaultDatabaseEngine;
import ru.saidgadjiev.ormnext.core.dialect.H2Dialect;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.Criteria;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.Restrictions;
import ru.saidgadjiev.ormnext.core.query.criteria.impl.SelectStatement;
import ru.saidgadjiev.ormnext.core.table.internal.metamodel.MetaModel;

import java.util.Arrays;
import java.util.Collections;

public class DigestHelperTest {

    @Test
    public void digest() throws Exception {
        MetaModel metaModel = new MetaModel(Arrays.asList(TestEntity1.class, TestEntity2.class));

        metaModel.init();
        DigestHelper helper = new DigestHelper(metaModel, new DefaultDatabaseEngine(new H2Dialect()));

        Assert.assertEquals(
                helper.digest(new SelectStatement<>(TestEntity1.class).limit(10).offset(10)),
                helper.digest(new SelectStatement<>(TestEntity1.class).limit(10).offset(10))
        );
        Assert.assertNotEquals(
                helper.digest(new SelectStatement<>(TestEntity1.class)),
                helper.digest(new SelectStatement<>(TestEntity2.class))
        );
    }

    @Test
    public void digestByEqualCriteria() throws Exception {
        MetaModel metaModel = new MetaModel(Arrays.asList(TestEntity1.class, TestEntity2.class));

        metaModel.init();
        DigestHelper helper = new DigestHelper(metaModel, new DefaultDatabaseEngine(new H2Dialect()));

        SelectStatement<TestEntity1> selectStatement1 = new SelectStatement<>(TestEntity1.class);

        selectStatement1.where(
                new Criteria().add(Restrictions.eq("id", 1))
        );

        SelectStatement<TestEntity1> selectStatement2 = new SelectStatement<>(TestEntity1.class);

        selectStatement2.where(
                new Criteria().add(Restrictions.eq("id", 1))
        );
        Assert.assertEquals(helper.digest(selectStatement1), helper.digest(selectStatement2));

        selectStatement1.setObject(1, 3);

        Assert.assertNotEquals(helper.digest(selectStatement1), helper.digest(selectStatement2));
    }

    @Test
    public void digestByEqualUserProvidedArgs() throws Exception {
        MetaModel metaModel = new MetaModel(Arrays.asList(TestEntity1.class, TestEntity2.class));

        metaModel.init();
        DigestHelper helper = new DigestHelper(metaModel, new DefaultDatabaseEngine(new H2Dialect()));

        SelectStatement<TestEntity1> selectStatement1 = new SelectStatement<>(TestEntity1.class);

        selectStatement1.where(
                new Criteria().add(Restrictions.eq("id", 3))
        );

        SelectStatement<TestEntity1> selectStatement2 = new SelectStatement<>(TestEntity1.class);

        selectStatement2.where(
                new Criteria().add(Restrictions.eq("id", 1))
        );
        selectStatement2.setObject(1, 3);

        Assert.assertEquals(helper.digest(selectStatement1), helper.digest(selectStatement2));
    }
}