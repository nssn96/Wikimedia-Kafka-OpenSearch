package io.conduktor.demos.kafka.wikimedia;


import com.launchdarkly.eventsource.MessageEvent;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikimediaChangeHandler implements BackgroundEventHandler {

    KafkaProducer<String,String> kafkaProducer;
    String topicName;

    private static final Logger log = LoggerFactory.getLogger(WikimediaChangeHandler.class.getSimpleName());

    //creating a constructor using kafkaproducer and topicname
    public WikimediaChangeHandler(KafkaProducer<String,String> kafkaProducer, String topicName){
        this.kafkaProducer = kafkaProducer;
        this.topicName=topicName;
    }

    @Override
    public void onOpen() throws Exception {
        // dont need to do anything
    }

    @Override
    public void onClosed() throws Exception {
            //this means the stream is closed, therefore we can close our producer as well
            log.info("The Event stream is closed, Therefore closing the producer as well!!!");
            kafkaProducer.close();
    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) throws Exception {
        log.info("The message event : "+messageEvent.getData());
        //send the message event to kafka producer when you get a new event
        ProducerRecord<String,String> record = new ProducerRecord<>(topicName,messageEvent.getData());
        kafkaProducer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if(exception==null){
                    log.info("Event Stored at partition : "+metadata.partition()+"\n Event stored at offset : "+metadata.offset());
                }
                else{
                    log.error("Exception while send event to kafka ",exception.getMessage());
                }

            }
        });

    }

    @Override
    public void onComment(String comment) throws Exception {
        // dont need to do anything
    }

    @Override
    public void onError(Throwable t) {
           //here we are just logging the error
            log.error("Error in stream reading ",t);
    }
}
