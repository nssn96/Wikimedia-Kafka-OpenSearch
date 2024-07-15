package io.conduktor.demos.kafka.opensearch;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;


public class OpenSearchConsumer {

    //create a logger variable
    private static final Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getSimpleName());

    public static KafkaConsumer<String,String> createConsumerClient(){
        log.info("Inside the createConsumerClient function");
        //setting the properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,"Consumer-OpenSearch");

        log.info("Exiting the createConsumerClient function");
        return new KafkaConsumer<>(properties);

    }

    public static RestHighLevelClient createOpenSearchClient(){
        log.info("Inside the createOpenSearchClient function");
        String url = "http://localhost:9200/";  //url for the opensearch running in the local.
        //Create a URI from our url
        URI uri = URI.create(url);
        //extract login info if exists
        String userInfo = uri.getUserInfo();

        RestHighLevelClient restHighLevelClient=null;
        //REST client without security
        if(userInfo==null){
             restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(uri.getHost(), uri.getPort(),"http")));
        }
        log.info("Exiting the createOpenSearchClient function");
        return restHighLevelClient;
    }

    public static String extractId(String json){

        return JsonParser.parseString(json)
                .getAsJsonObject()
                .getAsJsonObject("meta")
                .get("id")
                .getAsString();
    }
    public static void main(String[] args) throws IOException, InterruptedException {

        //create opensearch client
        RestHighLevelClient restHighLevelClient = createOpenSearchClient();
        log.info("OpenSearch Rest Client created!!!");

        //create a Kafka consumer client
        KafkaConsumer<String,String> consumer = createConsumerClient();
        log.info("kafka Consumer client created!!!!");

        //create a index if it doesnt exist
        boolean ifExists = restHighLevelClient.indices().exists(new GetIndexRequest("wikimedia-index"),RequestOptions.DEFAULT);
        if(!ifExists){
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("wikimedia-index"); //index name as parameter
            restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info("wikimedia index created!!!");
        }else{
            log.info("wikimedia index already exists!!!");
        }

        //main code logic


        //get the main thread instance
        Thread mainThread = Thread.currentThread();

        //adding a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                log.info("Detected a shutdown.so gracefully shuting down by calling Consumer.wake");
                consumer.wakeup();

                //joining to the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            //Subscribe to the topic
            consumer.subscribe(Arrays.asList("wikimedia.recentchange"));
        //read from topic
        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            log.info("Number of records polled : " + records.count());

            if (records.count() > 0) {  // we will only process, if any record is polled.

                BulkRequest bulkRequest = new BulkRequest();

                try {
                    for (ConsumerRecord record : records) {

                        //Strategy 1 -- to use Kafka cordinates to pass ID
                        //String id = record.topic()+"_"+record.partition()+"_"+record.offset();

                        //Strategy 2 -- to get the id from data itself -- metadata.id -- very unique
                        String id = extractId(record.value().toString());

                        //send each record to openSearch using Index request
                        IndexRequest indexRequest = new IndexRequest("wikimedia-index")
                                .source(record.value(), XContentType.JSON)
                                .id(id); // This is for sending unique id from our data , to make it idempotent(unique processing)
                        //
                        //IndexResponse response = restHighLevelClient.index(indexRequest,RequestOptions.DEFAULT);
                        //Instead of Single processing, we will do bulk processing
                        bulkRequest.add(indexRequest);

                    }
                } catch (Exception e) {
                    //do nothing
                }

                // finally once all the messages polled are added to bulk request, send to opensearch
                BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                log.info("The bulk record was added. Count : " + bulkResponse.getItems().length);

            }

            // we add some timeout , to increase the number of messages for bulk processing
            Thread.sleep(3000);

        }

        }catch(WakeupException e){
            log.info("Consumer starting to shutdown");
            log.info(e.getMessage().toString());
        }catch(Exception e) {
            log.error("Unknown exception in the consumer");

        }finally {
            //close the client,consumer
            log.info("Closing the openseach and consumer client");
            restHighLevelClient.close();
            consumer.close();
        }





    }
}
