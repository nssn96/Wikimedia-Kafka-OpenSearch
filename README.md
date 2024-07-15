## Project Overview

This project demonstrates the use of Apache Kafka for producing and consuming data streams, integrating with Wikimedia's EventStreams service, and sending data to OpenSearch, an open-source fork of Elasticsearch. Below is an overview of the concepts covered and the key components used in the implementation.

### Concepts Covered

#### Producer
- **KafkaProducer**: The primary class for producing records to Kafka.
- **ProducerRecord**: Represents the record that will be sent to Kafka.
- **producer.send()**: Method to send the data to Kafka.
- **producer.close()**: Method to close the producer and release resources.
- Producing data with callbacks to obtain metadata details like partitions and offsets.

#### Consumer
- **KafkaConsumer**: The primary class for consuming records from Kafka.
- **Consumer.subscribe(topic)**: Method to subscribe to a topic.
- **Consumer.poll()**: Method to poll for new data from Kafka.
- **Consumer.close()**: Method to close the consumer and release resources.
- Implementing graceful shutdown using `consumer.wake` and `mainThread` options for detailed explanation.

### Wikimedia Producer Project

This project uses EventStreams to access various data streams, primarily the `recentchange` stream, which emits events related to recent changes on Wikipedia. These events include user actions that modify existing Wikipedia pages (editing and categorization) and the addition of new pages. For more information, refer to [Introduction to Apache Kafka with Wikipedia's EventStreams Service](https://towardsdatascience.com/introduction-to-apache-kafka-with-wikipedias-eventstreams-service-d06d4628e8d9).

### Integration with OpenSearch

OpenSearch is used as the analytics database for storing the consumed data.


Key components and configurations include:

### Wikimedia to KafkaProducer
- **ProducerConfig**: Configuration for the Kafka producer.
- **EventHandler**: Handles and receives the stream of data, implements `EventHandler`.
- **EventSource**: Configures an instance to receive data from the source.
- **EventSource.Builder**: Used for configuring an instance.
- **Producer acks and min.insync.replicas**: Configuration settings for ensuring data reliability.

### KafkaConsumer to OpenSearch

To integrate KafkaConsumer with OpenSearch, the following components are used:
- **RestHighLevelClient**: To create an OpenSearch client.
- **RestClient.builder**: For building the OpenSearch client.
- **JsonParser using gson library**: For parsing JSON data.
- **KafkaConsumer**: For consuming records from Kafka.
- **GetIndexRequest**: To check whether an index exists in OpenSearch.
- **CreateIndexRequest**: For creating a new index in OpenSearch.
- **IndexRequest**: For sending individual messages to OpenSearch.
- **BulkRequest**: For sending bulk messages to OpenSearch.

Refer to the code for detailed implementation and explanations of the above concepts and components.
