# ProjectReactor example
The simple project to work with <a href="https://projectreactor.io/docs/core/release/reference/#getting-started">projectreactor.io</a> framework.

## Description
Spring WebFlux uses projectreactor.io framework to work with reactive streams.

The good idea is to start how projectreactor.io works with.

### Unit tests
All of Project reactor examples are prepared in unit tests to see a logic and assert a result.

Pay attention that operations use logger to print important information in reactor steps.

The following project reactor aspects tested here:
* <a href="ReactorPublishSubscribeOnTest.java">ReactorPublishSubscribeOnTest.java</a> unit test works with subscribeOn and publishOn schedulers;
* <a href="ReactorFilteringTest.java">ReactorFilteringTest.java</a> unit test shows how to filter data in pipeline;
* <a href="ReactorCombiningTest.java">ReactorCombiningTest.java</a> unit test works with different Flux operations like zip, merge, concat, switch; 
* <a href="ReactorCreatingTest.java">ReactorCreatingTest.java</a> unit test works with all consumers of subscriber, interval, etc;
* <a href="ReactorBackpressureTest.java">ReactorBackpressureTest.java</a> unit tests to work with BackPressure based on a limit of requests in subscriber.


## Build

Clone and install <a href="https://github.com/StepanMelnik/Parent.git">Parent</a> project before building.

Clone current project.

### Maven
	> mvn clean test

