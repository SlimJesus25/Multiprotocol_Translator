package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import common.ConnectionDetails;
import common.IProducer;
import eu.arrowhead.application.skeleton.consumer.classes.Constants;
import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import eu.arrowhead.application.skeleton.consumer.classes.Utils;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.Future;

public class KafkaCustomProducer extends IProducer {

    private KafkaProducer<String, String> producer;
    
    private org.slf4j.Logger log = LoggerFactory.getLogger(KafkaCustomProducer.class);

    private ProducerConfig producerConfig;

    private ConsumerGroupMetadata metadata2;

    private PubSubSettings settings;

    private int numberOfMessages = 0;

    long thing = System.currentTimeMillis();

    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    private final Logger logger = LogManager.getLogger(KafkaCustomProducer.class);

    private List<Integer> threadCount = new ArrayList<>();
    private List<Integer> threadPeakCount = new ArrayList<>();
    private List<String> heapUsage = new ArrayList<>();
    private List<String> nonHeapUsage = new ArrayList<>();
    private List<Integer> availableProcessors = new ArrayList<>();
    private List<Double> sysLoadAvg = new ArrayList<>();

    public KafkaCustomProducer(ConnectionDetails connectionDetails, Map<String, String> settings) {
        super(connectionDetails, settings);
        this.settings = new PubSubSettings(settings);
        loadDefaults(settings);
        producerConfig = new ProducerConfig(Constants.objectifyMap(settings));

        metadata2 = new ConsumerGroupMetadata(this.settings.getClientId());
        createProducer();
    }

    private void loadDefaults(Map<String,String> settings) {
        if (!settings.containsKey("key.serializer")) {
            settings.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        }

        if (!settings.containsKey("value.serializer")) {
            settings.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        }
    }

    @Override
    public void produce(String topic, String message) {

        String useTopic;

        if (settings.getTopic().equals("")) {
            useTopic = topicFromConsumer(topic);
        } else {
            useTopic = settings.getTopic();
        }

        String messageId;
        if (settings.isRandomId()) {
            messageId = UUID.randomUUID().toString();
        } else {
            messageId = message;
        }

        final ProducerRecord<String, String> record = new ProducerRecord<>(useTopic, messageId, message);

        if (numberOfMessages == 1) {
            Utils.threads(threadCount, threadPeakCount);
            Utils.memory(heapUsage, nonHeapUsage);

            thing = System.currentTimeMillis();
        }

        numberOfMessages++;

        try {
            Future<RecordMetadata> future = producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {

                    if (e != null) {
                        logger.error("Failed producing kafka message");
                        logger.error(e.toString());
                        throw new RuntimeException(e);
                    } else {
                        // logger.info("Produced kafka message to topic " + topic);
                    }


                }
            });
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            logger.warn(e);
        }

        if(this.numberOfMessages == 50000f || this.numberOfMessages == 25000f || this.numberOfMessages == 75000f){
            Utils.threads(threadCount, threadPeakCount);
            Utils.memory(heapUsage, nonHeapUsage);
        }

        if (numberOfMessages == 100000f) {
            long execTime = System.currentTimeMillis() - thing;
            log.info("Messages per second + " + (100000f / (execTime / 1000f)));
            log.info("Execution time: " + execTime / 1000f);
            Utils.cpu(availableProcessors, sysLoadAvg);
            Utils.cpuInfo(availableProcessors, sysLoadAvg, log);
            Utils.memoryInfo(heapUsage, nonHeapUsage, log);
            Utils.threadsInfo(threadCount, threadPeakCount, log);
            numberOfMessages = 0;
            clearLists();
        }

        // LocalDateTime fin = LocalDateTime.now();
        // log.info("Tempo de produção: " + Duration.between(ini.toLocalTime(), fin.toLocalTime()));
    }


    public void createProducer() {


        Map<String,Object> config = producerConfig.originals();

            ConnectionDetails cd = this.getConnectionDetails();
            config.put("bootstrap.servers", cd.getAddress() + ":" + cd.getPort());

            switch (settings.getQos()) {
                case 0:
                    config.put(ProducerConfig.ACKS_CONFIG, "0");
                    break;
                case 2:
                    config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
                    config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
                case 1:
                    config.put(ProducerConfig.ACKS_CONFIG,"all");
                    break;
            }

            producer = new KafkaProducer<>(config);


            // logger.info("Kafka Producer connected to " + cd.getAddress() + ":" + cd.getPort() + " with topic " + settings.getTopic());

    }

    private void clearLists(){
        this.availableProcessors.clear();
        this.heapUsage.clear();
        this.nonHeapUsage.clear();
        this.threadCount.clear();
        this.threadPeakCount.clear();
        this.sysLoadAvg.clear();
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }
}
