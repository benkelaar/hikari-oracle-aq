package com.bartenkelaar.hikari;

import com.zaxxer.hikari.HikariDataSource;
import oracle.jdbc.OracleOCIFailover;
import oracle.jdbc.OracleSavepoint;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQNotificationRegistration;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import oracle.jdbc.internal.KeywordValueLong;
import oracle.jdbc.internal.OracleConnection;
import oracle.jdbc.internal.OracleStatement;
import oracle.jdbc.internal.XSEventListener;
import oracle.jdbc.internal.XSNamespace;
import oracle.jdbc.oracore.OracleTypeADT;
import oracle.jdbc.oracore.OracleTypeCLOB;
import oracle.jdbc.pool.OracleConnectionCacheCallback;
import oracle.jdbc.pool.OraclePooledConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.BFILE;
import oracle.sql.BINARY_DOUBLE;
import oracle.sql.BINARY_FLOAT;
import oracle.sql.BLOB;
import oracle.sql.BfileDBAccess;
import oracle.sql.BlobDBAccess;
import oracle.sql.CLOB;
import oracle.sql.ClobDBAccess;
import oracle.sql.CustomDatum;
import oracle.sql.DATE;
import oracle.sql.Datum;
import oracle.sql.INTERVALDS;
import oracle.sql.INTERVALYM;
import oracle.sql.NUMBER;
import oracle.sql.StructDescriptor;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;
import oracle.sql.TIMEZONETAB;
import oracle.sql.TypeDescriptor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

import javax.transaction.xa.XAResource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Executor;

/**
 * {@link HikariDataSource} that wraps its {@link Connection}s
 * in an {@link OracleConnection} instance to allow oracle code
 * to cast created {@code Connection}s to {@link oracle.jdbc.OracleConnection} and
 * {@link oracle.jdbc.internal.OracleConnection}. The oracle connection is retrieved
 * via a {@link NativeJdbcExtractor}
 * <p />
 * Inspired by <a href="http://docs.spring.io/spring-data/data-jdbc/docs/current/reference/html/orcl.streamsaq.html">
 *     Spring Oracle AQ documentation</a> (chapter 5.3)
 */

public class HikariWrappingDataSource extends HikariDataSource {
    private final NativeJdbcExtractor extractor;

    public HikariWrappingDataSource(NativeJdbcExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new WrappedConnection(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new WrappedConnection(super.getConnection(username, password));
    }

    @SuppressWarnings("deprecation") // Ask Oracle.
    private class WrappedConnection implements OracleConnection {
        private final Connection connection;
        private final OracleConnection oracleConnection;

        public WrappedConnection(Connection connection) throws SQLException {
            this.connection = connection;
            this.oracleConnection = (OracleConnection) extractor.getNativeConnection(connection);
        }

        // Connection forwards
        @Override
        public Statement createStatement() throws SQLException {
            return connection.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return connection.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return connection.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return connection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            connection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return connection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            connection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            connection.rollback();
        }

        @Override
        public void close() throws SQLException {
            connection.close();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return connection.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return connection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            connection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return connection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            connection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return connection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            connection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return connection.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return connection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            connection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map getTypeMap() throws SQLException {
            return connection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map map) throws SQLException {
            connection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            connection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return connection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return connection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return connection.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            connection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            connection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return connection.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return connection.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return connection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return connection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return connection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return connection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return connection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            connection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            connection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return connection.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return connection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return connection.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return connection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            connection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return connection.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            connection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            connection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return connection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return connection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return connection.isWrapperFor(iface);
        }

        // Oracle Connection forwards
        @Override
        public short getStructAttrNCsId() throws SQLException {
            return oracleConnection.getStructAttrNCsId();
        }

        @Override
        public Properties getDBAccessProperties() throws SQLException {
            return oracleConnection.getDBAccessProperties();
        }

        @Override
        public Properties getOCIHandles() throws SQLException {
            return oracleConnection.getOCIHandles();
        }

        @Override
        public String getDatabaseProductVersion() throws SQLException {
            return oracleConnection.getDatabaseProductVersion();
        }

        @Override
        public String getURL() throws SQLException {
            return oracleConnection.getURL();
        }

        @Override
        public short getVersionNumber() throws SQLException {
            return oracleConnection.getVersionNumber();
        }

        @Override
        public Map getJavaObjectTypeMap() {
            return oracleConnection.getJavaObjectTypeMap();
        }

        @Override
        public void setJavaObjectTypeMap(Map map) {
            oracleConnection.setJavaObjectTypeMap(map);
        }

        @Override
        public byte getInstanceProperty(InstanceProperty instanceProperty) throws SQLException {
            return oracleConnection.getInstanceProperty(instanceProperty);
        }

        @Override
        public BfileDBAccess createBfileDBAccess() throws SQLException {
            return oracleConnection.createBfileDBAccess();
        }

        @Override
        public BlobDBAccess createBlobDBAccess() throws SQLException {
            return oracleConnection.createBlobDBAccess();
        }

        @Override
        public ClobDBAccess createClobDBAccess() throws SQLException {
            return oracleConnection.createClobDBAccess();
        }

        @Override
        public void setDefaultFixedString(boolean b) {
            oracleConnection.setDefaultFixedString(b);
        }

        @Override
        public boolean getDefaultFixedString() {
            return oracleConnection.getDefaultFixedString();
        }

        @Override
        public oracle.jdbc.OracleConnection getWrapper() {
            return oracleConnection.getWrapper();
        }

        @Override
        public Class classForNameAndSchema(String s, String s1) throws ClassNotFoundException {
            return oracleConnection.classForNameAndSchema(s, s1);
        }

        @Override
        public void setFDO(byte[] bytes) throws SQLException {
            oracleConnection.setFDO(bytes);
        }

        @Override
        public byte[] getFDO(boolean b) throws SQLException {
            return oracleConnection.getFDO(b);
        }

        @Override
        public boolean getBigEndian() throws SQLException {
            return oracleConnection.getBigEndian();
        }

        @Override
        public Object getDescriptor(byte[] bytes) {
            return oracleConnection.getDescriptor(bytes);
        }

        @Override
        public void putDescriptor(byte[] bytes, Object o) throws SQLException {
            oracleConnection.putDescriptor(bytes, o);
        }

        @Override
        public OracleConnection getPhysicalConnection() {
            return oracleConnection.getPhysicalConnection();
        }

        @Override
        public void removeDescriptor(String s) {
            oracleConnection.removeDescriptor(s);
        }

        @Override
        public void removeAllDescriptor() {
            oracleConnection.removeAllDescriptor();
        }

        @Override
        public int numberOfDescriptorCacheEntries() {
            return oracleConnection.numberOfDescriptorCacheEntries();
        }

        @Override
        public Enumeration descriptorCacheKeys() {
            return oracleConnection.descriptorCacheKeys();
        }

        @Override
        public long getTdoCState(String s, String s1) throws SQLException {
            return oracleConnection.getTdoCState(s, s1);
        }

        @Override
        public BufferCacheStatistics getByteBufferCacheStatistics() {
            return oracleConnection.getByteBufferCacheStatistics();
        }

        @Override
        public BufferCacheStatistics getCharBufferCacheStatistics() {
            return oracleConnection.getCharBufferCacheStatistics();
        }

        @Override
        public Datum toDatum(CustomDatum customDatum) throws SQLException {
            return oracleConnection.toDatum(customDatum);
        }

        @Override
        public short getDbCsId() throws SQLException {
            return oracleConnection.getDbCsId();
        }

        @Override
        public short getJdbcCsId() throws SQLException {
            return oracleConnection.getJdbcCsId();
        }

        @Override
        public short getNCharSet() {
            return oracleConnection.getNCharSet();
        }

        @Override
        public ResultSet newArrayDataResultSet(Datum[] datums, long l, int i, Map map) throws SQLException {
            return oracleConnection.newArrayDataResultSet(datums, l, i, map);
        }

        @Override
        public ResultSet newArrayDataResultSet(ARRAY array, long l, int i, Map map) throws SQLException {
            return oracleConnection.newArrayDataResultSet(array, l, i, map);
        }

        @Override
        public ResultSet newArrayLocatorResultSet(ArrayDescriptor arrayDescriptor, byte[] bytes, long l, int i, Map map) throws SQLException {
            return oracleConnection.newArrayLocatorResultSet(arrayDescriptor, bytes, l, i, map);
        }

        @Override
        public ResultSetMetaData newStructMetaData(StructDescriptor structDescriptor) throws SQLException {
            return oracleConnection.newStructMetaData(structDescriptor);
        }

        @Override
        public void getForm(OracleTypeADT oracleTypeADT, OracleTypeCLOB oracleTypeCLOB, int i) throws SQLException {
            oracleConnection.getForm(oracleTypeADT, oracleTypeCLOB, i);
        }

        @Override
        public int CHARBytesToJavaChars(byte[] bytes, int i, char[] chars) throws SQLException {
            return oracleConnection.CHARBytesToJavaChars(bytes, i, chars);
        }

        @Override
        public int NCHARBytesToJavaChars(byte[] bytes, int i, char[] chars) throws SQLException {
            return oracleConnection.NCHARBytesToJavaChars(bytes, i, chars);
        }

        @Override
        public boolean IsNCharFixedWith() {
            return oracleConnection.IsNCharFixedWith();
        }

        @Override
        public short getDriverCharSet() {
            return oracleConnection.getDriverCharSet();
        }

        @Override
        public int getC2SNlsRatio() {
            return oracleConnection.getC2SNlsRatio();
        }

        @Override
        public int getMaxCharSize() throws SQLException {
            return oracleConnection.getMaxCharSize();
        }

        @Override
        public int getMaxCharbyteSize() {
            return oracleConnection.getMaxCharbyteSize();
        }

        @Override
        public int getMaxNCharbyteSize() {
            return oracleConnection.getMaxNCharbyteSize();
        }

        @Override
        public boolean isCharSetMultibyte(short i) {
            return oracleConnection.isCharSetMultibyte(i);
        }

        @Override
        public int javaCharsToCHARBytes(char[] chars, int i, byte[] bytes) throws SQLException {
            return oracleConnection.javaCharsToCHARBytes(chars, i, bytes);
        }

        @Override
        public int javaCharsToNCHARBytes(char[] chars, int i, byte[] bytes) throws SQLException {
            return oracleConnection.javaCharsToNCHARBytes(chars, i, bytes);
        }

        @Override
        public void setStartTime(long l) throws SQLException {
            oracleConnection.setStartTime(l);
        }

        @Override
        public long getStartTime() throws SQLException {
            return oracleConnection.getStartTime();
        }

        @Override
        public boolean isStatementCacheInitialized() {
            return oracleConnection.isStatementCacheInitialized();
        }

        @Override
        public void getPropertyForPooledConnection(OraclePooledConnection oraclePooledConnection) throws SQLException {
            oracleConnection.getPropertyForPooledConnection(oraclePooledConnection);
        }

        @Override
        public String getProtocolType() {
            return oracleConnection.getProtocolType();
        }

        @Override
        public Connection getLogicalConnection(OraclePooledConnection oraclePooledConnection, boolean b) throws SQLException {
            return oracleConnection.getLogicalConnection(oraclePooledConnection, b);
        }

        @Override
        public void setTxnMode(int i) {
            oracleConnection.setTxnMode(i);
        }

        @Override
        public int getTxnMode() {
            return oracleConnection.getTxnMode();
        }

        @Override
        public int getHeapAllocSize() throws SQLException {
            return oracleConnection.getHeapAllocSize();
        }

        @Override
        public int getOCIEnvHeapAllocSize() throws SQLException {
            return oracleConnection.getOCIEnvHeapAllocSize();
        }

        @Override
        public void setAbandonedTimeoutEnabled(boolean b) throws SQLException {
            oracleConnection.setAbandonedTimeoutEnabled(b);
        }

        @Override
        public int getHeartbeatNoChangeCount() throws SQLException {
            return oracleConnection.getHeartbeatNoChangeCount();
        }

        @Override
        public void closeInternal(boolean b) throws SQLException {
            oracleConnection.closeInternal(b);
        }

        @Override
        public void cleanupAndClose(boolean b) throws SQLException {
            oracleConnection.cleanupAndClose(b);
        }

        @Override
        public OracleConnectionCacheCallback getConnectionCacheCallbackObj() throws SQLException {
            return oracleConnection.getConnectionCacheCallbackObj();
        }

        @Override
        public Object getConnectionCacheCallbackPrivObj() throws SQLException {
            return oracleConnection.getConnectionCacheCallbackPrivObj();
        }

        @Override
        public int getConnectionCacheCallbackFlag() throws SQLException {
            return oracleConnection.getConnectionCacheCallbackFlag();
        }

        @Override
        public Properties getServerSessionInfo() throws SQLException {
            return oracleConnection.getServerSessionInfo();
        }

        @Override
        public CLOB createClob(byte[] bytes) throws SQLException {
            return oracleConnection.createClob(bytes);
        }

        @Override
        public CLOB createClobWithUnpickledBytes(byte[] bytes) throws SQLException {
            return oracleConnection.createClobWithUnpickledBytes(bytes);
        }

        @Override
        public CLOB createClob(byte[] bytes, short i) throws SQLException {
            return oracleConnection.createClob(bytes, i);
        }

        @Override
        public BLOB createBlob(byte[] bytes) throws SQLException {
            return oracleConnection.createBlob(bytes);
        }

        @Override
        public BLOB createBlobWithUnpickledBytes(byte[] bytes) throws SQLException {
            return oracleConnection.createBlobWithUnpickledBytes(bytes);
        }

        @Override
        public BFILE createBfile(byte[] bytes) throws SQLException {
            return oracleConnection.createBfile(bytes);
        }

        @Override
        public boolean isDescriptorSharable(OracleConnection oracleConnection) throws SQLException {
            return oracleConnection.isDescriptorSharable(oracleConnection);
        }

        @Override
        public OracleStatement refCursorCursorToStatement(int i) throws SQLException {
            return oracleConnection.refCursorCursorToStatement(i);
        }

        @Override
        public XAResource getXAResource() throws SQLException {
            return oracleConnection.getXAResource();
        }

        @Override
        public boolean isV8Compatible() throws SQLException {
            return oracleConnection.isV8Compatible();
        }

        @Override
        public boolean getMapDateToTimestamp() {
            return oracleConnection.getMapDateToTimestamp();
        }

        @Override
        public boolean isGetObjectReturnsXMLType() {
            return oracleConnection.isGetObjectReturnsXMLType();
        }

        @Override
        public byte[] createLightweightSession(String s, KeywordValueLong[] keywordValueLongs, int i, KeywordValueLong[][] keywordValueLongs1, int[] ints) throws SQLException {
            return oracleConnection.createLightweightSession(s, keywordValueLongs, i, keywordValueLongs1, ints);
        }

        @Override
        public void executeLightweightSessionRoundtrip(int i, byte[] bytes, KeywordValueLong[] keywordValueLongs, int i1, KeywordValueLong[][] keywordValueLongs1, int[] ints) throws SQLException {
            oracleConnection.executeLightweightSessionRoundtrip(i, bytes, keywordValueLongs, i1, keywordValueLongs1, ints);
        }

        @Override
        public void executeLightweightSessionPiggyback(int i, byte[] bytes, KeywordValueLong[] keywordValueLongs, int i1) throws SQLException {
            oracleConnection.executeLightweightSessionPiggyback(i, bytes, keywordValueLongs, i1);
        }

        @Override
        public void doXSNamespaceOp(XSOperationCode xsOperationCode, byte[] bytes, XSNamespace[] xsNamespaces, XSNamespace[][] xsNamespaces1) throws SQLException {
            oracleConnection.doXSNamespaceOp(xsOperationCode, bytes, xsNamespaces, xsNamespaces1);
        }

        @Override
        public void doXSNamespaceOp(XSOperationCode xsOperationCode, byte[] bytes, XSNamespace[] xsNamespaces) throws SQLException {
            oracleConnection.doXSNamespaceOp(xsOperationCode, bytes, xsNamespaces);
        }

        @Override
        public String getDefaultSchemaNameForNamedTypes() throws SQLException {
            return oracleConnection.getDefaultSchemaNameForNamedTypes();
        }

        @Override
        public void setUsable(boolean b) {
            oracleConnection.setUsable(b);
        }

        @Override
        public Class getClassForType(String s, Map<String, Class> map) {
            return oracleConnection.getClassForType(s, map);
        }

        @Override
        public void addXSEventListener(XSEventListener xsEventListener) throws SQLException {
            oracleConnection.addXSEventListener(xsEventListener);
        }

        @Override
        public void addXSEventListener(XSEventListener xsEventListener, Executor executor) throws SQLException {
            oracleConnection.addXSEventListener(xsEventListener, executor);
        }

        @Override
        public void removeXSEventListener(XSEventListener xsEventListener) throws SQLException {
            oracleConnection.removeXSEventListener(xsEventListener);
        }

        @Override
        public int getTimezoneVersionNumber() throws SQLException {
            return oracleConnection.getTimezoneVersionNumber();
        }

        @Override
        public TIMEZONETAB getTIMEZONETAB() throws SQLException {
            return oracleConnection.getTIMEZONETAB();
        }

        @Override
        public String getDatabaseTimeZone() throws SQLException {
            return oracleConnection.getDatabaseTimeZone();
        }

        @Override
        public boolean getTimestamptzInGmt() {
            return oracleConnection.getTimestamptzInGmt();
        }

        @Override
        public boolean isDataInLocatorEnabled() throws SQLException {
            return oracleConnection.isDataInLocatorEnabled();
        }

        @Override
        public void commit(EnumSet<CommitOption> enumSet) throws SQLException {
            oracleConnection.commit(enumSet);
        }

        @Override
        public void archive(int i, int i1, String s) throws SQLException {
            oracleConnection.archive(i, i1, s);
        }

        @Override
        public void openProxySession(int i, Properties properties) throws SQLException {
            oracleConnection.openProxySession(i, properties);
        }

        @Override
        public boolean getAutoClose() throws SQLException {
            return oracleConnection.getAutoClose();
        }

        @Override
        public int getDefaultExecuteBatch() {
            return oracleConnection.getDefaultExecuteBatch();
        }

        @Override
        public int getDefaultRowPrefetch() {
            return oracleConnection.getDefaultRowPrefetch();
        }

        @Override
        public Object getDescriptor(String s) {
            return oracleConnection.getDescriptor(s);
        }

        @Override
        public String[] getEndToEndMetrics() throws SQLException {
            return oracleConnection.getEndToEndMetrics();
        }

        @Override
        public short getEndToEndECIDSequenceNumber() throws SQLException {
            return oracleConnection.getEndToEndECIDSequenceNumber();
        }

        @Override
        public boolean getIncludeSynonyms() {
            return oracleConnection.getIncludeSynonyms();
        }

        @Override
        public boolean getRestrictGetTables() {
            return oracleConnection.getRestrictGetTables();
        }

        @Override
        public Object getJavaObject(String s) throws SQLException {
            return oracleConnection.getJavaObject(s);
        }

        @Override
        public boolean getRemarksReporting() {
            return oracleConnection.getRemarksReporting();
        }

        @Override
        public String getSQLType(Object o) throws SQLException {
            return oracleConnection.getSQLType(o);
        }

        @Override
        public int getStmtCacheSize() {
            return oracleConnection.getStmtCacheSize();
        }

        @Override
        public short getStructAttrCsId() throws SQLException {
            return oracleConnection.getStructAttrCsId();
        }

        @Override
        public String getUserName() throws SQLException {
            return oracleConnection.getUserName();
        }

        @Override
        public String getCurrentSchema() throws SQLException {
            return oracleConnection.getCurrentSchema();
        }

        @Override
        public boolean getUsingXAFlag() {
            return oracleConnection.getUsingXAFlag();
        }

        @Override
        public boolean getXAErrorFlag() {
            return oracleConnection.getXAErrorFlag();
        }

        @Override
        public int pingDatabase() throws SQLException {
            return oracleConnection.pingDatabase();
        }

        @Override
        public int pingDatabase(int i) throws SQLException {
            return oracleConnection.pingDatabase(i);
        }

        @Override
        public void putDescriptor(String s, Object o) throws SQLException {
            oracleConnection.putDescriptor(s, o);
        }

        @Override
        public void registerSQLType(String s, Class aClass) throws SQLException {
            oracleConnection.registerSQLType(s, aClass);
        }

        @Override
        public void registerSQLType(String s, String s1) throws SQLException {
            oracleConnection.registerSQLType(s, s1);
        }

        @Override
        public void setAutoClose(boolean b) throws SQLException {
            oracleConnection.setAutoClose(b);
        }

        @Override
        public void setDefaultExecuteBatch(int i) throws SQLException {
            oracleConnection.setDefaultExecuteBatch(i);
        }

        @Override
        public void setDefaultRowPrefetch(int i) throws SQLException {
            oracleConnection.setDefaultRowPrefetch(i);
        }

        @Override
        public void setEndToEndMetrics(String[] strings, short i) throws SQLException {
            oracleConnection.setEndToEndMetrics(strings, i);
        }

        @Override
        public void setIncludeSynonyms(boolean b) {
            oracleConnection.setIncludeSynonyms(b);
        }

        @Override
        public void setRemarksReporting(boolean b) {
            oracleConnection.setRemarksReporting(b);
        }

        @Override
        public void setRestrictGetTables(boolean b) {
            oracleConnection.setRestrictGetTables(b);
        }

        @Override
        public void setStmtCacheSize(int i) throws SQLException {
            oracleConnection.setStmtCacheSize(i);
        }

        @Override
        public void setStmtCacheSize(int i, boolean b) throws SQLException {
            oracleConnection.setStmtCacheSize(i, b);
        }

        @Override
        public void setStatementCacheSize(int i) throws SQLException {
            oracleConnection.setStatementCacheSize(i);
        }

        @Override
        public int getStatementCacheSize() throws SQLException {
            return oracleConnection.getStatementCacheSize();
        }

        @Override
        public void setImplicitCachingEnabled(boolean b) throws SQLException {
            oracleConnection.setImplicitCachingEnabled(b);
        }

        @Override
        public boolean getImplicitCachingEnabled() throws SQLException {
            return oracleConnection.getImplicitCachingEnabled();
        }

        @Override
        public void setExplicitCachingEnabled(boolean b) throws SQLException {
            oracleConnection.setExplicitCachingEnabled(b);
        }

        @Override
        public boolean getExplicitCachingEnabled() throws SQLException {
            return oracleConnection.getExplicitCachingEnabled();
        }

        @Override
        public void purgeImplicitCache() throws SQLException {
            oracleConnection.purgeImplicitCache();
        }

        @Override
        public void purgeExplicitCache() throws SQLException {
            oracleConnection.purgeExplicitCache();
        }

        @Override
        public PreparedStatement getStatementWithKey(String s) throws SQLException {
            return oracleConnection.getStatementWithKey(s);
        }

        @Override
        public CallableStatement getCallWithKey(String s) throws SQLException {
            return oracleConnection.getCallWithKey(s);
        }

        @Override
        public void setUsingXAFlag(boolean b) {
            oracleConnection.setUsingXAFlag(b);
        }

        @Override
        public void setXAErrorFlag(boolean b) {
            oracleConnection.setXAErrorFlag(b);
        }

        @Override
        public void shutdown(DatabaseShutdownMode databaseShutdownMode) throws SQLException {
            oracleConnection.shutdown(databaseShutdownMode);
        }

        @Override
        public void startup(String s, int i) throws SQLException {
            oracleConnection.startup(s, i);
        }

        @Override
        public void startup(DatabaseStartupMode databaseStartupMode) throws SQLException {
            oracleConnection.startup(databaseStartupMode);
        }

        @Override
        public PreparedStatement prepareStatementWithKey(String s) throws SQLException {
            return oracleConnection.prepareStatementWithKey(s);
        }

        @Override
        public CallableStatement prepareCallWithKey(String s) throws SQLException {
            return oracleConnection.prepareCallWithKey(s);
        }

        @Override
        public void setCreateStatementAsRefCursor(boolean b) {
            oracleConnection.setCreateStatementAsRefCursor(b);
        }

        @Override
        public boolean getCreateStatementAsRefCursor() {
            return oracleConnection.getCreateStatementAsRefCursor();
        }

        @Override
        public void setSessionTimeZone(String s) throws SQLException {
            oracleConnection.setSessionTimeZone(s);
        }

        @Override
        public String getSessionTimeZone() {
            return oracleConnection.getSessionTimeZone();
        }

        @Override
        public String getSessionTimeZoneOffset() throws SQLException {
            return oracleConnection.getSessionTimeZoneOffset();
        }

        @Override
        public Properties getProperties() {
            return oracleConnection.getProperties();
        }

        @Override
        public Connection _getPC() {
            return oracleConnection._getPC();
        }

        @Override
        public boolean isLogicalConnection() {
            return oracleConnection.isLogicalConnection();
        }

        @Override
        public void registerTAFCallback(OracleOCIFailover oracleOCIFailover, Object o) throws SQLException {
            oracleConnection.registerTAFCallback(oracleOCIFailover, o);
        }

        @Override
        public oracle.jdbc.OracleConnection unwrap() {
            return oracleConnection.unwrap();
        }

        @Override
        public void setWrapper(oracle.jdbc.OracleConnection oracleConnection) {
            oracleConnection.setWrapper(oracleConnection);
        }

        @Override
        public OracleConnection physicalConnectionWithin() {
            return oracleConnection.physicalConnectionWithin();
        }

        @Override
        public OracleSavepoint oracleSetSavepoint() throws SQLException {
            return oracleSetSavepoint();
        }

        @Override
        public OracleSavepoint oracleSetSavepoint(String s) throws SQLException {
            return oracleSetSavepoint(s);
        }

        @Override
        public void oracleRollback(OracleSavepoint oracleSavepoint) throws SQLException {
            oracleConnection.oracleRollback(oracleSavepoint);
        }

        @Override
        public void oracleReleaseSavepoint(OracleSavepoint oracleSavepoint) throws SQLException {
            oracleConnection.oracleReleaseSavepoint(oracleSavepoint);
        }

        @Override
        public void close(Properties properties) throws SQLException {
            oracleConnection.close(properties);
        }

        @Override
        public void close(int i) throws SQLException {
            oracleConnection.close(i);
        }

        @Override
        public boolean isProxySession() {
            return oracleConnection.isProxySession();
        }

        @Override
        public void applyConnectionAttributes(Properties properties) throws SQLException {
            oracleConnection.applyConnectionAttributes(properties);
        }

        @Override
        public Properties getConnectionAttributes() throws SQLException {
            return oracleConnection.getConnectionAttributes();
        }

        @Override
        public Properties getUnMatchedConnectionAttributes() throws SQLException {
            return oracleConnection.getUnMatchedConnectionAttributes();
        }

        @Override
        public void registerConnectionCacheCallback(OracleConnectionCacheCallback oracleConnectionCacheCallback, Object o, int i) throws SQLException {
            oracleConnection.registerConnectionCacheCallback(oracleConnectionCacheCallback, o, i);
        }

        @Override
        public void setConnectionReleasePriority(int i) throws SQLException {
            oracleConnection.setConnectionReleasePriority(i);
        }

        @Override
        public int getConnectionReleasePriority() throws SQLException {
            return oracleConnection.getConnectionReleasePriority();
        }

        @Override
        public void setPlsqlWarnings(String s) throws SQLException {
            oracleConnection.setPlsqlWarnings(s);
        }

        @Override
        public AQNotificationRegistration[] registerAQNotification(String[] strings, Properties[] properties, Properties properties1) throws SQLException {
            return oracleConnection.registerAQNotification(strings, properties, properties1);
        }

        @Override
        public void unregisterAQNotification(AQNotificationRegistration aqNotificationRegistration) throws SQLException {
            oracleConnection.unregisterAQNotification(aqNotificationRegistration);
        }

        @Override
        public AQMessage dequeue(String s, AQDequeueOptions aqDequeueOptions, byte[] bytes) throws SQLException {
            return oracleConnection.dequeue(s, aqDequeueOptions, bytes);
        }

        @Override
        public AQMessage dequeue(String s, AQDequeueOptions aqDequeueOptions, String s1) throws SQLException {
            return oracleConnection.dequeue(s, aqDequeueOptions, s1);
        }

        @Override
        public void enqueue(String s, AQEnqueueOptions aqEnqueueOptions, AQMessage aqMessage) throws SQLException {
            oracleConnection.enqueue(s, aqEnqueueOptions, aqMessage);
        }

        @Override
        public DatabaseChangeRegistration registerDatabaseChangeNotification(Properties properties) throws SQLException {
            return oracleConnection.registerDatabaseChangeNotification(properties);
        }

        @Override
        public DatabaseChangeRegistration getDatabaseChangeRegistration(int i) throws SQLException {
            return oracleConnection.getDatabaseChangeRegistration(i);
        }

        @Override
        public void unregisterDatabaseChangeNotification(DatabaseChangeRegistration databaseChangeRegistration) throws SQLException {
            oracleConnection.unregisterDatabaseChangeNotification(databaseChangeRegistration);
        }

        @Override
        public void unregisterDatabaseChangeNotification(int i, String s, int i1) throws SQLException {
            oracleConnection.unregisterDatabaseChangeNotification(i, s, i1);
        }

        @Override
        public void unregisterDatabaseChangeNotification(int i) throws SQLException {
            oracleConnection.unregisterDatabaseChangeNotification(i);
        }

        @Override
        public void unregisterDatabaseChangeNotification(long l, String s) throws SQLException {
            oracleConnection.unregisterDatabaseChangeNotification(l, s);
        }

        @Override
        public ARRAY createARRAY(String s, Object o) throws SQLException {
            return oracleConnection.createARRAY(s, o);
        }

        @Override
        public BINARY_DOUBLE createBINARY_DOUBLE(double v) throws SQLException {
            return oracleConnection.createBINARY_DOUBLE(v);
        }

        @Override
        public BINARY_FLOAT createBINARY_FLOAT(float v) throws SQLException {
            return oracleConnection.createBINARY_FLOAT(v);
        }

        @Override
        public DATE createDATE(Date date) throws SQLException {
            return oracleConnection.createDATE(date);
        }

        @Override
        public DATE createDATE(Time time) throws SQLException {
            return oracleConnection.createDATE(time);
        }

        @Override
        public DATE createDATE(Timestamp timestamp) throws SQLException {
            return oracleConnection.createDATE(timestamp);
        }

        @Override
        public DATE createDATE(Date date, Calendar calendar) throws SQLException {
            return oracleConnection.createDATE(date, calendar);
        }

        @Override
        public DATE createDATE(Time time, Calendar calendar) throws SQLException {
            return oracleConnection.createDATE(time, calendar);
        }

        @Override
        public DATE createDATE(Timestamp timestamp, Calendar calendar) throws SQLException {
            return oracleConnection.createDATE(timestamp, calendar);
        }

        @Override
        public DATE createDATE(String s) throws SQLException {
            return oracleConnection.createDATE(s);
        }

        @Override
        public INTERVALDS createINTERVALDS(String s) throws SQLException {
            return oracleConnection.createINTERVALDS(s);
        }

        @Override
        public INTERVALYM createINTERVALYM(String s) throws SQLException {
            return oracleConnection.createINTERVALYM(s);
        }

        @Override
        public NUMBER createNUMBER(boolean b) throws SQLException {
            return oracleConnection.createNUMBER(b);
        }

        @Override
        public NUMBER createNUMBER(byte b) throws SQLException {
            return oracleConnection.createNUMBER(b);
        }

        @Override
        public NUMBER createNUMBER(short i) throws SQLException {
            return oracleConnection.createNUMBER(i);
        }

        @Override
        public NUMBER createNUMBER(int i) throws SQLException {
            return oracleConnection.createNUMBER(i);
        }

        @Override
        public NUMBER createNUMBER(long l) throws SQLException {
            return oracleConnection.createNUMBER(l);
        }

        @Override
        public NUMBER createNUMBER(float v) throws SQLException {
            return oracleConnection.createNUMBER(v);
        }

        @Override
        public NUMBER createNUMBER(double v) throws SQLException {
            return oracleConnection.createNUMBER(v);
        }

        @Override
        public NUMBER createNUMBER(BigDecimal bigDecimal) throws SQLException {
            return oracleConnection.createNUMBER(bigDecimal);
        }

        @Override
        public NUMBER createNUMBER(BigInteger bigInteger) throws SQLException {
            return oracleConnection.createNUMBER(bigInteger);
        }

        @Override
        public NUMBER createNUMBER(String s, int i) throws SQLException {
            return oracleConnection.createNUMBER(s, i);
        }

        @Override
        public TIMESTAMP createTIMESTAMP(Date date) throws SQLException {
            return oracleConnection.createTIMESTAMP(date);
        }

        @Override
        public TIMESTAMP createTIMESTAMP(DATE date) throws SQLException {
            return oracleConnection.createTIMESTAMP(date);
        }

        @Override
        public TIMESTAMP createTIMESTAMP(Time time) throws SQLException {
            return oracleConnection.createTIMESTAMP(time);
        }

        @Override
        public TIMESTAMP createTIMESTAMP(Timestamp timestamp) throws SQLException {
            return oracleConnection.createTIMESTAMP(timestamp);
        }

        @Override
        public TIMESTAMP createTIMESTAMP(String s) throws SQLException {
            return oracleConnection.createTIMESTAMP(s);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(Date date) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(date);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(Date date, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(date, calendar);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(Time time) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(time);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(Time time, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(time, calendar);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(Timestamp timestamp) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(timestamp);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(Timestamp timestamp, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(timestamp, calendar);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(String s) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(s);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(String s, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(s, calendar);
        }

        @Override
        public TIMESTAMPTZ createTIMESTAMPTZ(DATE date) throws SQLException {
            return oracleConnection.createTIMESTAMPTZ(date);
        }

        @Override
        public TIMESTAMPLTZ createTIMESTAMPLTZ(Date date, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPLTZ(date, calendar);
        }

        @Override
        public TIMESTAMPLTZ createTIMESTAMPLTZ(Time time, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPLTZ(time, calendar);
        }

        @Override
        public TIMESTAMPLTZ createTIMESTAMPLTZ(Timestamp timestamp, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPLTZ(timestamp, calendar);
        }

        @Override
        public TIMESTAMPLTZ createTIMESTAMPLTZ(String s, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPLTZ(s, calendar);
        }

        @Override
        public TIMESTAMPLTZ createTIMESTAMPLTZ(DATE date, Calendar calendar) throws SQLException {
            return oracleConnection.createTIMESTAMPLTZ(date, calendar);
        }

        @Override
        public void cancel() throws SQLException {
            oracleConnection.cancel();
        }

        @Override
        public void abort() throws SQLException {
            oracleConnection.abort();
        }

        @Override
        public TypeDescriptor[] getAllTypeDescriptorsInCurrentSchema() throws SQLException {
            return oracleConnection.getAllTypeDescriptorsInCurrentSchema();
        }

        @Override
        public TypeDescriptor[] getTypeDescriptorsFromListInCurrentSchema(String[] strings) throws SQLException {
            return oracleConnection.getTypeDescriptorsFromListInCurrentSchema(strings);
        }

        @Override
        public TypeDescriptor[] getTypeDescriptorsFromList(String[][] strings) throws SQLException {
            return oracleConnection.getTypeDescriptorsFromList(strings);
        }

        @Override
        public String getDataIntegrityAlgorithmName() throws SQLException {
            return oracleConnection.getDataIntegrityAlgorithmName();
        }

        @Override
        public String getEncryptionAlgorithmName() throws SQLException {
            return oracleConnection.getEncryptionAlgorithmName();
        }

        @Override
        public String getAuthenticationAdaptorName() throws SQLException {
            return oracleConnection.getAuthenticationAdaptorName();
        }

        @Override
        public boolean isUsable() {
            return oracleConnection.isUsable();
        }

        @Override
        public void setDefaultTimeZone(TimeZone timeZone) throws SQLException {
            oracleConnection.setDefaultTimeZone(timeZone);
        }

        @Override
        public TimeZone getDefaultTimeZone() throws SQLException {
            return oracleConnection.getDefaultTimeZone();
        }

        @Override
        public void setApplicationContext(String s, String s1, String s2) throws SQLException {
            oracleConnection.setApplicationContext(s, s1, s2);
        }

        @Override
        public void clearAllApplicationContext(String s) throws SQLException {
            oracleConnection.clearAllApplicationContext(s);
        }

        @Override
        public String toString() {
            return "Oracle wrapper around " + connection.toString();
        }
    }
}
