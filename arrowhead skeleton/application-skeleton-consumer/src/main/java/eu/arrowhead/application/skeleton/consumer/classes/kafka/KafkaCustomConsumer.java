package eu.arrowhead.application.skeleton.consumer.classes.kafka;

import eu.arrowhead.application.skeleton.consumer.classes.Constants;
import eu.arrowhead.application.skeleton.consumer.classes.PubSubSettings;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.JavaRepository;
import eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase.Repository;
import common.ConnectionDetails;
import common.IConsumer;
import common.IProducer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Duration;
import java.util.*;

public class KafkaCustomConsumer extends IConsumer {

    private Repository qosRepository = new JavaRepository();

    private KafkaConsumer<String,String> consumer;

    private ConsumerConfig consumerConfig;

    private PubSubSettings settings;

    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    private final Logger logger = LogManager.getLogger(KafkaCustomConsumer.class);

    public KafkaCustomConsumer(ConnectionDetails connectionDetails, List<IProducer> producer, Map<String, String> settings) {
        super(connectionDetails, producer, settings);
        settings.put("bootstrap.servers", connectionDetails.getAddress() + ":" + connectionDetails.getPort());

        loadDefaults(settings);
        this.settings = new PubSubSettings(settings);
        consumerConfig = new ConsumerConfig(Constants.objectifyMap(settings));
    }

    private void loadDefaults(Map<String,String> settings) {
        if (!settings.containsKey("key.deserializer")) {
            settings.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        }

        if (!settings.containsKey("value.deserializer")) {
            settings.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        }

        if (!settings.containsKey("group.id")) {
            settings.put("group.id","middleware-group-"+UUID.randomUUID());
        }
    }

    @Override
    public void run() {
        //logger.info("ACABEI DE SER INICIADO! KAFKA");
        createConsumer();
        subscribe();
    }

    public void createConsumer() {
        Properties config = new Properties();

            /*
            ConnectionDetails cd = this.getConnectionDetails();
            config.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerConfig.getString(ConsumerConfig.CLIENT_ID_CONFIG));
            config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerConfig.getString(ConsumerConfig.GROUP_ID_CONFIG));
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerConfig.getString(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
            config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,org.apache.kafka.common.serialization.StringDeserializer.class);
            config.put("value.deserializer",org.apache.kafka.common.serialization.StringDeserializer.class);

            if (settings.getQos() == 2) {
                config.put("enable.auto.commit", "false");
            }
            */



            consumer = new KafkaConsumer<>(consumerConfig.originals());
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
                        if (!qosRepository.messageExists(record.key())) {
                            newMessage(record);
                            qosRepository.registerNewMessage(record.key());
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
        lastMessage = record.value();
        numberOfMessages++;

        this.OnMessageReceived(record.topic(), record.value());
    }

    public void stop() {
        consumer.close();
    }

}
