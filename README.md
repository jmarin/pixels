# pixels
Demo of cloud photo management system built as a set of microservices with Akka Typed

## Dependencies

Run a cassandra and postgres docker containers as follows:

`docker run -it -p 9042:9042 cassandra`
`docker run -it -p 5432:5432 postgres`

and configure the application to access it: 

`export HOST=localhost` 
`export CASSANDRA_CLUSTER_HOSTS=$HOST`
`export PG_HOST=$HOST`



