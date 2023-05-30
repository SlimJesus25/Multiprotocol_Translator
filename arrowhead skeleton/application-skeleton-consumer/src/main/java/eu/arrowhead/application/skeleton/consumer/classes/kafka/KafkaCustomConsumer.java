package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.JavaRepository;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.Repository;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.*;

public class KafkaCustomConsumer extends IConsumer {

    private Repository qosRepository = new JavaRepository();

    private KafkaConsumer<String,String> consumer;

    private KafkaConsumerSettings settings;

    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    private final Logger logger = LogManager.getLogger(KafkaCustomConsumer.class);

    public KafkaCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        this.settings = new KafkaConsumerSettings(settings);

    }

    @Override
    public void run() {
        createConsumer();
        subscribe();
    }

    public void createConsumer() {
        Properties config = new Properties();


            ConnectionDetails cd = this.getConnectionDetails();
            config.put("client.id", settings.getClientId());
            config.put("group.id", settings.getGroupId());
            config.put("bootstrap.servers", cd.getAddress() + ":" + cd.getPort());
            config.put("key.deserializer",org.apache.kafka.common.serialization.StringDeserializer.class);
            config.put("value.deserializer",org.apache.kafka.common.serialization.StringDeserializer.class);

            if (settings.getQos() == 2) {
                config.put("enable.auto.commit", "false");
            }


            consumer = new KafkaConsumer<>(config);
            // logger.info("Kafka Consumer connected to " + cd.getAddress() + ":" + cd.getPort() + " with topic " + settings.getTopic());


    }

    public void subscribe() {
        List<String> topicList = new ArrayList<>();
        topicList.add(settings.getTopic());
        consumer.subscribe(topicList);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            for (ConsumerRecord<String,String> record : records) {
                if (settings.getQos() == 0) {
                    newMessage(record);
                } else {
                    if (settings.getQos()==2) {
                        if (qosRepository.messageExists(String.valueOf(record.offset()))) {
                            newMessage(record);
                        }
                    } else {
                        newMessage(record);
                    }

                    offsets.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)); // commit offset for next message
                    consumer.commitSync(offsets);
                }

            }

        }


    }

    public void newMessage(ConsumerRecord<String,String> record) {
        // logger.info("Received new message from Kafka at " + getConnectionDetails() + " at topic " + record.topic());
        // logger.info("Message content - " + record.value());
        this.OnMessageReceived(record.topic(), record.value());
    }


}
