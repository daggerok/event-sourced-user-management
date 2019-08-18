# CQRS and event sourcing app [![Build Status](https://travis-ci.org/daggerok/event-sourced-user-management.svg?branch=master)](https://travis-ci.org/daggerok/event-sourced-user-management)
CQRS and event sourcing using plain embedded into jdk `com.sun.net.httpserver` with dynamic groovy spock tests

## TL;DR
this is an example of event sourced system I'm going to use for teaching... So please, don't even try 
use it in production

[Read: Why Developers Should Not Write Programs That Call 'sun' Packages](https://www.oracle.com/technetwork/java/faq-sun-packages-142232.html)

Status: in progress...

RoadMap:
- ~~Setup Gradle / Groovy repository with Spring IoC dependency injection and Spock test framework~~
- ~~Implement REST API with plain java embedded http server~~
- ~~Implement basic REST API info and shutdown endpoints~~
- ~~Implement and cover with tests User Account aggregate functionality~~
- ~~Implement and cover with tests User Account aggregate repository functionality~~
- ~~Implement user-account REST API endpoints~~
- ~~Implement Friend Request aggregate functionality~~
- ~~Implement Friend Request aggregate repository functionality~~
- Implement friend-request REST API endpoints
- Implement messenger aggregate functionality
- Implement messenger aggregate repository functionality
- Implement messenger REST API endpoints
- Implement async PubSub / Queue functionality to decouple direct REST API calls from aggregate repository
- Introduce read (query) side to avoid non efficient event sourcing repository querying
- Split current REST API into CQRS for eventual consistency and projections efficiency
- Implement CLI interface which is going to be interact with REST API by using CQRS
- Prepare future RoadMap for EventStore persistence introduction:
  - MapDB
  - JDBC
  - JPA
  - Spring Data
  - NoSQL
  - etc...
- Prepare future RoadMap for PubSub introduction:
  - plain pub-sub patter implementation
  - simple im-memory pub-sub solution from Google or Android library
  - Using spring application events
  - Kafka

<!--

_run kafka_

```bash
rm -rf /tmp/c ; git clone --depth=1 https://github.com/confluentinc/cp-docker-images.git /tmp/c
docker-compose -f /tmp/c/examples/kafka-single-node/docker-compose.yml up -d
docker-compose -f /tmp/c/examples/kafka-single-node/docker-compose.yml down -v --rmi local
```

_run app and test_

```bash
./gradlew run

http :8080/api/v1/messages message=hello
http :8080/api/v1/messages message=world
http :8080/api/v1/messages
http :8080
```

-->

**NOTE:** _For better developer experience during testing, use idea cURL integration tests from `rest-client*` files. Read more: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html_

## why?

just see how everything is clear in next analitics:

```yaml
events:
- OrderCreated(id=123)
- ItemAdded(product=Bose Headphones, price=400)
- FollowSimilarItemsLink(product=Sony Headphones)
- AccessReviews()
- VoteReviewHelpful(answer=5/5)
- ItemAdded(product=Sony Headphones, price=450)
- ItemRemoved(product=Bose Headphones, price=400)
- OrderConfirmed()
- OrderShipped()
```

resources:

* [@Log4l2](https://logging.apache.org/log4j/2.x/maven-artifacts.html)

<!--

* [YouTube: Building Event Driven Systems with Spring Cloud Stream](https://www.youtube.com/watch?v=LvmPa7YKgqM&t=2673s)
* [YouTube: 2018-10 Advanced Microservices Patterns: CQRS and Event Sourcing](https://www.youtube.com/watch?v=W_wySQ0lTI4&t=1448s)

-->
