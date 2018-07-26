package ru.saidgajiev.ormnext.cache;

import ru.saidgadjiev.ormnext.core.connection.DatabaseResults;
import ru.saidgadjiev.ormnext.core.connection.DatabaseResultsMetadata;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class CacheResults implements DatabaseResults {

    private ResultSetMap resultSetMap;

    @Override
    public boolean next() throws SQLException {
        return false;
    }

    @Override
    public String getString(int columnId) throws SQLException {
        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public int getInt(int columnId) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public boolean getBoolean(int columnId) throws SQLException {
        return false;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return false;
    }

    @Override
    public double getDouble(int columnId) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(int columnId) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public Long getLong(int columnId) throws SQLException {
        return null;
    }

    @Override
    public Long getLong(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public byte getByte(int columnId) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(int columnId) throws SQLException {
        return 0;
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(int columnId) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnId) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnId) throws SQLException {
        return null;
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public <T> T getResultsObject() {
        return null;
    }

    @Override
    public DatabaseResultsMetadata getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void close() throws SQLException {

    }
}
