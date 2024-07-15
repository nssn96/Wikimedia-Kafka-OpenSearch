package io.conduktor.demos.kafka.wikimedia;



import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import com.launchdarkly.eventsource.background.BackgroundEventSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;


import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class WikimediaChangesProducer {

    public static void main(String[] args) throws InterruptedException {

        String localHost = "127.0.0.1:9092";
        String topicName = "wikimedia.recentchange";
        String url = "https://stream.wikimedia.org/v2/stream/recentchange";

        //Set the producer properties - bootstrapServers,serializer for key and value
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,localHost);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //Set the kafkaProducer
        KafkaProducer<String,String> producer = new KafkaProducer<>(properties);

        //Set EventHandler and EventSource to stream the data
        BackgroundEventHandler eventHandler = new WikimediaChangeHandler(producer,topicName);    //EventHandler will handle the stream of events

        //EventSource.Builder is used to configure an instance
        BackgroundEventSource.Builder builder = new BackgroundEventSource.Builder(eventHandler,new EventSource.Builder(URI.create(url)));
        BackgroundEventSource eventSource = builder.build();

        //start the producer in another thread
        eventSource.start(); //start the own thread and start the process

        //Since it starts its own thread , we need to stop/pause the main thread
        // we produce for 10 min and block the program untill then
        TimeUnit.MINUTES.sleep(10);

    }
}
