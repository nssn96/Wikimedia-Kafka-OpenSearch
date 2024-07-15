package io.conduktor.demos.kafka;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ProducerDemo {

    //logging variable
    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());
    public static void main(String[] args) {
        log.info("I am a Kafka Producer");

        //create and set the conduktor cluster properties
        Properties properties = new Properties();
        properties.setProperty("security.protocol","SASL_SSL");
        properties.setProperty("sasl.mechanism","SCRAM-SHA-256");
        properties.setProperty("sasl.jaas.config","org.apache.kafka.common.security.scram.ScramLoginModule required username='bmVhdC1wb255LTEwMzQzJOoX8r64w63DNYNECDL64rSP_jOlG5z-7L__zOIKp10' password='MWMyYTg1OWItZmYwNy00NTc2LThjMDEtMzliY2Q2YTQ3MWVl';");
        properties.setProperty("bootstrap.servers","neat-pony-10343-us1-kafka.upstash.io:9092");
        //set the producer properties
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        // create the Producer
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        //create a create Producer record
        String topic = "java_topic";
        String key = "Name";
        String value = "Surya";
        ProducerRecord<String,String> record = new ProducerRecord<>(topic,key,value);

        //send data
        producer.send(record);

        //flush the  producer
        // This will actually tell the producer to send all data and block until done --synchronous
        producer.flush();

        //close the producer
        producer.close();
    }
}
