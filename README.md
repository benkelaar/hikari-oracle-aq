# hikari-oracle-aq
Example code of hooking up Oracle AQ over Spring with [HikariCP data source](https://github.com/brettwooldridge/HikariCP)

Since the oracle driver code assumes the use of an Oracle specific DataSource, it can be a bit of a hassle getting it to work with a different datasource. The friendly people at spring developed the NativeJdbcExtractors for that ([as described here](http://docs.spring.io/spring-data/data-jdbc/docs/current/reference/html/orcl.streamsaq.html)). I've tried to use their code to wrap Connections served by the HikariDataSource, which works functionality-wise, except that the pool no longer seems to release (and thus reuse) any connections

This code is here to help me figure out why that is.

I suspect that the wrapping of the connections done via the SimpleNativeJdbcExtractor is interfering with
an equality check somewhere or something like that.

# Database requirements
The code assumes the existence of a table called TEST_TABLE with a single column that accepts numbers or varchars.
It also needs a queue to exist with the name TEST_QUEUE. This should reside in the schema configured in hiraki.properties

# Code structure
- Maven module in hikari-oracle-aq directory
- All code resides in the com.bartenkelaar.hikari package.
- hikari.properties contains all properties read by the code.
- log4j.properties contains logger configuration.
- Application is the main class and configuration file.
- Spring bean configuration for the data source and JMS setup is in DataSourceConfig and JmsConfig respectively.
- HikariWrappingDataSource is the subclass of HikariDataSource that wraps the connections via the NativeJdbcExtractor
- TestListener is a JMS listener on the TEST\_QUEUE table that inserts the messages directly into TEST\_TABLE
