# CloudMatch

This is an re-implementation of the engine powering my former startup [CloudMatch](http://cloudmatch.github.io/).
The idea is to allow entities to establish a connection and then exchange data via a Websocket connection. This project is currently a learning playground.

* Scala
* Akka (Clustering, Streaming, Http/Websockets)
* Docker
* Kubernetes (cluster discovery via Akka Management / Cluster Bootstrap)
* Slick
* RabbitMQ (as a cloud service)
* Flink

I wrote a couple of blog posts about the distribution of Websocket connections:

* [Akka Http, handle Websockets with Actors](http://ticofab.io/akka-http-websocket-example/)
* [Distributed Websocket server with Akka Http](http://ticofab.io/distributed-websocket-server-with-akka-http/)

The project consists of a few items:

### cloudmatch-service

The core of the project. I talked about this architecture at lots of conferences - notably during the online webinar with Lightbend. Click on the image below to view the recording.

[![Reactive From Code to Cloud](https://img.youtube.com/vi/FyneQrH-0Rc/0.jpg)](https://youtu.be/FyneQrH-0Rc?t=1901)

The idea is to have a Listener service which provide an endpoing for connecting devices, and then each device will live on a node of a cluster. The actors representing different devices will exchange messages carrying the sent payloads.

![Cloudmatch aervice architecture](https://raw.githubusercontent.com/ticofab/cloudmatch/master/images/cloudmatch-service-architecture.png)

### cloudmatch-stream

I use Flink as a data stream processor via its official RabbitMQ connector.

### cloudmatch-db

A service that hides a persistence layer using an in-memory database (H2) and uses Slick to insert and retrieve events. It is meant to be used as a sort of half-baked event journal.

### cloudmatch-dev-client

A tool to test the matching of connecting devices that I built in 2013 with the very first version of Angular. It still works!

### paw-endpoints

A few simple test endpoints for Rest clients such as Paw (my fave) or Postman.

### CI/CD

Automation is quite limited at the moment. A couple of scripts package all services in Docker containers and deploy to the current context in `kubectl`.

# Put it all together

Each actor representing a device talks to the database service to append events to the journal (such as `ConnectionEstablished`) and publishes events to the RabbitMQ topic where each message is the size in bytes of a message exchanged between two matched entities.
The Flink streaming engines consumes these events, say you want to window over how many bytes are exchanged per minute. 

The screenshot below shows a few things

* All pods and services deployed on my local K8S cluster
* The fabulous test client - devices 1 and 3 have matched and device 1 has sent "ciao" to device 3
* On the bottom left, logs from the Flink stream show that it consumed the message containing "4" from the RabbitMQ queue, as "ciao" is made by 4 unicode characters.

![Cloudmatch aervice architecture](https://raw.githubusercontent.com/ticofab/cloudmatch/master/images/working-screenshot.png)

# Potential usages in the real world

* IoT devices talking to each other
* Exchanging payload between devices during a videogame
* Anything where two devices need to exchange something, basically

## License

    Copyright 2019 Fabio Tiriticco - Fabway

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
