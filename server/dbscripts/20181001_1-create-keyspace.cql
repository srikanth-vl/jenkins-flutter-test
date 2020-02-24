-- Keyspace for UniApp.

CREATE KEYSPACE uniapp_data WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;
