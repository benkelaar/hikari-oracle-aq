# hikari-oracle-aq
Example code of hooking up Oracle AQ over Spring with [HikariCP data source](https://github.com/brettwooldridge/HikariCP)

Since the oracle driver code assumes the use of an Oracle specific DataSource, it can be a bit of a hassle getting it to work with a different datasource. The friendly people at spring developed the NativeJdbcExtractors for that ([as described here](http://docs.spring.io/spring-data/data-jdbc/docs/current/reference/html/orcl.streamsaq.html)). In the HikariWrappingDataSource created connections are wrapped by a class implements the internal.OracleConnection interface. It  forwards all Connection calls directly to the Connection and all OracleConnection specific calls to the connection retrieved via the NativeJdbcExtractor.

# When is this a good idea?
In general probably not, since AQ connections are kept active and open all the time there is no good reason including them in a connection pool. You're probably better served defining an oracle source for your AQ connection and using that source also as input for the Hikari connection pool.

However, we also had a large codebase of Oracle StoredProcedure mapping which also needed the OracleConnection internals, so for our code this was currently the best solution. If you have the same situation, it could work for you as well.

# Database requirements
The test code assumes the existence of a table called TEST_TABLE with a single column that accepts numbers or varchars.
It also needs a queue to exist with the name TEST_QUEUE. This should reside in the schema configured in hiraki.properties

# Code structure
- Maven module in hikari-oracle-aq directory
- All code resides in the com.bartenkelaar.hikari package.
- hikari.properties contains all properties read by the code.
- log4j.properties contains logger configuration.
- Application is the main class and configuration file.
- Spring bean configuration for the data source and JMS setup is in DataSourceConfig and JmsConfig respectively.
- HikariWrappingDataSource is the subclass of HikariDataSource that wraps the connections in an in inner WrappedConnection
- TestListener is a JMS listener on the TEST\_QUEUE table that inserts the messages directly into TEST\_TABLE
