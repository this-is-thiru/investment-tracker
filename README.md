## Testing End points:
* http://instance-url/auth/login
  * eg: http://16.16.187.252:8080/auth/login (ACS)

## Online RabbitMQ setup:
* Online RabbitMQ website used to implement the Queue Service is [Link](https://customer.cloudamqp.com/instance)
* create queue on remote broker (server)
* create exchange on remote broker (server)
* add binding to exchange
  * queue 
  * routing key
###### Note: binding for exchange is mandatory, based on this only consumer will consume from queue.