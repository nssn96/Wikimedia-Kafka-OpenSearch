package io.conduktor.demos.kafka;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;


public class ConsumerDemoShutdown {

    //logging variable
    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoShutdown.class.getSimpleName());
    public static void main(String[] args) {
        log.info("I am a Kafka Consumer with graceful shutdown");

        //create and set the conduktor cluster properties
        Properties properties = new Properties();
        properties.setProperty("security.protocol","SASL_SSL");
        properties.setProperty("sasl.mechanism","SCRAM-SHA-256");
        properties.setProperty("sasl.jaas.config","org.apache.kafka.common.security.scram.ScramLoginModule required username='bmVhdC1wb255LTEwMzQzJOoX8r64w63DNYNECDL64rSP_jOlG5z-7L__zOIKp10' password='MWMyYTg1OWItZmYwNy00NTc2LThjMDEtMzliY2Q2YTQ3MWVl';");
        properties.setProperty("bootstrap.servers","neat-pony-10343-us1-kafka.upstash.io:9092");
        //set the producer properties
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer",StringDeserializer.class.getName());

        //unique consumer config
        String groupName="second-java-application";
        String topicName = "java_topic";
        properties.setProperty("group.id",groupName);
        properties.setProperty("auto.offset.reset","earliest");

        // create the Consumer
        KafkaConsumer<String,String> consumer  = new KafkaConsumer<>(properties);

        // get the instance of the main thread
        final Thread mainThread = Thread.currentThread();



        /* When there is an exit from the program, we will call consumer wake, and join to the main thread.
        When consumer wake is used it will call the wakeException when consumer poll is called,
        we can handle that exception and exit gracefully
        */

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                log.info("Detected a shutdown, lets exit by calling consumer.wakeup()....");
                // use consumer wake up
                consumer.wakeup();

                //join to the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);

                }

            }
        });

        try{
            //Subscribe the consumer to a topic
            consumer.subscribe(Arrays.asList(topicName));

            //Polling must happen continuously, so it must be in a while loop
            //poll the data from kafka -- return type will be ConsumerRecords
            while(true){
                ConsumerRecords<String,String> records = consumer.poll(Duration.ofSeconds(2));
                log.info("The Number of messages Consumed from kafka : "+records.count());

                for(ConsumerRecord record:records){
                    log.info("Key : "+record.key()+" Value : "+record.value()+"\nPartition : "
                            +record.partition()+" Offset : "+record.offset());
                }
            }

        }catch (WakeupException e){
            log.info("Consumer is starting to shutdown");
            e.printStackTrace();
        }catch (Exception e){
            log.error("Unexpected error in the consumer");
            e.printStackTrace();
        }
        finally {
            consumer.close(); //closing the consumer will commit the offset
            log.info("The Consumer is gracefully shutdown");
        }




    }
}
