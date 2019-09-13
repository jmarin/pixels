# pixels
Demo of cloud photo management system built as a set of microservices with Akka Typed

## Dependencies

Run a cassandra docker container as follows:

`docker run -it -p 9042:9042 cassandra`

and configure the application to access it: 

`export CASSANDRA_CLUSTER_HOSTS=<docker environment ip>`

