# hikari-oracle-aq
Example code of hooking up Oracle AQ over Spring with HikariCP data source

This code is here to help me figure out why connecting to Oracle AQ over a HikariCP data source doesn't seem
to close the connections properly.

I suspect that the wrapping of the connections done via the SimpleNativeJdbcExtractor is interfering with
an equality check somewhere or something like that.

# Database requirements
The code assumes the existence of a table called TEST_TABLE with a single column that accepts numbers or varchars.
It also needs a queue to exist with the name TEST_QUEUE. This should reside in the schema configured in hiraki.properties
