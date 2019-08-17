# CQRS and event sourcing app [![Build Status](https://travis-ci.org/daggerok/cqrs-eventsourcing-user-management-example.svg?branch=master)](https://travis-ci.org/daggerok/cqrs-eventsourcing-user-management-example)
CQRS and event sourcing using plain embedded into jdk `com.sun.net.httpserver` with dynamic groovy spock tests

## TL;DR
this is an example of event sourced system I'm going to use for teaching... So please, don't even try 
use it in production

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
