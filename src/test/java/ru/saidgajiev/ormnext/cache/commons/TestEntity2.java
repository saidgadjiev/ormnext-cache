package ru.saidgajiev.ormnext.cache.commons;

import ru.saidgadjiev.ormnext.core.field.DatabaseColumn;

public class TestEntity2 {

    @DatabaseColumn(id = true)
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
