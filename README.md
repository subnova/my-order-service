Spring Cloud Streams API Service
================================

Simple Spring Boot / Spring Cloud Streams application that provides a simple REST API
that accepts an 'order', posts it to a Kafka stream, waits until a (probably) modified
message is received on another Kafka stream and stores it in redis.  The modified
message can then be retrieved using the GET method on the API. 

Also provides support for configuration of the redis connection details from
CloudFoundry.

To build:

$ ```./gradlew build```

To run:

$ ```./gradlew composeUp```

N.B. the project at https://github.com/subnova/my-legacy-order-processor must be in
a parallel directory and built in order to using the docker-compose file unmodified.

A Swagger UI is provided at: http://localhost:8085/swagger-ui.html.

An order should look like:

```json
{
  "clientAccount":"123456",
  "service":"C24P",
  "to":{
    "customerName":"Bob Smith",
    "address":{
      "addressLine1":"12 Hope Street",
     "town":"Matlock",
     "postcode":"DE11AA",
     "countryCode":"UK"
    }
  },
  "shipments":[
    {"shipmentNumber":1,"totalShipmentCount":3,"shipmentDate":"2017-01-01"}
  ]
}
```

or

```json
{
  "clientAccount":"123456",
  "service":"C24P",
  "to":{
    "customerName":"Bob Smith",
    "storeCode":"AAAA",
    "customerContact":{
      "phoneNumber":"1234567",
      "emailAddress":"bob@smith.net",
      "contactStrategy":"EMAIL"
    }
  },
  "shipments":[
    {"shipmentNumber":1,"totalShipmentCount":1,"shipmentDate":"2017-01-01"}
  ]
}
```

The POSTed document is validated using JSON schema.

